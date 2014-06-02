package net.petsinamerica.askavet;

import net.petsinamerica.askavet.utils.AccessTokenManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class Application extends android.app.Application {
	
	public static final boolean DEBUGTAG = true;
	public static final String APPTAG = "AskaVet";
    public static final String PREFERENCES_NAME = "net_pets_in_america_askavet";
	
	private static SharedPreferences preferences;

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUGTAG){
			Log.d(APPTAG, "Application.onCreate() is called");
		}
		preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
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
		Editor editor = preferences.edit();
		
	}
	
	
	
}
