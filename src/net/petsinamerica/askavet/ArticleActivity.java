package net.petsinamerica.askavet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*
 *  Need to combine this with the Article List activity,
 *  and make this just a fragment
 */

public class ArticleActivity extends Activity {

	private WebView mWebView;
	private TextView mTitleTextView;
	private ProgressBar mProgBarView;
	private ImageView mWeiboShareIcon;
	
	private static final String HTML_CONTENT = "Html_Content";
	private static final String HTML_TITLE = "Html_Title";
	private static final String HTML_SNAPSHOT_URL = "Html_SnapShot_Url";
	
	private String mSnapShotUrl = null; 
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	private static String TAG_IMAGE;
	private static String TAG_SNAPSHOT;
	private static String TAG_CONTENT;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article);
		
		mTitleTextView = (TextView) findViewById(R.id.article_activity_title);
		//mImageView = (ImageView) findViewById(R.id.article_image);
		mProgBarView = (ProgressBar) findViewById(R.id.article_activity_load_progressbar);
		mProgBarView.setVisibility(View.VISIBLE);
		
		mWeiboShareIcon = (ImageView) findViewById(R.id.article_activity_weibo_share);
		mWeiboShareIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSnapShotUrl == null){
					Toast.makeText(getApplicationContext(), 
								   "Try again after webpage is fully loaded", 
								   Toast.LENGTH_LONG)
								   .show();
				}else{
					Uri uriText = Uri.parse("@北美宠物网");
					Uri uriImage = Uri.parse(mSnapShotUrl);
					shareByApp("weibo", uriText, uriImage);
				}
			}
		});
		
		mWebView = (WebView) findViewById(R.id.article_activity_web_view);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);
		
		TAG_TITLE = getResources().getString(R.string.JSON_tag_title);
		TAG_SNAPSHOT = getResources().getString(R.string.JSON_tag_snapshot);
		TAG_IMAGE = getResources().getString(R.string.JSON_tag_image);
		TAG_CONTENT = getResources().getString(R.string.JSON_tag_content);		
		
		String articleURL_API = getIntent().getStringExtra("URL_API");
		
		new HttpGetTask().execute(articleURL_API);
	}
	
	/**
	 * Share a text and an image to a native app via implicit intents, if the name supplied
	 * is specific enough, then it starts the app with matching name immediately; if empty 
	 * string is provided for nameApp, all apps that can accept Intent.ACTION_SEND intent 
	 * will be shown for user selection. 
	 * 
	 * @param nameApp 	part of the name of the app to be shared content with
	 * @param textUri 	a Uri for a text, supply null if no text to be shared
	 * @param imageUri 	a Uri for an image, supply null if no image to be shared
	 */
	private void shareByApp(String nameApp, Uri textUri, Uri imageUri) {
	    List<Intent> targetedShareIntents = new ArrayList<Intent>();
	    Intent share = new Intent(android.content.Intent.ACTION_SEND);
	    share.setType("image/*");
	    List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);

	    if (!resInfo.isEmpty()){
	        for (ResolveInfo info : resInfo) {
	            Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
	            targetedShare.setType("image/*"); // put here your mime type
	            
	            if (info.activityInfo.name.toLowerCase().contains(nameApp)) {
	                targetedShare.putExtra(Intent.EXTRA_TEXT, textUri.toString());
	                targetedShare.putExtra(Intent.EXTRA_STREAM, imageUri);
	                targetedShare.setPackage(info.activityInfo.packageName);
	                targetedShareIntents.add(targetedShare);
	            }
	        }

	        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
	        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
	        startActivity(chooserIntent);
	    }else{
	    	/* TODO handle no native app with part of its name matching nameApp, 
	    	 * possibly needing to bring up the browswer and share there
	    	 */
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
				
				return results;
			} catch (HttpResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return null;
		}

		@Override
		protected void onPostExecute(Map<String, String> results) {
			if (null != mClient)
				mClient.close();
			
			String noContent = getResources().getString(R.string.no_content_available);
			String sTitle = results.get(HTML_TITLE); 
			if (sTitle != null){
				mTitleTextView.setText(sTitle);
			}else{
				mTitleTextView.setText(noContent);
			}
			
			String html_string = results.get(HTML_CONTENT);
			if (html_string == null){
				html_string = "<body>" + noContent + "</body>";
			}
			mWebView.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
			
			mSnapShotUrl = results.get(HTML_SNAPSHOT_URL);
			
			File tmpFile = GeneralHelpers.getOutputMediaFile(GeneralHelpers.MEDIA_TYPE_IMAGE);
			
			
			
			mProgBarView.setVisibility(View.GONE);
			
			
		}
	}
	
	

}
