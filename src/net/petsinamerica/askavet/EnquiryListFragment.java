package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.JsonHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class EnquiryListFragment extends ListFragment {

	public static final int URL_ALL_ENQUIRY = 0;
	public static final int URL_MY_ENQUIRY = 1;
	private static final String sURL_ENQUIRY = "http://petsinamerica.net/new/api/publicQueryList/";
	private static final String sURL_MYENQUIRY = "http://petsinamerica.net/new/api/userQueryList/";
	
	private static String sTAG_RESULT;
	private static String sTAG_CONTENT;
	
	private String mUrl = sURL_ENQUIRY;		// default URL, use setUrl() to change URL
	private int mUrlType = URL_ALL_ENQUIRY;		// default URL type, use setUrl() to change type
	
	private int mPageMyEnqury = 1;				// page number for list
	private int mPageAllEnqury = 1;				// page number for list
	EnquiryListAdapter mELAdapter;
	private Context mContext;
	private Button mBtnMyQuery;
	private Button mBtnAllQuery;
	private TextView mTvEmpty;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		sTAG_RESULT = mContext.getResources().getString(R.string.JSON_tag_result);
		sTAG_CONTENT = mContext.getResources().getString(R.string.JSON_tag_content);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_enquirylist,
				container, false);
		// set up all query button, and listener load new list if necessary
		mBtnAllQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_allquery);
		mBtnAllQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (URL_ALL_ENQUIRY != mUrlType){
					setUrl(URL_ALL_ENQUIRY);
					loadEnquiryList(mUrl,mPageAllEnqury);
				}else{
					// TODO: refresh the contents if necessary
				}
			}
		});
		// set up my query button, and listener load new list if necessary
		mBtnMyQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_myquery);
		mBtnMyQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (URL_MY_ENQUIRY != mUrlType){
					setUrl(URL_MY_ENQUIRY);
					loadEnquiryList(mUrl, mPageMyEnqury);
				}else{
					// TODO: refresh the contents if necessary
				}
			}
		});
		
		// get a refernce to the textview when no data available to list
		mTvEmpty = (TextView) rootView.findViewById(android.R.id.empty);
		
		return rootView;
	}
	
	/*
	 *  Determine which query list to show, either 
	 */
	public void setUrl(int urlType){
		mUrlType = urlType;
		if (urlType == URL_ALL_ENQUIRY){
			mUrl = sURL_ENQUIRY;
		}else if (urlType == URL_MY_ENQUIRY){
			mUrl = sURL_MYENQUIRY;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		loadEnquiryList(mUrl, mPageAllEnqury);
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);	
	}
	
	private void loadEnquiryList(String url, int page){
		// fetch enquiry list from the website
		new HttpPostTask().execute(url + Integer.toString(page));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// obtain the article ID clicked
		//int articleID = mELAdapter.getQueryID(v);
		
		// store the article ID clicked
		//Record_Usage(articleID);
		
		// start a new activity 
		String enQueryContent = mELAdapter.getEnqueryContent(v);
		Intent newIntent = new Intent(mContext, EnquiryActivity.class);
		newIntent.putExtra(sTAG_CONTENT, enQueryContent);
		startActivity(newIntent);
	}
	
	
	private class HttpPostTask extends AsyncTask<String, Void, List<Map<String, String>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected List<Map<String, String>> doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			
			// add token information when request the list of my query
			if (mUrlType == URL_MY_ENQUIRY){
				AccessToken accessToken = AccessTokenManager.readAccessToken(mContext);
				if (!accessToken.isExpired()){
					post = AccessTokenManager.addAccessTokenPost(post, mContext, accessToken);
				}else{
					mTvEmpty.setText("请先登录!");
					// TODO: handle when access token is expired, such as display a login button and ask for login
				}
				
			}
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(post);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				
				//String userQueryList = new BasicResponseHandler().handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = null;
				JSONArray enqueries = null;
				List<Map<String, String>> enquiryList = null;
				
				responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				enqueries = responseObject.getJSONArray(sTAG_RESULT);
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
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, String>> resultArray) {
			if (null != mClient)
				mClient.close();
			if (!isAdded() || resultArray == null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				return;
			}
			// if no data is fetched from the server
			if (resultArray.size() == 0){
				if (mPageMyEnqury == 1){
					// no matter which mUrlType, set to null
					setListAdapter(null);
				}
				if (mUrlType ==  URL_MY_ENQUIRY){
					// in addition, URL_MY_ENQUIRY case, change text
					mTvEmpty.setText("您还没有提问哟！");
				}
				return;
			}
			// if some data is obtained here
			switch (mUrlType){
			case URL_ALL_ENQUIRY:
				if (mPageAllEnqury == 1){
					mELAdapter = new EnquiryListAdapter(mContext, 
										R.layout.enquiry_list_item, resultArray);
					/*TODO: setup an asynctask to batch download the images, 
					       based on all urls from results*/
					
					setListAdapter(mELAdapter);
				}else{
					mELAdapter.addAll(resultArray);
				}
				break;
			case URL_MY_ENQUIRY:
				if (mPageMyEnqury == 1){
					mELAdapter = new EnquiryListAdapter(mContext, 
										R.layout.enquiry_list_item, resultArray);
					/*TODO: setup an asynctask to batch download the images, 
					       based on all urls from results*/
				
					setListAdapter(mELAdapter);
				}else{
					mELAdapter.addAll(resultArray);
				}
				break;
			}
		}
	}
}
