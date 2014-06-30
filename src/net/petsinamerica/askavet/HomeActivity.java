package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.utils.UserInfoManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
		ActionBar.TabListener {

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
	
	private static final int sTOTAL_PAGES = 4;
	
	private static Context mContext;
	private static String sTAG_RESULT;
	private static String sTAG_LIST;
	private static AccessToken mToken;
	
	private UsersAPI mUsersAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// initialize push service 
		PushManager.getInstance().initialize(this.getApplicationContext());
		
		setContentView(R.layout.activity_home);
		
		//initialize local variables
		mContext = getApplicationContext();
		sTAG_RESULT = getResources().getString(R.string.JSON_tag_result);
		sTAG_LIST = getResources().getString(R.string.JSON_tag_list);
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
			Toast.makeText(this, "微博信息获取失败,请重新登录微博", Toast.LENGTH_LONG);
		}
	    // 对statusAPI实例化
	    //StatusesAPI statusesAPI = new StatusesAPI(weiboToken);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
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
	protected void onDestroy() {
		super.onDestroy();
		
	}

	public void logMeOut(MenuItem item){
		if (item.getTitle().equals(getResources().getString(R.string.action_logout))){
			AccessTokenManager.clear(this);
			AccessTokenManager.clearWeiboToken(this);
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
			case 2:
				fragment = new ProductListFragment();
				break;
			case 3:
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
			case 2:
				return getString(R.string.viewpager_title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.viewpager_title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * 
	 */
	public static class ArticleListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_BLOGCN, sTAG_LIST,true);
		}

		@Override
		protected void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray) {
			getListView().setDivider(null);
			getListView().setDividerHeight(10);
			getListView().setCacheColorHint(android.R.color.transparent);
			getListView().setBackgroundColor(mContext.getResources().getColor(R.color.WhiteSmoke));
			getListView().setHeaderDividersEnabled(true);
			getListView().addHeaderView(new View(getActivity()));
			setCustomAdapter(new ArticleListAdapter2(
					this.getActivity(), R.layout.article_list_header,
					R.layout.article_list_item, resultArray));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			int articleID = ((ArticleListAdapter2)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			// start a new activity 
			Intent newIntent = new Intent(this.getActivity(), ArticleActivity.class);
			newIntent.putExtra("ArticleId", articleID);
			startActivity(newIntent);
			
		}
	
	}
	
	public static class ProductListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_PRODUCTLIST, sTAG_LIST,true);
		}

		@Override
		protected void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray) {
			setCustomAdapter(new ProductListAdapter(mContext,
					R.layout.list_large_item2, resultArray));
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
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();
				Map<String, Object> resultMap = JsonHelper
											.toMap(responseObject.getJSONObject(sTAG_RESULT));
				return resultMap;
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

}
