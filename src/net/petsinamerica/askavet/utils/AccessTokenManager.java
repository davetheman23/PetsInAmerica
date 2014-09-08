
package net.petsinamerica.askavet.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Define parameters needed to access user data
 * 
 * @author David Zeng
 * @since 05/23/2014
 */
public class AccessTokenManager {
	
    private static final String sPREFERENCES_NAME = Constants.PREFERENCES_NAME;
    private static final String sPREFERENCES_NAME_WEIBO = sPREFERENCES_NAME +"_weibo";

    private static final String sKEY_USERID           = Constants.KEY_USERID;
    private static final String sKEY_ACCESS_TOKEN  	=  Constants.KEY_USERTOKEN;
    private static final String sKEY_EXPIRATION  	=  "expires_in";
    
    private static final String sTAG = "AccessTokenManager";
    
    /**
     * Save PIA token into a shared Pref object
     * 
     */
    public static void SaveAccessToken(Context context, AccessToken token) {
    	// do not save token if token is null
        if (null == context || null == token) {
            return;
        }
        // do not save token if token is empty
        if ("" == token.getUserId() || 
        	"" == token.getToken() ||
        	null == token.getExpiration()){
        	return;
        }
        // do not save token if token expired
        if (token.isExpired()){
        	return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(sKEY_USERID, token.getUserId());
        editor.putString(sKEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(sKEY_EXPIRATION, token.getExpiration().getTimeInMillis());
        editor.commit();
    }
    

    /**
     * Read token from SharedPreferences
     * return current token for current user
     * return null if user token not available
     * 
     */
    public static AccessToken readAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME, Context.MODE_PRIVATE);
        String uid = pref.getString(sKEY_USERID, "");
        String token = pref.getString(sKEY_ACCESS_TOKEN, "");
        Long dateLong = pref.getLong(sKEY_EXPIRATION, 0);
        
        if ("" == uid || "" == token || 0 == dateLong){
        	clear(context);
        	return null;
        }
        
        Calendar expireIn = Calendar.getInstance(); 
        expireIn.setTimeInMillis(dateLong);
        
        return new AccessToken(uid, token, expireIn);
    }
    /**
     * renew the PIA token, extending it locally 
     */
    public static AccessToken renewTokenLocal(Context context, AccessToken oldToken){
    	if (null == context) {
            return null;
        }
    	AccessToken newToken = new AccessToken(oldToken.getUserId(), oldToken.getToken());
    	return newToken;
    }
    
    /**
     * Get the PIA id of the user current in session
     * @param context
     * @return the user id in integer format; -1 if not available
     */
    public static int getUserId(Context context){
    	SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME, Context.MODE_PRIVATE);
        String uid = pref.getString(sKEY_USERID, "");
        int userid=-1;
        if (uid != null && uid != ""){
        	userid = Integer.parseInt(uid);
        }
        return userid;
    }
    
    /**
     * clear only the PIA token from SharedPreferences
     * 
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    
    /**
     * Save Weibo token into a shared Pref object
     * 
     */
    public static void SaveWeiboAccessToken(Context context, Oauth2AccessToken token) {
        if (null == context || null == token) {
            return;
        }
        Calendar expiresin = Calendar.getInstance();
        expiresin.setTimeInMillis(token.getExpiresTime());
        
        AccessToken weiboToken = new AccessToken(token.getUid(), 
        									 token.getToken(),
        									 expiresin);
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME_WEIBO, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(sKEY_USERID, token.getUid());
        editor.putString(sKEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(sKEY_EXPIRATION, token.getExpiresTime());
        editor.commit();
    }
    
    public static Oauth2AccessToken readWeiboAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        
        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME_WEIBO, Context.MODE_PRIVATE);
        String uid = pref.getString(sKEY_USERID, "");
        String tokenstring = pref.getString(sKEY_ACCESS_TOKEN, "");
        long expiresin = pref.getLong(sKEY_EXPIRATION, 0);
        
        if ("" == uid || "" == tokenstring || 0 == expiresin){
        	clearWeiboToken(context);
        	return null;
        }
        
        token.setUid(uid);
        token.setToken(tokenstring);
        token.setExpiresTime(expiresin);
        return token;
    }
    
    /**
     * clear only the weibo token from SharedPreferences
     * 
     */
    public static void clearWeiboToken(Context context) {
        if (null == context) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(sPREFERENCES_NAME_WEIBO, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    
    /**
     * clear all tokens from SharedPreferences
     */
    public static void clearAllTokens(Context context){
    	clear(context);
    	clearWeiboToken(context);
    }
    
    /**
     * this is a convenience method that reads the token from sharedpref and adds
     * the stored token into an HttpPost object, it is possible that the token is 
     * either null or expired, encompass this call with {@link #isSessionValid(Context)}
     * to ensure token validity, and handle when token invalid.  
     */
    public static HttpPost addAccessTokenPost(HttpPost post, Context context){
    	if (null != post && null != context){ 
	    	AccessToken accessToken = readAccessToken(context);
	    	post = addAccessTokenPost(post, context, accessToken);
    	}
    	return post;
    }
    
    /**
     * package the input post with a token supplied by the caller
     * no test of token expiration will be conducted, 
     */
    public static HttpPost addAccessTokenPost(HttpPost post, Context context, AccessToken token){
    	if (null != post && null != context && null != token){ 
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair(sKEY_USERID, token.getUserId()));
			nameValuePairs.add(new BasicNameValuePair(sKEY_ACCESS_TOKEN, token.getToken()));
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e(sTAG, e.toString());
			}
    	}
    	return post;
    }
    
    /**
     * Test if current session is valid, can be called from any page to see if current Pia token is valid
     * @param context supply current application context will surffice 
     * @return
     */
    public static boolean isSessionValid(Context context){
    	AccessToken token = readAccessToken(context);
    	
    	if (token!=null && !token.isExpired()){
    		return true;
    	}
    	return false;
    }

}
