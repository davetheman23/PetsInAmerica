
package net.petsinamerica.askavet.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Define parameters needed to access user data
 * 
 * @author David Zeng
 * @since 05/23/2014
 */
public class AccessTokenManager {
	
    private static final String sPREFERENCES_NAME = "net_pets_in_america_askavet";
    private static final String sPREFERENCES_NAME_WEIBO = sPREFERENCES_NAME +"_weibo";

    private static final String sKEY_USERID           = "userid";
    private static final String sKEY_ACCESS_TOKEN  	=  "token";
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
        token.setUid(pref.getString(sKEY_USERID, ""));
        token.setToken(pref.getString(sKEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(sKEY_EXPIRATION, 0));
        return token;
    }
    
    /**
     * package the input post with a non-expiring token stored in shared pref 
     * A test of token expiration will be conducted, if expired return the same
     * post supplied in the input
     */
    public static HttpPost addAccessTokenPost(HttpPost post, Context context){
    	if (null != post && null != context){ 
	    	AccessToken accessToken = readAccessToken(context);
	    	
	    	if (!accessToken.isExpired()){
				post = addAccessTokenPost(post, context, accessToken);
	    	}else{
	    		clear(context);
	    		// TODO: do something more when token expired
	    	}
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
     * clear token from SharedPreferences
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

}
