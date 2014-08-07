package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
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
	// variables that can be used by subclasses
	protected View mfooterview = null; 
	
	private static final String TAG = "BaseListFragment";

	
	/**
	 * mPage will be appended to the URL, for some API calls, it indicates 
	 * the page of the list, for other API calls, it could mean userid or other object id.
	 * Page id can be incremented, while object id can not.   
	 */
	private int mPage = 0;
	private boolean mflag_page = false;		// flag whether a page should be added to url
	private boolean mflag_objectId = false;		// flag whether an object id should be added to url
	private boolean mIsUserSpecific = false;	// flag for user specific data
	private boolean mHasFooter = true;			// flag to indicate if footer is needed
	
	private ArrayAdapter<Map<String, Object>> mCustomAdapter;
	private Context mContext;
	private String mUrl;
	
	/**
	 * This method handles the actions to be taken when one item within the list is clicked
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

	//Set<String> mReadArticleList = null;		// the article list that has been read by the user
	//SharedPreferences mUsageData;
		
	public void setParameters(String url,  boolean hasPageId, boolean hasObjectId, boolean hasfooter) {
		mUrl = url;
		mflag_page = hasPageId;
		mflag_objectId = hasObjectId;
		mHasFooter = hasfooter;
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
	
	public void loadListInBackground(){
		if (!mflag_page && !mflag_objectId){
			new HttpPostTask().execute(mUrl);
		}else if (mflag_page){
			// fetch list data from the network
			new HttpPostTask().execute(mUrl + Integer.toString(mPage));
		}else if (mflag_objectId){
			// fetch object data from the network
			new HttpPostTask().execute(mUrl + Integer.toString(mPage));
		}
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
	}
	
	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getListAdapter() == null){
			// first time the view is created
			setListAdapter(mCustomAdapter);
			loadListInBackground();
			// set up footer 
			if (mHasFooter){
				setUpFooterView();
			}
		}
		
		//mReadArticleList = new HashSet<String>();
		
		if (mflag_page){
			// disable scroll bar
			getListView().setVerticalScrollBarEnabled(false);		
			
			// monitor scroll activity, add more articles when scroll close to bottom
			getListView().setOnScrollListener(new OnScrollListener() {
				int currentFirstVisibleItem, currentVisibleItemCount, currentTotalItemCount;
				/*
				 * this listener is used to continuously load more article when scroll down to bottom
				 */
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (currentFirstVisibleItem + currentVisibleItemCount > currentTotalItemCount - 1
						&& currentTotalItemCount != 0
						&& scrollState == SCROLL_STATE_IDLE){
						mPage += 1;
						loadListInBackground();
						if (getListView().getFooterViewsCount() == 0){
							setUpFooterView();
						}
						if (mfooterview != null){
							mfooterview.setVisibility(View.VISIBLE);
						}
					}
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					this.currentFirstVisibleItem = firstVisibleItem;
					this.currentVisibleItemCount = visibleItemCount;
					this.currentTotalItemCount = totalItemCount;
					
				}
			});
		}
		
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
	 * if the list has a footer view, then a default footer view is setup, but 
	 * this can be overwritten using a new layout file. By doing so, {@link #handleEmptyList()} 
	 * and {@link #handleEndofList()} needs to be overwritten to use the new elements in the 
	 * new layout file
	 */
	protected void setUpFooterView(){		
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mfooterview = (View) inflater.inflate(R.layout.list_footer, null);
		getListView().addFooterView(mfooterview);
		getListView().setFooterDividersEnabled(true);
		// set the footer as invisible only make it visible when needed
		mfooterview.setVisibility(View.GONE);
		mfooterview.setClickable(false);
	}
	

	/**
	 * This function displays no content available when the list is empty,
	 * Overwriting this function to customize actions to be taken 
	 * when the list is empty 
	 */
	protected void handleEmptyList(){
		if (mfooterview!=null){
			TextView tvFooter = (TextView) mfooterview
					.findViewById(R.id.list_footer_tv_loading);
			tvFooter.setText("没有可显示的内容");
			
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
			tvFooter.setText("已获取全部内容");
			
			ProgressBar pbFooter = (ProgressBar) mfooterview
							.findViewById(R.id.list_footer_pb_loading);
			pbFooter.setVisibility(View.INVISIBLE);
		}
	}
	
	
	/**
	 * This method simply provide a reference of a custom adapter instance to a private variable,
	 * so later on the adapter can be modified by using the reference
	 */
	protected void setCustomAdapter(ArrayAdapter<Map<String, Object>> customAdapter){
		mCustomAdapter = customAdapter;
	}
	
	/**
	 *  this is a method with out implementation, it is called directly after the http
	 *  onPostExecute() method, should overwrite if data is needed 
	 */
	protected void onHttpDone(List<Map<String, Object>> resultArray){}
	

	private class HttpPostTask extends AsyncTask<String, Void, List<Map<String, Object>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected List<Map<String, Object>> doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			if (mIsUserSpecific){
				AccessTokenManager.addAccessTokenPost(post, mContext);
			}
			
			try {
				HttpResponse response = mClient.execute(post);
				return GeneralHelpers.handlePiaResponseArray(response);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "JSONException");
				Log.e(TAG, "Currently Loading URL:" + url);
			}finally{
				if (null != mClient){
					mClient.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> resultArray) {
			onHttpDone(resultArray);
			if (isAdded() && resultArray != null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				if (resultArray.size() > 0 ){
					if (getListAdapter() != null){
						mCustomAdapter.addAll(resultArray);
					}else{
						// it is not normal resultArray.size() >0 but mflag_addData is true
						Log.e(TAG, "ListAdapter is not set properly, plese check!");
					}
					if (mfooterview!=null){
						mfooterview.setVisibility(View.GONE);
					}
					
				}else{
					// no more list items to be displayed and handle it
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
	
	/**
	 *  function to fetch the attribute set defined in layoutResource element ViewName
	 */
	private AttributeSet getAttributeSet(Context context, int layoutResource, String ViewName){
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
