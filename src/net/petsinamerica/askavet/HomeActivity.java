package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.LoginActivity.AlertDialogFragment;
import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.utils.PiaApplication;
import net.petsinamerica.askavet.utils.UserInfoManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.Picasso;

public class HomeActivity extends FragmentActivity implements
		ActionBar.TabListener {

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
	
	private static final int sTOTAL_PAGES = 4;
	
	private Context mContext;
	private static String sTAG_RESULT;
	private AccessToken mToken;
	
	private UsersAPI mUsersAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//initialize local variables
		mContext = getApplicationContext();
		sTAG_RESULT = getResources().getString(R.string.JSON_tag_result);
		mToken =  AccessTokenManager.readAccessToken(mContext);
		// get userinfo
		if (!UserInfoManager.isInfoAvailable() && mToken != null 
											   && !mToken.isExpired()){
			String url = PiaApplication.URL_USERINFO + "/" + mToken.getUserId();
			new GetUserInfoTask().execute(url);
		}
		
		// get weibo userinfo
		// 获取当前已保存过的 Token
		Oauth2AccessToken weiboToken = AccessTokenManager.readWeiboAccessToken(this);
		if (!UserInfoManager.isWeiboInfoAvailable() && weiboToken != null 
													&& weiboToken.isSessionValid()){
			// instantiate UsersAPI to get weibo user info
			mUsersAPI = new UsersAPI(weiboToken);
			mUsersAPI.show(Long.parseLong(weiboToken.getUid()), mListener);
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
		
		// this option will cache the view Hierachy of the pagers
		mViewPager.setOffscreenPageLimit(2);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
				fragment = (Fragment) new EnquiryListFragment();
				break;
			case 1:
				fragment = (Fragment) new ArticleListFragment();
				break;
			case 2:
				fragment = (Fragment) new PetListFragment();
				break;
			default:
				fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
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
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return "我的宠物";
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_home,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(android.R.id.empty);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
	/**
	 * A subclass of AsyncTask to get userinfo after user verification
	 */
	public class GetUserInfoTask extends AsyncTask<String, Void, Integer> {
		
		private static final int sSUCCEED = 0;
		private static final int sFAIL = 1;

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		@Override
		protected void onPreExecute() {
			// TODO show a dialog box with a loading icon
			super.onPreExecute();
		}

		
		@Override
		protected Integer doInBackground(String... params) {
			String url = params[0];			
			
			HttpPost post = new HttpPost(url);
			
			if (!mToken.isExpired()){
				post = AccessTokenManager.addAccessTokenPost(post, mContext, mToken);
			}else{
				// TODO: report problem here
				
			}
			
			try {				
				HttpResponse response = mClient.execute(post);
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();
				
				Map<String, Object> resultMap = JsonHelper
											.toMap(responseObject.getJSONObject(sTAG_RESULT));

				// store the userinfo in a global scope
				UserInfoManager.cacheUserInfo(resultMap);
				
				return sSUCCEED;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
			}
			return sFAIL;
		}

		@Override
		protected void onPostExecute(Integer loginresult) {
			if (null != mClient)
				mClient.close();
			
			DialogFragment df;
			switch (loginresult){
			case sSUCCEED:
				
				df = AlertDialogFragment.newInstance();
				df.show(getFragmentManager(), "Successed");
				break;
			case sFAIL:
				df = AlertDialogFragment.newInstance();
				df.show(getFragmentManager(), "Failed");
				break;
				// == do something 
			}
			
		}

		@Override
		protected void onCancelled() {
			// TODO develop handler for cancel event if necessary
			super.onCancelled();
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
                	UserInfoManager.cacheWeiboUserInfo(user, getApplicationContext());
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
