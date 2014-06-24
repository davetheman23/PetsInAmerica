package net.petsinamerica.askavet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
	
	private static final String HTML_CONTENT = "Html_Content";
	private static final String HTML_TITLE = "Html_Title";
	private static final String HTML_SNAPSHOT_URL = "Html_SnapShot_Url";
	private static final String HTML_SUB_TITLE = "Html_SubTitle";

	
	// these tags are those for reading the JSON objects
	private static String KEY_TITLE;
	private static String KEY_IMAGE;
	private static String KEY_SNAPSHOT;
	private static String KEY_CONTENT;
	private static String KEY_AUTHOR;
	private static String KEY_TIME;
	private static String KEY_ARTICLE_LIKES = "like_num";
	private static String KEY_ERROR;
	private static String KEY_RESULT;
	
	private int articleId; 
	private Uri mShareText = null;
	private Uri mShareImage = null;
	private boolean isClicked = false; 	/* true if article like is clicked */
	
	private WebView mWebView;
	private TextView mTitleTextView;
	private TextView mSubTitleTextView;
	private ProgressBar mProgBarView;
	private ImageView mWeiboShareIcon;
	private ImageView mWeixinShareIcon;
	private ImageView mFacebookShareIcon;
	private Menu mMenu;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get the json keys from the string resource
		KEY_TITLE = getResources().getString(R.string.JSON_tag_title);
		KEY_SNAPSHOT = getResources().getString(R.string.JSON_tag_snapshot);
		KEY_IMAGE = getResources().getString(R.string.JSON_tag_image);
		KEY_CONTENT = getResources().getString(R.string.JSON_tag_content);		
		KEY_AUTHOR = getResources().getString(R.string.JSON_tag_owner);
		KEY_TIME = getResources().getString(R.string.JSON_tag_time);
		KEY_ERROR = getResources().getString(R.string.JSON_tag_error);
		KEY_RESULT = getResources().getString(R.string.JSON_tag_result);
		
		// inflate the layouts
		setContentView(R.layout.activity_article);
		
		// get references to each layout views
		mTitleTextView = (TextView) findViewById(R.id.article_activity_title);
		mSubTitleTextView = (TextView) findViewById(R.id.article_activity_author_date);
		mProgBarView = (ProgressBar) findViewById(R.id.article_activity_load_progressbar);
		mProgBarView.setVisibility(View.VISIBLE);
		
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
		
		
		mWebView = (WebView) findViewById(R.id.article_activity_web_view);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);

		// get article id from the extra that was set when the activity was started
		articleId = getIntent().getIntExtra("ArticleId", 0);
		
		if (articleId != 0){
			String articleURL_API = Constants.URL_ARTICLE_API + Integer.toString(articleId);
			new GetArticleInBackground().execute(articleURL_API);
		}else{
			//TODO notify the user
		}
	}
	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.article_activity_menu, menu);
		mMenu = menu;
		return true;
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
				Toast.makeText(getApplicationContext(), 
							   "Try again after webpage is fully loaded", 
							   Toast.LENGTH_LONG)
							   .show();
			}else{
				Intent intent = GeneralHelpers.shareByApp(appName, shareTextUri, mShareImage);
				startActivity(intent);
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
				int error = responseObject.getInt(KEY_ERROR);
				switch (error){
					case 0:
						return responseObject.getInt(KEY_RESULT);
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
				if (mMenu != null){
					MenuItem item = mMenu.findItem(R.id.action_like);
					String likeString = getResources().getString(R.string.action_like);
					item.setTitle(likeString + " " + result);
				}
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

	private class GetArticleInBackground extends AsyncTask<String, Void, Map<String, String>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgBarView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Map<String, String> doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(post);
				JSONResponse = new BasicResponseHandler().handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				String sTitle = responseObject.get(KEY_TITLE).toString();
				String imageURL = responseObject.get(KEY_IMAGE).toString();
				String snapshotURL = responseObject.get(KEY_SNAPSHOT).toString();
				String sContent = responseObject.get(KEY_CONTENT).toString();
				String author = responseObject.get(KEY_AUTHOR).toString();
				String time = responseObject.get(KEY_TIME).toString();
				String like_nums = responseObject.get(KEY_ARTICLE_LIKES).toString();
				
				String format = "MMM-dd, yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
				String subTitle = "作者：" + author + " 发表于  " + 
								sdf.format(new Date(Long.parseLong(time))); 
				
				String html_string = null;
				if (imageURL != null && sContent != null){
					html_string = "<body>" + "<img src=\"" + imageURL + "\">" + sContent + "</body>";
					String pattern = "(<img.*?[jp][pn]g.*?\")(.?)(>)";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
					html_string = html_string.replaceAll(pattern, "$1 width=\"100%\" alt=\"\"$3");
				}
				
				// -- put the data together and display on the UI 
				Map<String, String> results = new HashMap<String, String>();
				results.put(HTML_TITLE, sTitle);
				results.put(HTML_CONTENT, html_string);
				results.put(HTML_SNAPSHOT_URL, snapshotURL);
				results.put(HTML_SUB_TITLE, subTitle);
				results.put(KEY_ARTICLE_LIKES, like_nums);
				return results;
			} catch (HttpResponseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}			
			return null;
		}

		@Override
		protected void onPostExecute(Map<String, String> results) {
			if (null != mClient)
				mClient.close();
			// display title and subtitle (author, date)
			String noContent = getResources().getString(R.string.no_content_available);
			String sTitle = results.get(HTML_TITLE); 
			if (sTitle != null){
				mTitleTextView.setText(sTitle);
			}else{
				mTitleTextView.setText(noContent);
			}
			String subTitle = results.get(HTML_SUB_TITLE);
			mSubTitleTextView.setText(subTitle);
			
			// display html content in a webview
			String html_string = results.get(HTML_CONTENT);
			if (html_string == null){
				html_string = "<body>" + noContent + "</body>";
			}
			mWebView.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
			
			/* set up a background task to load the snapshot url into the target
			 	in case user will share it, so it can be ready after user read */
			String snapShotUrl = results.get(HTML_SNAPSHOT_URL);
			Picasso.with(getApplication())
					.load(Uri.parse(snapShotUrl))
					.into(target);
			
			int like_nums = Integer.parseInt(results.get(KEY_ARTICLE_LIKES));
			if (mMenu != null){
				MenuItem item = mMenu.findItem(R.id.action_like);
				String likeString = getResources().getString(R.string.action_like);
				item.setTitle(likeString + " " + like_nums);
			}
			
			mProgBarView.setVisibility(View.GONE);
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
							GeneralHelpers.MEDIA_TYPE_IMAGE);					
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
