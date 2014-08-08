package net.petsinamerica.askavet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

/*
 *  Need to combine this with the Article List activity,
 *  and make this just a fragment
 */

public class ArticleActivity extends Activity {
	
	private static final int SHARE_TO_WEIBO = 1;
	private static final int SHARE_TO_WEIXIN = 2;
	private static final int SHARE_TO_TWITTER = 3;
	private static final int SHARE_TO_FACEBOOK = 4;
	
	/*private static final String HTML_CONTENT = "Html_Content";
	private static final String HTML_TITLE = "Html_Title";
	private static final String HTML_SNAPSHOT_URL = "Html_SnapShot_Url";
	private static final String HTML_SUB_TITLE = "Html_SubTitle";*/

	
	// these tags are those for reading the JSON objects
	private static Resources res = App.appContext.getResources();
	private static final String likeString = res.getString(R.string.action_like);
	
	private int articleId; 
	private Uri mShareText = null;
	private Uri mShareImage = null;
	private String mShareSnapshotUrl = null;
	private boolean isClicked = false; 	/* true if article like is clicked */
	
	private WebView mWebView;
	private TextView mTitleTextView;
	private TextView mSubTitleTextView;
	private ProgressBar mProgBarView;
	private Button mShareButton;
	private Button mCommentButton;
	private Button mLikeButton;
	private ImageView mWeiboShareIcon;
	private ImageView mWeixinShareIcon;
	private ImageView mFacebookShareIcon;
	private ImageView mShareMoreIcon;
	private Menu mMenu;
	
	private SlidingUpPanelLayout mSlideUpPanelLayout;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// inflate the layouts
		setContentView(R.layout.activity_article);
		
		// get a screen height
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		
        mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mSlideUpPanelLayout.setOverlayed(false);
		mSlideUpPanelLayout.setMaxSlideRange((int)(height * 0.3));
		
		View mainLayout = findViewById(R.id.article_activity_main_view);
		mainLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSlideUpPanelLayout != null && mSlideUpPanelLayout.isPanelExpanded()){
					mSlideUpPanelLayout.collapsePanel();
				}
			}
		});
		
		
		// get references to each layout views
		mTitleTextView = (TextView) findViewById(R.id.article_activity_title);
		mSubTitleTextView = (TextView) findViewById(R.id.article_activity_author_date);
		mProgBarView = (ProgressBar) findViewById(R.id.article_activity_load_progressbar);
		mProgBarView.setVisibility(View.VISIBLE);
		
		
		mShareButton = (Button) findViewById(R.id.article_activity_btn_share);
		mShareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSlideUpPanelLayout.expandPanel();
			}
		});
		mCommentButton = (Button) findViewById(R.id.article_activity_btn_comment);
		mCommentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessage("评论功能还在完善中！");
			}
		});
		mLikeButton = (Button) findViewById(R.id.article_activity_btn_like);
		mLikeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendLikeInBackground().execute(Constants.URL_ARTICLE_LIKES 
						+ Integer.toString(articleId));
			}
		});
		
		// set up the weibo share icon, share with text and image
		mWeiboShareIcon = (ImageView) findViewById(R.id.article_activity_weibo_share);
		mWeiboShareIcon.setOnClickListener(new ShareIconClickListener("weibo"));
		mWeiboShareIcon.setTag(SHARE_TO_WEIBO);
		
		// set up the weixin share icon, share with text and image
		mWeixinShareIcon = (ImageView) findViewById(R.id.article_activity_weixin_share);
		mWeixinShareIcon.setOnClickListener(new ShareIconClickListener("tencent.mm"));
		mWeixinShareIcon.setTag(SHARE_TO_WEIXIN);
		
		// set up the weixin share icon, share with text and image
		mFacebookShareIcon = (ImageView) findViewById(R.id.article_activity_facebook_share);
		mFacebookShareIcon.setOnClickListener(new ShareIconClickListener("facebook"));
		mFacebookShareIcon.setTag(SHARE_TO_FACEBOOK);
		
		// set up the weixin share icon, share with text and image
		mShareMoreIcon = (ImageView) findViewById(R.id.article_activity_more_share);
		mShareMoreIcon.setOnClickListener(new ShareIconClickListener(null));
		mShareMoreIcon.setTag(999);
		

		mWebView = (WebView) findViewById(R.id.article_activity_web_view);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setSupportZoom(false);

		// get article id from the extra that was set when the activity was started
		articleId = getIntent().getIntExtra("ArticleId", 0);
		//articleId = 397;
		if (articleId != 0){
			String articleURL_API = Constants.URL_ARTICLE_API + Integer.toString(articleId);
			new GetArticleInBackground2().execute(articleURL_API);
		}else{
			//TODO notify the user
		}
	}
	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.article_activity_menu2, menu);
		mMenu = menu;
		return true;
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


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_like:
				if (!isClicked){
					new SendLikeInBackground().execute(Constants.URL_ARTICLE_LIKES 
													+ Integer.toString(articleId));
					isClicked = true;
				}else{
					showMessage("你已经点过赞了，谢谢你的支持");
				}
				return true;
			//case R.id.action_share:
				//return true;
			case R.id.action_logout:
				
				return true;
			default:
				return super.onOptionsItemSelected(item);			
		}		
	}
	
	
	
	@Override
	public void onBackPressed() {
		if (mSlideUpPanelLayout != null && mSlideUpPanelLayout.isPanelExpanded()){
			mSlideUpPanelLayout.collapsePanel();
			return;
		}else{
			super.onBackPressed();
		}
	}



	class ShareIconClickListener implements View.OnClickListener{
		private String appName = "";
		private Uri shareTextUri = null;
		
		public ShareIconClickListener(String namePart){
			appName = namePart;
		}
		@Override
		public void onClick(View v) {
			
			// 
			switch (Integer.parseInt(v.getTag().toString())){
			case SHARE_TO_WEIBO:
				shareTextUri = Uri.parse("@北美宠物网");
				break;
			default:
				shareTextUri = Uri.parse("--来自于 北美宠物网");
			}
			if (mShareImage == null){
				showMessage("页面还未完全读取完成，请稍后再试");
			}else{
				Intent intent = GeneralHelpers.shareByApp(appName, shareTextUri, mShareImage);
				startActivity(intent);
			}
			
			// close up the share panel
			if (mSlideUpPanelLayout != null){
				mSlideUpPanelLayout.collapsePanel();
			}
		}
	}
	
	private class SendLikeInBackground extends AsyncTask<String, Void, Integer>{
		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected Integer doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			
			post = AccessTokenManager.addAccessTokenPost(post, getApplicationContext());
			JSONObject responseObject = null;
			try {
				HttpResponse response = mClient.execute(post);
				String JSONResponse = new BasicResponseHandler().handleResponse(response);
				
				// -- Parse Json object, 
				responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				int error = responseObject.getInt(Constants.KEY_ERROR);
				switch (error){
					case 0:
						return responseObject.getInt(Constants.KEY_RESULT);
					default:
						return -error;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return 0;
		}

		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (mClient != null){
				mClient.close();
			}
			/*
			 * if results is negative, then its absolute value is the error code
			 * else, it is the number of likes for the article
			 */
			if (result >= 0){
				mLikeButton.setText(likeString + " " + result);
			}else{
				result = -result;
				if (result == 13){
					showMessage("你已经点过赞了，谢谢你的支持");
				}else{
					showMessage("unknown error, possibly network connection failure");
				}
			}
		}
	}
	
	private void showMessage(String message){
		Toast.makeText(getApplication(), 
				   message, 
				   Toast.LENGTH_LONG)
				   .show();
	}
	
	private class GetArticleInBackground2 extends GeneralHelpers.CallPiaApiInBackground{

		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			if (result != null){
				String imageURL = result.get(Constants.KEY_IMAGE).toString();
				if (imageURL.isEmpty()){
					imageURL = result.get("summary_img").toString();
				}
				String snapshotURL = "";
				if (result.get(Constants.KEY_SNAPSHOT) != null){
					snapshotURL = result.get(Constants.KEY_SNAPSHOT).toString();
				}
				String sContent = result.get(Constants.KEY_CONTENT).toString();
				String author = result.get(Constants.KEY_AUTHOR).toString();
				String time = result.get(Constants.KEY_TIME).toString();
				String like_nums = result.get(Constants.KEY_ARTICLE_LIKES).toString();
				
				String format = "MMM-dd, yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
				String subTitle = "作者：" + author + " 发表于  " + 
								sdf.format(new Date(Long.parseLong(time))); 
				
				// display title and subtitle (author, date)
				String noContent = getResources().getString(R.string.no_content_available);
				String sTitle = result.get(Constants.KEY_TITLE).toString();
				if (sTitle != null){
					mTitleTextView.setText(Html.fromHtml(sTitle.trim()));
				}else{
					mTitleTextView.setText(noContent);
				}
				mSubTitleTextView.setText(Html.fromHtml(subTitle));
				// display html content in a webview
				String html_string = null;
				if (imageURL != null && sContent != null){
					//sContent = "<img src=\"" + imageURL + "\">" + sContent;
					// adding a parameters to allow the pictures to fit in the screen. 
					String pattern1 = "(<img src=\".*?\")(.*?)(>)";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
					sContent = sContent.replaceAll(pattern1, "$1 width=\"100%\" alt=\"\"$3");
					// delete the reference section, which is too long and cannot be wrapped, if not deleted, the webview will try to fit it. 
					String pattern2 = "<p>Reference.+</p>"; 
					sContent = sContent.replaceAll(pattern2, "");
					html_string = "<body>" + sContent + "</body>";
				}else{
					html_string = "<body>" + noContent + "</body>";
				}
				mWebView.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
				
				/* set up a background task to load the snapshot url into the target
			 	in case user will share it, so it can be ready after user read */			
				mShareSnapshotUrl = snapshotURL;
				Picasso.with(getApplication())
					.load(Uri.parse(mShareSnapshotUrl))
					.into(target);
				
				/*if (mMenu != null){
					MenuItem item = mMenu.findItem(R.id.action_like);
					String likeString = getResources().getString(R.string.action_like);
					item.setTitle(likeString + " " + like_nums);
				}*/
				
				mLikeButton.setText(likeString + " " + like_nums);
				
				mProgBarView.setVisibility(View.GONE);
			}
		}

		@Override
		protected void addParamstoPost(HttpPost post, Context context) {
		}
		
	}
	
	/*
	 * define a private variable of the class Target to be used for Picasso
	 */
	private Target target = new Target() {
		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
		}
		
		@Override
		public void onBitmapLoaded(final Bitmap bitmap, LoadedFrom from) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					File tmpFile = GeneralHelpers.getOutputMediaFile(
							GeneralHelpers.MEDIA_TYPE_IMAGE, true);					
					try 
					{
						tmpFile.createNewFile();
						FileOutputStream ostream = new FileOutputStream(tmpFile);
						bitmap.compress(CompressFormat.JPEG, 75, ostream);
						ostream.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					mShareImage = Uri.fromFile(tmpFile);
					
				}
			}).start();

		}
		
		@Override
		public void onBitmapFailed(Drawable errorDrawable) {

		}
	};
	
	

}
