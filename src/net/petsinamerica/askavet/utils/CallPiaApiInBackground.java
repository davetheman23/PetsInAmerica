package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.widget.Toast;

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
public abstract class CallPiaApiInBackground extends AsyncTask<String, Void, Object>{
	
	public final static int TYPE_RETURN_LIST = 1;
	public final static int TYPE_RETURN_MAP = 0;
	
	
	private int mType = TYPE_RETURN_MAP;
	
	private Context mContext = App.appContext;
	
	AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
	
	/**1st level Json key to retrieve results, 
	 * default is defined in R.string.JSON_tag_result
	 */
	public void setResultType(int type){
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
			
			if (mType == TYPE_RETURN_LIST){
				return GeneralHelpers.handlePiaResponseArray(response);
			}else if (mType == TYPE_RETURN_MAP){
				return GeneralHelpers.handlePiaResponse(response);
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
		if (mType == TYPE_RETURN_LIST){
			result_list = (List<Map<String, Object>>)result;
			onCallCompleted(result_list);
			
		}else if(mType == TYPE_RETURN_MAP){
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