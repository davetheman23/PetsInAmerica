package net.petsinamerica.askavet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
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
	

	private WebView mWebView;
	private TextView mTitleTextView;
	private TextView mSubTitleTextView;
	private ProgressBar mProgBarView;
	private ImageView mWeiboShareIcon;
	private ImageView mWeixinShareIcon;
	private ImageView mFacebookShareIcon;
	
	private static final String HTML_CONTENT = "Html_Content";
	private static final String HTML_TITLE = "Html_Title";
	private static final String HTML_SNAPSHOT_URL = "Html_SnapShot_Url";
	private static final String HTML_SUB_TITLE = "Html_SubTitle";

	private Uri mShareText = null;
	private Uri mShareImage = null;
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	private static String TAG_IMAGE;
	private static String TAG_SNAPSHOT;
	private static String TAG_CONTENT;
	private static String KEY_AUTHOR;
	private static String KEY_TIME;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article);
		
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
		
		TAG_TITLE = getResources().getString(R.string.JSON_tag_title);
		TAG_SNAPSHOT = getResources().getString(R.string.JSON_tag_snapshot);
		TAG_IMAGE = getResources().getString(R.string.JSON_tag_image);
		TAG_CONTENT = getResources().getString(R.string.JSON_tag_content);		
		KEY_AUTHOR = getResources().getString(R.string.JSON_tag_owner);
		KEY_TIME = getResources().getString(R.string.JSON_tag_time);
		
		String articleURL_API = getIntent().getStringExtra("URL_API");
		
		new HttpGetTask().execute(articleURL_API);
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
	
	
	
	
	private class HttpGetTask extends AsyncTask<String, Void, Map<String, String>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mProgBarView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Map<String, String> doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(request);
				JSONResponse = new BasicResponseHandler().handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				String sTitle = responseObject.get(TAG_TITLE).toString();
				String imageURL = responseObject.get(TAG_IMAGE).toString();
				String snapshotURL = responseObject.get(TAG_SNAPSHOT).toString();
				String sContent = responseObject.get(TAG_CONTENT).toString();
				String author = responseObject.get(KEY_AUTHOR).toString();
				String time = responseObject.get(KEY_TIME).toString();
				
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
