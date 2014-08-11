package net.petsinamerica.askavet.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class GeneralHelpers {
	
	/*
	 * define the media type int, for 
	 */
	public final static int MEDIA_TYPE_IMAGE = 1;
	
	public final static int MEDIA_TYPE_VIDEO = 2;

	
	/** 
	 * Create a File for saving an image or video
	 * @param type MEDIA_TYPE_IMAGE or MEDIA_TYPE_VIDEO 
	 * @param fileNameWithTimeStamp if false, only "img_temp" will be created, if file 
	 * 		  already existed, it will be overwriten. 
	 * @return a file object 
	 */
	public static File getOutputMediaFile(int type, boolean fileNameWithTimeStamp){
	    // To be safe, you should check that the SDCard is mounted
		String storageDir = App.EXTERNAL_DIRECTORY;
		if (storageDir == null){
			storageDir = App.INTERNAL_DIRECTORY;
		}

	    File mediaStorageDir = new File(storageDir + File.separator +
        		Constants.TEMP_SUBDIR);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d(Constants.PIA_ROOT_DIR, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String surfix = "temp";
	    if (fileNameWithTimeStamp){
	    	surfix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    }
	    
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    	String filePath = mediaStorageDir.getPath() + File.separator + 
	    								"img_" + surfix + ".jpg";
	        mediaFile = new File(filePath);
	    } else if(type == MEDIA_TYPE_VIDEO) {
	    	String filePath = mediaStorageDir.getPath()  + File.separator + 
	    								"img_" + surfix + ".mp4";
	        mediaFile = new File(filePath);
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	/**
	 * Share a text and an image to a native app via implicit intents, if the name supplied
	 * is specific enough, then it starts the app with matching name immediately; if null 
	 * string is provided for nameApp, all apps that can accept Intent.ACTION_SEND intent 
	 * will be shown for user selection. 
	 * 
	 * @param nameApp 	part of the name of the app to be shared content with, supply null to share
	 *    				to all native apps that can share image
	 * @param textUri 	a Uri for a text, supply null if no text to be shared
	 * @param imageUri 	a Uri for an image, supply null if no image to be shared
	 * @return gives back an intent that the activity can directly use to startActivity()
	 */
	public static Intent shareByApp(String nameApp, Uri textUri, Uri imageUri) {
	    Intent share = new Intent(android.content.Intent.ACTION_SEND);
	    share.setType("image/*");
	    if (nameApp == null){
	    	return share;
	    }
	    
	    List<Intent> targetedShareIntents = new ArrayList<Intent>();
	    List<ResolveInfo> resInfo = App.appContext.getPackageManager().queryIntentActivities(share, 0);
	    boolean appAvailable = true;
	    if (!resInfo.isEmpty()){
	        for (ResolveInfo info : resInfo) {
	            Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
	            targetedShare.setType("image/*"); // put here your mime type
	            
	            // get the activity that matching the name provided
	            if (info.activityInfo.name.toLowerCase().contains(nameApp) || 
	            	info.activityInfo.packageName.toLowerCase().contains(nameApp)) {
	                targetedShare.putExtra(Intent.EXTRA_TEXT, textUri.toString());
	                targetedShare.putExtra(Intent.EXTRA_STREAM, imageUri);
	                targetedShare.setPackage(info.activityInfo.packageName);
	                
	                // set the correct label name for each app/activity
	                CharSequence label = info.loadLabel(App.appContext.getPackageManager());
	                Intent extraIntents = new LabeledIntent(targetedShare, 
	                										info.activityInfo.name, 
	                										label, 
	                										info.activityInfo.icon);
	                targetedShareIntents.add(extraIntents);
	                //targetedShareIntents.add(targetedShare);
	            }
	        }
	        if (targetedShareIntents.size() == 0){
	        	appAvailable = false;
	        }
	    }else{
	    	appAvailable = false;
	    }
	    // create intent chooser and start activity as a result of user action
	    if (appAvailable){
	        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
	        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
	        return chooserIntent;
	    }else{
	    	/* TODO handle no native app with part of its name matching nameApp, 
	    	 * possibly needing to bring up the browswer and share there
	    	 */
	    }
	    return null;
	}
	/**
	 * parse a PIA server response, return results in a map if no error, 
	 * This applies to the responses that will return a ErrorMessage field, 
	 * @param response raw response from PIA server
	 * @return a data map contains responses from PIA server
	 */
	public static Map<String, Object> handlePiaResponse(HttpResponse response) throws 
									JSONException, HttpResponseException, IOException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		
		JSONObject responseObject = (JSONObject) new JSONTokener(responseString).nextValue(); 
		
		Map<String, Object> responseMap = JsonHelper.toMap(responseObject);
		
		if (responseMap != null){
			int errorCode = Integer.parseInt(responseMap.get(Constants.KEY_ERROR).toString());
			switch (errorCode) {
			case 0:
				@SuppressWarnings("unchecked")
				Map<String, Object> jObject = (Map<String, Object>)responseMap.get(Constants.KEY_RESULT);
				return jObject;
			default:
				Map<String, Object> jErrObject = new HashMap<String, Object>();
				jErrObject.put(Constants.KEY_ERROR_MESSAGE, responseMap.get(Constants.KEY_ERROR_MESSAGE).toString());
				return jErrObject;
			}
		}
		return null;
	}
	
	/**
	 * parse a PIA server response, return results in a list of maps if no error, 
	 * This applies to the responses that returns a list of objects 
	 * @param response raw response from PIA server
	 * @return a data map contains responses from PIA server, null if error
	 */
	public static List<Map<String, Object>> handlePiaResponseArray(HttpResponse response) throws 
	JSONException, HttpResponseException, IOException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		
		JSONObject responseObject = (JSONObject) new JSONTokener(responseString).nextValue(); 
		
		Map<String, Object> responseMap = JsonHelper.toMap(responseObject);
		
		if (responseMap != null){
			int errorCode = Integer.parseInt(responseMap.get(Constants.KEY_ERROR).toString());
			switch (errorCode) {
			case 0:
				JSONArray responseArray = responseObject.getJSONArray(Constants.KEY_RESULT);
				return JsonHelper.toList(responseArray);
			}
		}
		return null;
	}
	
	public static void showAlertDialog(Context context, String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title)
		.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("好的", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	
	/**
	 * On Android 3.0 and above, while using the ActionBar tabbed navigation style, the tabs sometimes appear above the action bar.
	 * This helper method allows you to control the 'hasEmbeddedTabs' behaviour.
	 * A value of true will put the tabs inside the ActionBar, a value of false will put it above or below the ActionBar.
	 * 
	 * You should call this method while initialising your ActionBar tabs. 
	 * Don't forget to also call this method during orientation changes (in the onConfigurationChanged() method).
	 * 
	 * @param inActionBar
	 * @param inHasEmbeddedTabs
	 */
	public static void setActionBarHasEmbeddedTabs(Object inActionBar, final boolean inHasEmbeddedTabs)
	{
		// get the ActionBar class
		Class<?> actionBarClass = inActionBar.getClass();

		// if it is a Jelly Bean implementation (ActionBarImplJB), get the super class (ActionBarImplICS)
		if ("android.support.v7.app.ActionBarImplJB".equals(actionBarClass.getName()))
		{
			actionBarClass = actionBarClass.getSuperclass();
		}

		try
		{
			// try to get the mActionBar field, because the current ActionBar is probably just a wrapper Class
			// if this fails, no worries, this will be an instance of the native ActionBar class or from the ActionBarImplBase class
			final Field actionBarField = actionBarClass.getDeclaredField("mActionBar");
			actionBarField.setAccessible(true);
			inActionBar = actionBarField.get(inActionBar);
			actionBarClass = inActionBar.getClass();
		}
		catch (IllegalAccessException e) {}
		catch (IllegalArgumentException e) {}
		catch (NoSuchFieldException e) {}

		try
		{
			// now call the method setHasEmbeddedTabs, this will put the tabs inside the ActionBar
			// if this fails, you're on you own ;-)
			final Method method = actionBarClass.getDeclaredMethod("setHasEmbeddedTabs", new Class[] { Boolean.TYPE });
			method.setAccessible(true);
			method.invoke(inActionBar, new Object[]{ inHasEmbeddedTabs });
		}
		catch (NoSuchMethodException e)	{}
		catch (InvocationTargetException e) {}
		catch (IllegalAccessException e) {}
		catch (IllegalArgumentException e) {}
	}

	/**
	 * This is a helper class for PIA api calls, it creates a new thread and subclass can simply
	 * implement the action to be taken on completion of the API call, by default a map object will be returned 
	 * if no error, using {@link #setResultType(Type)} to set the result type if a non-map object will be 
	 * returned. Additionally, {@link #addParamstoPost(HttpPost, Context)} can be overwritten to add more 
	 * parameters to an API call
	 * The map object is the result 
	 * @author David
	 *
	 */
	public static abstract class CallPiaApiInBackground extends AsyncTask<String, Void, Object>{
		
		public static enum Type{
			list, map,
		}
		
		private Type mType = Type.map;
		
		private Context mContext = App.appContext;
		
		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		/**1st level Json key to retrieve results, 
		 * default is defined in R.string.JSON_tag_result
		 */
		public void setResultType(Type type){
			mType = type;
		}
		
		@Override
		protected Object doInBackground(String... params) {
			HttpPost post = new HttpPost(params[0]);
			
			AccessTokenManager.addAccessTokenPost(post, mContext);

			try {
				addParamstoPost(post, mContext);
				
				// execute post
				HttpResponse response = mClient.execute(post);
				
				if (mType == Type.list){
					return handlePiaResponseArray(response);
				}else if (mType == Type.map){
					return handlePiaResponse(response);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}finally{
				if (mClient!=null){
					mClient.close();
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			if (result != null){
				Toast.makeText(mContext, "成功", Toast.LENGTH_LONG).show();		
			}else{
				Toast.makeText(mContext, "失败", Toast.LENGTH_LONG).show();
			}
			
			List<Map<String, Object>> result_list = null;
			Map<String, Object> result_map = null;
			if (mType == Type.list){
				result_list = (List<Map<String, Object>>)result;
				onCallCompleted(result_list);
				
			}else if(mType == Type.map){
				result_map = (Map<String, Object>)result;
				onCallCompleted(result_map);
			}
			
			
		}

		/** perform action when it is completed, if result is of map type, 
		 * then no implementation is needed for this method */
		protected abstract void onCallCompleted(Map<String, Object> result);
		
		/** perform action when it is completed, if result is of list type, 
		 * then no implementation is needed for this method */
		protected abstract void onCallCompleted(List<Map<String, Object>> result);
		
		/** add additional parameters to the post object */
		protected void addParamstoPost(HttpPost post, Context context) 
				 						throws UnsupportedEncodingException{
			
		}
	}
	


}
