package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.LoginActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;

import com.igexin.sdk.PushManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * This is a helper class for PIA api calls, it creates a new thread and subclass can simply
 * implement the action to be taken on completion of the API call, by default a map object will be returned 
 * if no error, using {@link #setParameters(Type)} to set the result type if a non-map object will be 
 * returned. Additionally, {@link #addParamstoPost(HttpPost, Context)} can be overwritten to add more 
 * parameters to an API call
 * The map object is the result 
 * @author David
 *
 */
public abstract class CallPiaApiInBackground extends AsyncTask<String, Void, Object>{
	
	public final static int TYPE_RETURN_MAP = 0;
	public final static int TYPE_RETURN_LIST = 1;
	public final static int TYPE_RETURN_INT = 2;
	
	
	private int mType = TYPE_RETURN_MAP;
	
	private boolean mRequireValidSession = true;
	
	private boolean mIsStarted = false;
	private boolean mIsIdle = true;
	private boolean mShowDialog = false;
	
	private Context mContext = App.appContext;
	
	private ProgressDialog mDialog = null;
	
	
	protected AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
	
	/** set parameters to customize this abstract class
	 * @param context   context of which started this API call task
	 * @param resultType	the type of the http response object, default is map type 
	 * @param requireValidSession	whether this call require a valid login session, default is true
	 */
	public CallPiaApiInBackground setParameters(Context context, int resultType, boolean requireValidSession){
		mContext = context;
		mType = resultType;
		mRequireValidSession = requireValidSession;
		return this;
	}
	
	/** set parameters to customize this abstract class
	 * @param context   context of which started this API call task
	 * @param resultType	the type of the http response object, default is map type 
	 */
	public CallPiaApiInBackground setParameters(Context context, int resultType){
		mContext = context;
		mType = resultType;
		return this;
	}
	
	/**
	 * set if to show the progress dialog during the doInBackground
	 * @param showDialog default is false, set true to show a progress dialog
	 */
	public CallPiaApiInBackground setProgressDialog(boolean showDialog){
		mShowDialog = showDialog;
		return this;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mIsStarted = false;
		mIsIdle = true;
		if (mDialog == null && mShowDialog){
			mDialog = new ProgressDialog(mContext);
			mDialog.setMessage("请稍后...");
			mDialog.show();
		}
		if (!AccessTokenManager.isSessionValid(App.appContext) && mRequireValidSession){
			handleInvalidSession();
		}
	}
	
	@Override
	protected Object doInBackground(String... params) {
		HttpPost post = new HttpPost(params[0]);
		
		mIsStarted = true;
		mIsIdle = false;
		
		if (mRequireValidSession){
			AccessTokenManager.addAccessTokenPost(post, mContext);
		}

		try {
			addParamstoPost(post, mContext);
			
			// execute post
			HttpResponse response = mClient.execute(post);
			switch (mType){
			case TYPE_RETURN_LIST:
				return GeneralHelpers.handlePiaResponseArray(response);
			case TYPE_RETURN_MAP:
				return GeneralHelpers.handlePiaResponse(response);
			case TYPE_RETURN_INT:
				return GeneralHelpers.handlePiaResponseInt(response);
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
		if (mDialog!= null && mDialog.isShowing()){
			mDialog.dismiss();
		}
		if (isCancelled()){
			return;
		}
		// -- below for alpha test use -- //
		if (result != null){
			String outstr = result.toString();
			int len = result.toString().length();
			outstr = outstr.substring(1, (len-1>20? 20 : len-1));
			Toast.makeText(mContext, "成功" + outstr, Toast.LENGTH_LONG).show();		
		}else{
			Toast.makeText(mContext, "失败", Toast.LENGTH_LONG).show();
		}
		// -- above for alpha test use -- //
		
		List<Map<String, Object>> result_list = null;
		Map<String, Object> result_map = null;
		Integer result_int = null;
		switch (mType){
		case TYPE_RETURN_LIST:
			result_list = (List<Map<String, Object>>)result;
			onCallCompleted(result_list);
			break;
		case TYPE_RETURN_MAP:
			result_map = (Map<String, Object>)result;
			onCallCompleted(result_map);
			break;
		case TYPE_RETURN_INT:
			result_int = (Integer)result;
			onCallCompleted(result_int);
			break;
		}
		mIsIdle = true;
		
	}

	/** perform action when it is completed, if result is not of list type, 
	 * then no implementation is needed for this method */
	protected abstract void onCallCompleted(Map<String, Object> result);
	
	/** perform action when it is completed, if result is not of map type, 
	 * then no implementation is needed for this method */
	protected abstract void onCallCompleted(List<Map<String, Object>> result);
	
	/** perform action when it is completed, if result is not of integer type, 
	 * then no implementation is needed for this method */
	protected abstract void onCallCompleted(Integer result);
	
	/** this provides a way for subclasses to handle when session is invalid
	 * such as token is expired or not available. Note canceling background thread
	 * is handled automatically, handle other events such as clearing token, and log out*/
	protected void handleInvalidSession(){
		// 1. cancel thread
		cancel(true);
		
		// 2. invalidate the login session
		App.inValidateSession(mContext);
	};
	
	/** add additional parameters to the post object, do not need to call super(),
	 * this assumes the app session is valid */
	protected void addParamstoPost(HttpPost post, Context context) 
			 						throws UnsupportedEncodingException, IOException{
	}
	
	/**
	 * Check if the current AsyncTask started
	 * @return
	 */
	public boolean isStarted(){
		return mIsStarted;
	}
	
	/**
	 * Check if the current AsyncTask is in working state
	 * @return true if thread is idle, false if thread is running in background
	 */
	public boolean isIdle(){
		return mIsIdle;
	}
	
}