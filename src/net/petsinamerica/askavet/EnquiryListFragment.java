package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.petsinaermica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class EnquiryListFragment extends ListFragment {

	private static final String URL_ENQUIRY = "http://petsinamerica.net/new/api/publicQueryList/";
	private static String TAG_RESULT;
	private static String TAG_CONTENT;
	
	private int mPage = 1;
	boolean mflag_addData = false;		// false-don't add data
	EnquiryListAdapter mELAdapter;
	private Context mContext;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		TAG_RESULT = mContext.getResources().getString(R.string.JSON_tag_result);
		TAG_CONTENT = mContext.getResources().getString(R.string.JSON_tag_content);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
		// fetch enquiry list from the website
		new HttpGetTask().execute(URL_ENQUIRY + Integer.toString(mPage));
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);	
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		// obtain the article ID clicked
		//int articleID = mELAdapter.getQueryID(v);
		
		// store the article ID clicked
		//Record_Usage(articleID);
		
		// start a new activity 
		String query_content = mELAdapter.getQueryContent(v);
		Intent newIntent = new Intent(mContext, EnquiryActivity.class);
		//newIntent.putExtra("URL_API", articleURL_API);
		newIntent.putExtra(TAG_CONTENT, query_content);
		startActivity(newIntent);
	}
	
	
	private class HttpGetTask extends AsyncTask<String, Void, List<Map<String, String>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected List<Map<String, String>> doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			//JSONResponseHandler responseHandler = new JSONResponseHandler();
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(request);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = null;
				JSONArray enqueries = null;
				List<Map<String, String>> enquiryList = null;
				
				responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				enqueries = responseObject.getJSONArray(TAG_RESULT);
				if (enqueries != null){
					JsonHelper jhelper = new JsonHelper(); 
					enquiryList = jhelper.toList(enqueries);
				}			
				return enquiryList;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, String>> resultArray) {
			if (null != mClient)
				mClient.close();
			if (isAdded() && resultArray != null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				
				if (mflag_addData == false){
					mELAdapter = new EnquiryListAdapter(mContext, 
										R.layout.enquiry_list_item, resultArray);
					
					//setup an asynctask to batch download the images, based on all urls from results
					
					//getListView().addHeaderView(BuildHeaderView());
					setListAdapter(mELAdapter);
				}else{
					if (resultArray.size() > 0 ){
						mELAdapter.addAll(resultArray);
						mflag_addData = false;
					}
				}
			}
		}
	}

}
