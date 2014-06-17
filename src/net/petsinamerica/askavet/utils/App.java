package net.petsinamerica.askavet.utils;

import java.io.File;

import android.app.Application;
import android.os.Environment;

public class App extends Application {
	/*
	 * define the directories that can be used to store application data,
	 * this application should always try to save in the external_directory first
	 */
	public static String EXTERNAL_DIRECTORY = null;
	public static String INTERNAL_DIRECTORY = null;
	public static final String PIA_ROOT_DIR = "AskaVet";

	@Override
	public void onCreate() {
		super.onCreate();
		/* the external card exists*/
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			EXTERNAL_DIRECTORY = Environment.getExternalStorageDirectory().toString();
			EXTERNAL_DIRECTORY = EXTERNAL_DIRECTORY + File.separator + PIA_ROOT_DIR;
		}else{
			INTERNAL_DIRECTORY = Environment.getRootDirectory().toString();
			INTERNAL_DIRECTORY = INTERNAL_DIRECTORY + File.separator + PIA_ROOT_DIR;
		}
		
		
	}
	
	
}
