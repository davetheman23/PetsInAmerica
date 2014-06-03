package net.petsinamerica.askavet.utils;

import java.util.Map;

import android.util.Log;


public class PiaApplication extends android.app.Application {
	
	public static final boolean DEBUGTAG = true;
	
	public static final String URL_USERINFO = "http://petsinamerica.net/new/api/userinfo";
	public static final String URL_LOGIN = "http://petsinamerica.net/new/api/login";
	
	public static final String APPTAG = "AskaVet";
    public static final String PREFERENCES_NAME = "net_pets_in_america_askavet";
    
    public static final String sTAG_USERNAME = "username";
	public static final String sTAG_PASSWORD = "password";
	
	
	
	//private static SharedPreferences preferences;
	

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUGTAG){
			Log.d(APPTAG, "Application.onCreate() is called");
		}
		//preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		//AccessTokenManager.clear(getApplicationContext());
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (DEBUGTAG){
			Log.d(APPTAG, "Application.onLowMemory() is called");
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (DEBUGTAG){
			Log.d(APPTAG, "Application.onTerminate() is called");
		}
		//Editor editor = preferences.edit();
	}
	
		
	
}
