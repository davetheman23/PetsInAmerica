package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
	
	public static final boolean FLAG_URL_NO_PAGE = false;
	public static final boolean FLAG_URL_HAS_PAGE = true;
	//public static final String KEY_ARTICLE_READ = "Article_Read";
	
	// customize the style of the lists
	public static enum Style{
		card,
		normal //default style
	};
	
	private static final String TAG = "BaseListFragment";
	private static final String KEY_ERR = "error";
	
	/*
	 * the key at the first level of the JSON response object
	 * Note: don't make it static, because each instance of this fragment class
	 *       may have different key_list value. 
	 */
	private String KEY_LIST;
	
	/*
	 * mPage will be appended to the URL, for some API calls, it indicates 
	 * the page of the list, for other API calls, it could mean userid.  
	 */
	private int mPage = 1;
	private boolean mflag_page = true;		// flag whether a page should be added to url
	private boolean mflag_addData = false;		// false-don't add data
	private boolean mIsUserSpecific = false;	// flag for user specific data
	private boolean mHasFooter = true;			// flag to indicate if footer is needed
	private boolean mSetScrollListener = true;	// flag whether to setscrollListener for the listview
	
	private ArrayAdapter<Map<String, Object>> mBaseAdapter;
	private Context mContext;
	private String mUrl;
	private View mfooterview = null; 
	
	
	/**
	 * Set the custom list adapter to use when the Http request is completed,
	 * simply call function setCustomAdapter() passing in an instance of the 
	 * custom listadapter
	 * 
	 * @param resultArray  - the resulting data 
	 */
	protected abstract void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray);
	
	/**
	 *	 
	 * @param v
	 * @param position
	 * @param id
	 */
	protected abstract void onItemClickAction(View v, int position, long id);
	
	public void setPage(int page){
		mPage = page;
	}
	
	public int getPage(){
		return mPage;
	}
	public void setPageFlag(boolean flag){
		mflag_page = flag;
	}
	
	//Set<String> mReadArticleList = null;		// the article list that has been read by the user
	//SharedPreferences mUsageData;
		
	public void setParameters(String url,  String jsonListKey, boolean hasfooter) {
		mUrl = url;
		KEY_LIST = jsonListKey;
		mHasFooter = hasfooter;
	}
	
	public void setParameters(String url,  String jsonListKey, boolean hasfooter, 
			boolean isSetScrollListener) {
		mUrl = url;
		KEY_LIST = jsonListKey;
		mHasFooter = hasfooter;
		mSetScrollListener = isSetScrollListener;
	
	}
	public void setUserDataFlag(boolean isUserSpecific){
		mIsUserSpecific = isUserSpecific;
	}
	
	/**
	 * this is useful when certain style need to be achieved, call this function after
	 * the onViewCreated(). <p>
	 * If <b>card style</b> is needed, in the item layout xml, need to set both 
	 * android:layout_margin="10dp", android:background="@drawable/layer_card_background"
	 * and also passing in Style.card 
	 */
	public void setStyle(Style style){
		switch (style){
		case card:
			getListView().setDivider(null);
			getListView().setDividerHeight(10);
			getListView().setCacheColorHint(android.R.color.transparent);
			getListView().setBackgroundColor(mContext.getResources().getColor(R.color.WhiteSmoke));
			break;
		default:
			// do nothing
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
	}
	
	public void loadListInBackground(){
		if (!mflag_page){
			new HttpPostTask().execute(mUrl);
		}else{
			// fetch list data from the network
			new HttpPostTask().execute(mUrl + Integer.toString(mPage));
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getListAdapter() == null){
			// first time the view is created
			loadListInBackground();
			// set up footer 
			if (mHasFooter){
				setUpFooterView();
			}
		}
		
		
		//mReadArticleList = new HashSet<String>();
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);		
		
		if (mSetScrollListener){
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
					if(firstVisibleItem + visibleItemCount >= totalItemCount - 1 && totalItemCount != 0)
					{
						
						// when the visible item reaches the last item, 
						if (mflag_addData == false)
						{
							mPage += 1;
							mflag_addData = true;
							loadListInBackground();
							if (getListView().getFooterViewsCount() == 0){
								setUpFooterView();
							}
							if (mfooterview != null){
								mfooterview.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			});
		}
		
	}
	
	
	private void setUpFooterView(){		
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mfooterview = (View) inflater.inflate(R.layout.list_footer, null);
		getListView().addFooterView(mfooterview);
		getListView().setFooterDividersEnabled(true);
		// set the footer as invisible only make it visible when needed
		mfooterview.setVisibility(View.GONE);
		mfooterview.setClickable(false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (mfooterview != null){
			if (position == getListView().getCount() - 1){
				// if footer is clicked, this assumes footer exists
				return;
			}
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
		if (mfooterview!=null){
			TextView tvFooter = (TextView) mfooterview
					.findViewById(R.id.list_footer_tv_loading);
			tvFooter.setText("没有可显示的文章");
			
			ProgressBar pbFooter = (ProgressBar) mfooterview
							.findViewById(R.id.list_footer_pb_loading);
			pbFooter.setVisibility(View.INVISIBLE);
		}
		
	}
	
	/**
	 * This function displays "the end of the list" when the no more
	 * list items is available 
	 * Overwriting this function to customize actions to be taken 
	 * when the list is complete
	 */
	protected void handleEndofList(){
		if (mfooterview!=null){
			TextView tvFooter = (TextView) mfooterview
					.findViewById(R.id.list_footer_tv_loading);
			tvFooter.setText("已获取全部文章");
			
			ProgressBar pbFooter = (ProgressBar) mfooterview
							.findViewById(R.id.list_footer_pb_loading);
			pbFooter.setVisibility(View.INVISIBLE);
		}
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
					/*int errorCode = responseObject.getInt(KEY_ERR);
					if (errorCode != Constants.NO_ERROR){
						return arrayList;
					}*/
					String listObject = responseObject.getString(KEY_LIST);
					if (listObject.equalsIgnoreCase("null")){
						return arrayList;
					}
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
				Log.e(TAG, "Currently Loading URL:" + url);
				// TODO Log.d(TAG, "Please handle exception here");
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
						if (mfooterview!=null){
							mfooterview.setVisibility(View.GONE);
						}
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
			else{
				Log.d(TAG, "Need to handle null return result cases");
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
