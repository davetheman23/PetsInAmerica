package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.NotificationsDataSource;
import net.petsinamerica.askavet.utils.PiaNotification;
import net.petsinamerica.askavet.utils.PushReceiver;
import net.petsinamerica.askavet.utils.UserInfoManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.TextUtils;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;

public class HomeActivity extends FragmentActivity implements
		ActionBar.TabListener, PushReceiver.onReceiveNotificationListener {

	private static final String sTAG = "HomeActivity"; 
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	Menu menu;
	
	/** the button that shows the number of notifications */
	private Button btnNotification;
	
	private static final int sTOTAL_PAGES = 3;
	
	private static Context mContext;
	//private static String sTAG_RESULT = Constants.KEY_RESULT;
	private static AccessToken mToken;
	
	private UsersAPI mUsersAPI;
	
	private boolean mResumed = false;
	
	
	private NotificationsDataSource dataSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("HomeActivity", "OnCreate() method is called");
		
		// initialize/turn on push service 
		//PushManager.getInstance().initialize(App.appContext);
		PushManager.getInstance().turnOnPush(App.appContext);
		//boolean abc = PushManager.getInstance().isPushTurnedOn(App.appContext);
		// NOTE: it seems like initialize and turn on serve the same purpose, only one needs to be called
		// & the method isPushTurnedOn always return false;
		
		final ActionBar actionBar = setupActionBar();
		
		setContentView(R.layout.activity_home);
		
		//initialize local variables
		mContext = getApplicationContext();
		
		mToken =  AccessTokenManager.readAccessToken(mContext);
		// get userinfo
		if (!UserInfoManager.isInfoAvailable() && mToken != null 
											   && !mToken.isExpired()){
			new GetUserInfoTask().execute(Constants.URL_USERINFO 
												+ mToken.getUserId());
		}
		
		// get weibo userinfo
		// 获取当前已保存过的 Token
		Oauth2AccessToken weiboToken = AccessTokenManager.readWeiboAccessToken(this);
		if (!UserInfoManager.isWeiboInfoAvailable() && weiboToken != null 
													&& weiboToken.isSessionValid()){
			// instantiate UsersAPI to get weibo user info
			mUsersAPI = new UsersAPI(weiboToken);
			mUsersAPI.show(Long.parseLong(weiboToken.getUid()), mListener);
		}else{
			Toast.makeText(this, "微博信息获取失败,请重新登录微博", Toast.LENGTH_LONG).show();;
		}
	    // 对statusAPI实例化
	    //StatusesAPI statusesAPI = new StatusesAPI(weiboToken);

		// Set up the action bar.
		//final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		actionBar.setSelectedNavigationItem(1);
		
		/*
		 *  this option will cache the view Hierachy of the pagers, make sure
		 *  the limit covers all the pages, benefits are that the user don't 
		 *  have to reload everything everytime the pages are flipped, and the asynctask
		 *  will not conflict with fragment create and destroy events
		 */
		mViewPager.setOffscreenPageLimit(3);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		mResumed = true;
		
		if (!AccessTokenManager.isSessionValid(mContext)){
			App.inValidateSession(this);
		}
		
		// get from the SQL database on the number of notifications
		if (dataSource == null){
			dataSource = new NotificationsDataSource(this);
			PushReceiver.registerPiaNotificationListener(this);
		}
		if (btnNotification!= null && mResumed){
 			updateNotificationIcon();
 		}
		
	}
	
	

	@Override
	protected void onPause() {
		mResumed = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	/*
	 * This setup the custom action bar layout
	 * set home icon as gone essentially achieves this ab.setDisplayShowHomeEnabled(false);
	 * but due to the use of view-pager, that code will make the tabs above the actionbar
	 * this is a work-around approach
	 */
	private ActionBar setupActionBar() {
        ActionBar ab = getActionBar();
        //ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setDisplayShowCustomEnabled(true);
        
        ab.setDisplayShowHomeEnabled(true);
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_home, null);

        ab.setCustomView(v);
        
        // set the home-icon as gone, so it still there just cannot be seen
        /*View homeIcon = findViewById(android.R.id.home);
		((View)homeIcon.getParent()).setVisibility(View.GONE);*/
        
        btnNotification = (Button) v.findViewById(R.id.actionbar_home_btn_notification);
        btnNotification.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(mContext, "提醒功能还在完善中", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(HomeActivity.this, NotificationCenterActivity.class);
				startActivity(intent);
			}
		});
        
        
        
        
		
        return ab;
    }
	
	public void logMeOut(MenuItem item){
		if (item.getTitle().equals(getResources().getString(R.string.action_logout))){
			// clean up the user token
			AccessTokenManager.clearAllTokens(this);
			// clean up user info
			UserInfoManager.clearAllUserInfo();
			
			
			// turn off the pushservice
			if (PushManager.getInstance() != null ){
				PushManager.getInstance().turnOffPush(App.appContext);
				//PushManager.getInstance().stopService(App.appContext);
				//Log.d("HomeActivity", "PushService Stopped");
			}
			
			// take user back to the log-in page
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment =  null;
			switch (position){
			case 0:
				fragment = new EnquiryListFragment1();
				break;
			case 1:
				fragment = new ArticleListFragment();
				break;
			/*case 2:
				fragment = new ProductListFragment();
				break;*/
			case 2:
				fragment = new UserInfoFragment();
				break;	
			}

			return fragment;
		}

		@Override
		public int getCount() {
			// Show sTOTAL_PAGES total pages.
			return sTOTAL_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.viewpager_title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.viewpager_title_section2).toUpperCase(l);
			/*case 2:
				return getString(R.string.viewpager_title_section3).toUpperCase(l);*/
			case 2:
				return getString(R.string.viewpager_title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	public static class ArticleListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_BLOGCN, true, false, true);
			setPage(1);
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new ArticleListAdapter2(
					this.getActivity(), R.layout.list_article_header,
					R.layout.list_article_item, emptyList));
		}
		

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setStyle(Style.card);
			
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			int articleID = ((ArticleListAdapter2)this.getListAdapter()).getArticleID(v);
			
			int likeNum = ((ArticleListAdapter2)this.getListAdapter()).getLikeNum(v);
			
			int commentNum = ((ArticleListAdapter2)this.getListAdapter()).getCommentNum(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			// start a new activity 
			Intent newIntent = new Intent(this.getActivity(), ArticleActivity.class);
			newIntent.putExtra("ArticleId", articleID);
			newIntent.putExtra(Constants.KEY_ARTICLE_COMMENTS, commentNum);
			newIntent.putExtra(Constants.KEY_ARTICLE_LIKES, likeNum);
			startActivity(newIntent);
			
		}

	
	}
	
	public static class ProductListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_PRODUCTLIST, true,false,true);
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new ProductListAdapter(mContext,
					R.layout.list_large_item2, emptyList));
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			//int articleID = ((ArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			// start a new activity 
			//String articleURL_API = Constants.URL_ARTICLE_API + articleID;
			//Intent newIntent = new Intent(this.getActivity(), ArticleActivity.class);
			//newIntent.putExtra("URL_API", articleURL_API);
			//startActivity(newIntent);
			
		}
	}
	
	/**
	 * A subclass of AsyncTask to get userinfo after user verification
	 */
	public class GetUserInfoTask extends AsyncTask<String, Void, Map<String, Object>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected Map<String, Object> doInBackground(String... params) {
			String url = params[0];				
			HttpPost post = new HttpPost(url);

			if (!mToken.isExpired()){
				post = AccessTokenManager.addAccessTokenPost(post, mContext, mToken);
			}else{
				// TODO: report problem here
				Log.d(sTAG, "Token expired");
			}
			try {				
				HttpResponse response = mClient.execute(post);
				
				return GeneralHelpers.handlePiaResponse(response);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
			}finally{
				if (null != mClient)
					mClient.close();
			}
			return null;
		}


		@Override
		protected void onPostExecute(Map<String, Object> result) {
			super.onPostExecute(result);
			
			if (result != null){
				/*
				 *  Store the userinfo in a global scope
				 *  Note: cache action needs to be done at the UI thread because
				 *  it may invoke other actions that will run in background once
				 *  the userinfo is available in the UserInfoManager 
				 */
				UserInfoManager.cacheUserInfo(result);
			}
		}
		

	}
	
	/**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
    	private static final String TAG = "Request Listener";
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
                	UserInfoManager.cacheWeiboUserInfo(user, getApplication());
                } else {
                    Toast.makeText(HomeActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(HomeActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

	@Override
	public void onReceivedNotification(PiaNotification notification) {
		if (dataSource == null){
			dataSource = new NotificationsDataSource(this);
		}
 		if (btnNotification!= null && mResumed){
 			updateNotificationIcon();
 		}
		
	}
	
	/** change the number and color of the notification icon*/
	private void updateNotificationIcon(){
		Integer cnt = dataSource.getUnreadCount();
		btnNotification.setText(cnt.toString());
		if (cnt > 0){
			btnNotification.setBackgroundColor(getResources().getColor(R.color.Red));
			btnNotification.setTextColor(getResources().getColor(R.color.White));
		}else{
			btnNotification.setBackgroundColor(getResources().getColor(R.color.White));
			btnNotification.setTextColor(getResources().getColor(R.color.Black));
		}
		
		
		
	}

}
