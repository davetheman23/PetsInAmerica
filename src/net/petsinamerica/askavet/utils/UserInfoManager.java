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
	
	
	
	private static boolean infoAvailable = false;
	private static boolean infoWeiboAvailable = false;
	
	/*
	 * pia specific data available from pia server
	 */
	public static String piaToken = null;
	public static String userid = null;
	public static String userName = null;
	public static String userDisplayName = null;
	public static String email = null;
	public static String avatarURL = null;
	public static Bitmap avatar = null;			// a decoded bitmap
	public static String language = null;
	public static String city = null;
	
	/*
	 * weibo data available from pia server
	 */
	public static String weiboToken = null;		// the weibo token saved in the server
	public static String weiboUsername = null;	// the weibo username saved in the server
	
	/*
	 * weibo data available from weibo server
	 */
	public static User weiboUser = null;
	
	
	/*
	 * listener interface, this will help trigger events on its subscriber
	 * currently, using static modifer to allow only one subscriber 
	 */
	public static interface Listener{
		public void onWeiboInfoStateChange();
		public void onPiaInfoStateChange();
	}
	private static Listener mWeiboInfoListener = null;
	private static Listener mPiaInfoListener = null;
	public static void registerWeiboInfoListener (Listener listener){
		mWeiboInfoListener = listener;
	}
	public static void registerPiaInfoListener (Listener listener){
		mPiaInfoListener = listener;
	}
	
	public static void cacheUserInfo(Map<String, Object> resultMap){
		if (resultMap.get("weibo_token") != null){
			weiboToken = resultMap.get("weibo_token").toString();
		}
		if (resultMap.get("uid") != null){
			userid = resultMap.get("uid").toString();
		}
		if (resultMap.get("name") != null){
			userName = resultMap.get("name").toString();
		}
		if (resultMap.get("displayname") != null){
			userDisplayName = resultMap.get("displayname").toString();
		}
		if (resultMap.get("email") != null){
			email = resultMap.get("email").toString();
		}
		if (resultMap.get("city") != null){
			city = resultMap.get("city").toString();
		}
		if (resultMap.get("weibo") != null){
			weiboUsername = resultMap.get("weibo").toString();
		}
		if (resultMap.get("avatar") != null){
			avatarURL = resultMap.get("avatar").toString();
		}
		if (resultMap.get("language") != null){
			language = resultMap.get("language").toString();
		}
		
		piaToken = AccessTokenManager.getUserToken(App.appContext);

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
		city = null;
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
	
	public static void clearAllUserInfo(){
		clearUserInfo();
		clearWeiboUserInfo();
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

