package net.petsinamerica.askavet.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.AttributeSet;
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


/**
 * This abstract listfragment class is intended to be used to call PIA api and 
 * show the results in a listview, with a standard footerview added already for lazyloading.
 * Simply extending it and supply basic parameters using {@link #setParameters} method. <p>
 * This listfragment inflates a R.layout.fragment_standard_list layout. This will prevent the list
 * fragment from showing a footerview when the list is empty, if that's not desirable, then
 * shall not use this list fragment. 
 * 
 * @author David
 *
 */
public abstract class BaseListFragment extends ListFragment implements OnRefreshListener{
//public abstract class BaseListFragment extends ListFragment{
	
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
	
	private ProgressBar mOverallProgBar = null;
	
	private TextView mOverallEmptyListView = null;
	
	private HttpPostTask httpPostTask = null;
	
	//private static final String TAG = "BaseListFragment";
	
	private SwipeRefreshLayout mSwipeRefreshLayout;

	
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
	private List<Map<String, Object>> mData = new ArrayList<Map<String,Object>>();
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
	 * This method will cancel the asynctask enclosed in this listfragment. It should be called in 
	 * the parent activity when the parent activity is destroyed or no longer visible
	 */
	public void cancelOngoingTask(){
		if (httpPostTask != null){
			httpPostTask.cancel(true);
		}
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
		String url = mUrl;
		if (mflag_page){
			// fetch list data from the network
			url = mUrl + Integer.toString(mPage);
		}else if (mflag_objectId){
			// fetch object data from the network
			url = mUrl + Integer.toString(mPage);
		}
		httpPostTask = (HttpPostTask) new HttpPostTask()
			.setParameters(getActivity(), CallPiaApiInBackground.TYPE_RETURN_LIST, mIsUserSpecific)
			.execute(url);
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.fragment_standard_list_w_refresh, null, false);
		mOverallProgBar = (ProgressBar) rootview.findViewById(android.R.id.progress);
		mOverallEmptyListView = (TextView) rootview.findViewById(android.R.id.empty);
		
		mSwipeRefreshLayout = (SwipeRefreshLayout) rootview.findViewById(R.id.swipe_container);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mSwipeRefreshLayout.setColorScheme(R.color.Red, R.color.Blue, R.color.White, R.color.Green);
		return rootview;
	}
	
	@Override 
	public void onRefresh() {
		if (httpPostTask != null){
			if (httpPostTask.isIdle()){
				mSwipeRefreshLayout.setRefreshing(true);
				// if the thread is in idle state, 
				loadListInBackground();
			}else{
				// if the thread is getting list in the background
				GeneralHelpers.showAlertDialog(getActivity(), "请稍等", "正在和服务器在通信中");
			}
		}
		
		
	
		
    }
	
	
	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getListAdapter() == null){
			// set up footer 
			if (mHasFooter){
				setUpFooterView();
			}
			// first time the view is created
			setListAdapter(mCustomAdapter);
			loadListInBackground();
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
						if (mHasFooter && getListView().getFooterViewsCount() == 0){
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
	public void onDestroyView() {
		cancelOngoingTask();
		super.onDestroyView();
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
			tvFooter.setText(getResources().getString(R.string.no_content_available));
			
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
	
	
	private class HttpPostTask extends CallPiaApiInBackground{
		@Override
		protected void onPreExecute() {
			if (mOverallProgBar != null && getListView().getCount() <= 1){
				mOverallProgBar.setVisibility(View.VISIBLE);
			}
			if (mOverallEmptyListView != null){
				mOverallEmptyListView.setText("");
			}
			super.onPreExecute();
		}

		@Override
		protected void onCallCompleted(Map<String, Object> result) {}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
			// allow subclasses to call this method
			onHttpDone(result);
			
			if (mOverallProgBar != null){
				mOverallProgBar.setVisibility(View.GONE);
			}
			if (mOverallEmptyListView != null){
				mOverallEmptyListView.setText(getResources().getString(R.string.no_content_available));
			}
			
			if (result != null){
				if (!result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
					// if no error
					if (mfooterview!=null){
						mfooterview.setVisibility(View.GONE);
					}
					if (result.size()>0){
						if (mSwipeRefreshLayout.isRefreshing()){
							// if refreshing is from top, then need to get only new feeds
							// and add that on top of the list
							int i = 0;
							for (Map<String, Object> r : result){
								if (!mData.contains(r)){
									mData.add(r);
									mCustomAdapter.insert(r, i);
									i++;
								}else{
									// the assumption is that once the first item is the 
									// same as the ones stored in mData, then all those 
									// following will be also the same
									break;
								}
							}
						}else{
							// if refreshing is from bottom, then all needs to be added below
							mData.addAll(result);
							mCustomAdapter.addAll(result);
						}
						//after all update operations, notify the dataset changed
						mCustomAdapter.notifyDataSetChanged();
					}else{
						// no more list items to be displayed and handle it
						if (getListView().getCount() < 1){
							handleEmptyList();;
						}else{
							handleEndofList();
						}
					}
				}else{
					// if error 
					String errorMsg = result.get(0).get(Constants.KEY_ERROR_MESSAGE).toString();
					GeneralHelpers.showAlertDialog(getActivity(), null, errorMsg);
				}
			}
			
			// stop the refreshing animation
			if (mSwipeRefreshLayout != null){
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}

		@Override
		protected void onCallCompleted(Integer result) {}
		
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
