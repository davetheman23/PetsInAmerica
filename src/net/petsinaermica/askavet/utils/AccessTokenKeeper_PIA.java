
package net.petsinaermica.askavet.utils;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Define parameters needed to access user data
 * 
 * @author David Zeng
 * @since 05/23/2014
 */
public class AccessTokenKeeper_PIA {
    private static final String PREFERENCES_NAME = "net_pets_in_america_askavet";

    private static final String KEY_USERID           = "userid";
    private static final String KEY_ACCESS_TOKEN  	=  "token";
    private static final String KEY_EXPIRATION  	=  "expires_in";
    
    /**
     * Save token into a shared Pref object
     * 
     */
    public static void SaveAccessToken(Context context, AccessToken token) {
        if (null == context || null == token) {
            return;
        }
        if (token.isExpired()){
        	return;
        }
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(KEY_USERID, token.getUserId());
        editor.putString(KEY_ACCESS_TOKEN, token.getToken());
        editor.putString(KEY_EXPIRATION, token.getExpiration().toString());
        editor.commit();
    }

    /**
     * Read token from SharedPreferences
     * 
     */
    public static AccessToken readAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String uid = pref.getString(KEY_USERID, "");
        String token = pref.getString(KEY_ACCESS_TOKEN, "");
        @SuppressWarnings("deprecation")
		Date expires_date = new Date(pref.getString(KEY_EXPIRATION, ""));
        Calendar expires = Calendar.getInstance();
        expires.setTime(expires_date);
        
        return new AccessToken(uid, token, expires);
    }

    /**
     * clear token from SharedPreferences
     * 
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
