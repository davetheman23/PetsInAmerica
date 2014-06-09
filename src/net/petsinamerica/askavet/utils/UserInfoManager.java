package net.petsinamerica.askavet.utils;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

import com.sina.weibo.sdk.openapi.models.User;

/**
 * This class only manage userinfo obtained from PIA web server
 * userinfo from weibo should be maintained elsewhere
 * 
 * @author David
 *
 */
public class UserInfoManager {
	
	public static interface Listener{
		public void onWeiboInfoStateChange();
		public void onPiaInfoStateChange();
	}
	
	private static boolean infoAvailable = false;
	private static boolean infoWeiboAvailable = false;
	
	public static String piaToken = null;
	public static String userid = null;
	public static String userName = null;
	public static String userDisplayName = null;
	public static String email = null;
	public static String avatarURL = null;
	public static Bitmap avatar = null;			// a decoded bitmap
	public static String language = null;
	public static String city = null;
	
	public static String weiboToken = null;		// the weibo token saved in the server
	public static String weiboUsername = null;	// the weibo username saved in the server
	public static User weiboUser = null;
	
	private static Listener mWeiboInfoListener = null;
	private static Listener mPiaInfoListener = null;
	public static void registerWeiboInfoListener (Listener listener){
		mWeiboInfoListener = listener;
	}
	public static void registerPiaInfoListener (Listener listener){
		mPiaInfoListener = listener;
	}
	
	public static void cacheUserInfo(Map<String, Object> resultMap){
		weiboToken = resultMap.get("weibo_token").toString();
		userid = resultMap.get("uid").toString();
		userName = resultMap.get("name").toString();
		userDisplayName = resultMap.get("displayname").toString();
		email = resultMap.get("email").toString();
		city = resultMap.get("city").toString();
		weiboUsername = resultMap.get("weibo").toString();
		avatarURL = resultMap.get("avatar").toString();
		language = resultMap.get("language").toString();
		
		infoAvailable = true;
		if (mPiaInfoListener!= null){
			mPiaInfoListener.onPiaInfoStateChange();
		}
	}
	
	public static void clearUserInfo(){
		weiboToken = null;
		userid = null;
		userName = null;
		userDisplayName = null;
		email = null;
		weiboUsername = null;
		avatarURL = null;
		language = null;
		
		infoAvailable = false;
		if (mPiaInfoListener!= null){
			mPiaInfoListener.onPiaInfoStateChange();
		}
		
	}
	
	public static void cacheWeiboUserInfo(User user, Context context){
		weiboUser = user;
		infoWeiboAvailable = true;
		if (mWeiboInfoListener != null){
			mWeiboInfoListener.onWeiboInfoStateChange();
		}
	}
	
	public static void clearWeiboUserInfo(){
		weiboUser = null;
		infoWeiboAvailable = false;
		if (mWeiboInfoListener != null){
			mWeiboInfoListener.onWeiboInfoStateChange();
		}
	}
	
	public static void cacheUserAvatar(Bitmap userAvatar){
		avatar = userAvatar;
	}
	
	public static boolean isInfoAvailable(){
		return infoAvailable;
	}
	
	public static boolean isWeiboInfoAvailable(){
		return infoWeiboAvailable;
	}

	
}

