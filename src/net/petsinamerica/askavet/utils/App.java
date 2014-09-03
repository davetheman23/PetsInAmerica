package net.petsinamerica.askavet.utils;

import java.io.File;

import net.petsinamerica.askavet.LoginActivity;

import com.igexin.sdk.PushManager;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;

/**
 * define all global variables here, for global variables that are known in advance 
 * and will not changed throughout the app life-time, define them in Constants.java 
 *
 */
public class App extends Application {
	/*
	 * define the directories that can be used to store application data,
	 * this application should always try to save in the external_directory first
	 */
	public static String EXTERNAL_DIRECTORY = null;
	public static String INTERNAL_DIRECTORY = null;
	
	/**
	 * the context of the app that will persist as long as the app is not killed,
	 * it can be called from anywhere within the application
	 */
	public static Context appContext;

	@Override
	public void onCreate() {
		super.onCreate();
		/* the external card exists*/
		INTERNAL_DIRECTORY = Environment.getRootDirectory().toString();
		INTERNAL_DIRECTORY = INTERNAL_DIRECTORY + File.separator + Constants.PIA_ROOT_DIR;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			EXTERNAL_DIRECTORY = Environment.getExternalStorageDirectory().toString();
			EXTERNAL_DIRECTORY = EXTERNAL_DIRECTORY + File.separator + Constants.PIA_ROOT_DIR;
		}
		
		appContext = getApplicationContext();
	}

	@Override
	public void onTerminate() {
		if (PushManager.getInstance() != null){
			PushManager.getInstance().turnOffPush(appContext);
			//PushManager.getInstance().stopService(appContext);
		}
		super.onTerminate();
	}
	
	/**
	 * This method basically logs out of current session
	 * @param context the activity in which this method is called, an activity instance is required
	 */
	public static void inValidateSession(final Context context){
		// 1. clear all token
		AccessTokenManager.clearAllTokens(appContext);
		UserInfoManager.clearAllUserInfo();
		// 2. turn off the pushservice
		if (PushManager.getInstance() != null ){
			PushManager.getInstance().turnOffPush(appContext);
		}
		// 3. show message to user to notify session expired
		//GeneralHelpers.showLoginExpiredAlertDialog(context);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("登录过期")
			.setMessage("您已经有一段长时间没登录了，请重新登录")
			.setCancelable(false)
			.setPositiveButton("好的", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					// 4. redirect the user to the login page
					Intent intent = new Intent(context, LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
}
