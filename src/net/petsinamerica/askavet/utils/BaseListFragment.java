package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public abstract class BaseListFragment extends ListFragment{
	
	//public static final String PREFS_NAME = "PetInAmerica_ListArticles";
	//public static final String KEY_ARTICLE_READ = "Article_Read";

	private static final String TAG = "BaseListFragment";
	
	/*
	 * the key at the first level of the JSON response object
	 */
	private String KEY_LIST;
	
	private int mPage = 1;
	private boolean mflag_addData = false;		// false-don't add data
	private boolean mIsUserSpecific = false;	// flag for user specific data
	private ArrayAdapter<Map<String, Object>> mBaseAdapter;
	private Context mContext;
	private String mUrl;
	private View mfooterview; 
	
	/**
	 * Set the custom list adapter to use when the Http request is completed,
	 * simply call function setCustomAdapter() passing in an instance of the 
	 * custom listadapter
	 * 
	 * @param resultArray  - the resulting data 
	 */
	protected abstract void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray);
	
	protected abstract void onItemClickAction(View v, int position, long id);
	
	protected void setPage(int page){
		mPage = page;
	}
	
	protected int getPage(){
		return mPage;
	}
	
	//Set<String> mReadArticleList = null;		// the article list that has been read by the user
	//SharedPreferences mUsageData;
		
	public void setParameters(String url,  String jsonListKey) {
		mUrl = url;
		KEY_LIST = jsonListKey;
	}
	public void setUserDataFlag(boolean isUserSpecific){
		mIsUserSpecific = isUserSpecific;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
	}
	
	public void loadListInBackground(){
		// fetch list data from the network
		new HttpPostTask().execute(mUrl + "/" + Integer.toString(mPage));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getListAdapter() == null){
			// first time the view is created
			loadListInBackground();
		}
		
		// set up footer 
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mfooterview = (View) inflater.inflate(R.layout.list_footer, null);
		getListView().addFooterView(mfooterview);
		getListView().setFooterDividersEnabled(true);
		// set the footer as invisible only make it visible when needed
		mfooterview.setVisibility(View.GONE);
		mfooterview.setClickable(false);
		
		//mReadArticleList = new HashSet<String>();
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);		
		
		// monitor scroll activity, add more articles when scroll close to bottom
		getListView().setOnScrollListener(new OnScrollListener() {
			/*
			 * this listener is used to continuously load more article when scroll down to bottom
			 */
		@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount - 1 && totalItemCount != 0)
				{
					
					// when the visible item reaches the last item, 
					if (mflag_addData == false)
					{
						mPage += 1;
						mflag_addData = true;
						loadListInBackground();
						mfooterview.setVisibility(View.VISIBLE);
					}
				}
			}
		});
		
	}
	
	
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (position == getListView().getCount() - 1){
			// if footer is clicked, this assumes footer exists
			return;
		}
		onItemClickAction(v, position, id);
	}
	
	/**
	 * This function displays no content available when the 
	 * list is empty
	 * Overwriting this function to customize actions to be taken 
	 * when the list is empty 
	 */
	protected void handleEmptyList(){
		TextView tvFooter = (TextView) mfooterview
				.findViewById(R.id.list_footer_tv_loading);
		tvFooter.setText("没有可显示的文章");
		
		ProgressBar pbFooter = (ProgressBar) mfooterview
						.findViewById(R.id.list_footer_pb_loading);
		pbFooter.setVisibility(View.INVISIBLE);
		
	}
	
	/**
	 * This function displays "the end of the list" when the no more
	 * list items is available 
	 * Overwriting this function to customize actions to be taken 
	 * when the list is complete
	 */
	protected void handleEndofList(){
		TextView tvFooter = (TextView) mfooterview
				.findViewById(R.id.list_footer_tv_loading);
		tvFooter.setText("已获取全部文章");
		
		ProgressBar pbFooter = (ProgressBar) mfooterview
						.findViewById(R.id.list_footer_pb_loading);
		pbFooter.setVisibility(View.INVISIBLE);

	}
	
	
	protected void setCustomAdapter(ArrayAdapter<Map<String, Object>> customAdapter){
		mBaseAdapter = customAdapter;
		setListAdapter(mBaseAdapter);
	}
	

	private class HttpPostTask extends AsyncTask<String, Void, List<Map<String, Object>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<Map<String, Object>> doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			if (mIsUserSpecific){
				AccessTokenManager.addAccessTokenPost(post, mContext);
			}
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(post);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = null;
				JSONArray responseArray = null;
				List<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
				
				responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				if (responseObject !=null){
					String listObject = responseObject.getString(KEY_LIST);
					if (listObject.equalsIgnoreCase("null")){
						return arrayList;
					}
				}
				if (responseObject !=null){
					responseArray = responseObject.getJSONArray(KEY_LIST);
					if (responseArray != null){
						arrayList = JsonHelper.toList(responseArray);
						return arrayList;
					}
				}
				return arrayList;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "JSONException");
			}finally{
				if (null != mClient){
					mClient.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> resultArray) {
			if (isAdded() && resultArray != null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				if (resultArray.size() > 0 ){
					if (mflag_addData == false){
						// set adapter only for the first time.
						onHttpDoneSetAdapter(resultArray);
					}else{
						if (getListAdapter() != null){
							mBaseAdapter.addAll(resultArray);
							mflag_addData = false;
						}else{
							// it is not normal resultArray.size() >0 but mflag_addData is true
							Log.e(TAG, "ListAdapter is not set properly, plese check!");
						}
						
						mfooterview.setVisibility(View.GONE);
					}
				}else{
					// no more list items to be displayed
					// handle it
					if (getListView().getCount() < 1){
						handleEmptyList();;
					}else{
						handleEndofList();
					}
				}
				
			}
		}
	}
	
	
	private AttributeSet getAttributeSet(Context context, int layoutResource, String ViewName){
		// function to fetch the attribute set defined in layoutResource element ViewName
		AttributeSet Attributes = null;
		XmlPullParser parser = context.getResources().getLayout(layoutResource);
		int state = 0;
	    do {
	        try {
	            state = parser.next();
	        } catch (XmlPullParserException e1) {
	            e1.printStackTrace();
	        } catch (IOException e1) {
	            e1.printStackTrace();
	        }       
	        if (state == XmlPullParser.START_TAG) {
	            if (parser.getName().equals(ViewName)) {
	            	Attributes = Xml.asAttributeSet(parser);
	                break;
	            }
	        }
	    } while(state != XmlPullParser.END_DOCUMENT);
	    return Attributes;
	}

}
