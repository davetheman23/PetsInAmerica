package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.petsinamerica.askavet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EnquiryActivity extends Activity {

	private WebView mWebView;
	private TextView mTitleTextView;
	private ProgressBar mProgBarView;
	//private ImageView mImageView = null;
	private static final String HTML_CONTENT = "Html_Content";
	private static final String HTML_TITLE = "Html_Title";
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	private static String TAG_IMAGE;
	private static String TAG_CONTENT;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article);
		
		mTitleTextView = (TextView) findViewById(R.id.article_title);
		//mImageView = (ImageView) findViewById(R.id.article_image);
		mProgBarView = (ProgressBar) findViewById(R.id.article_load_progressbar);
		mProgBarView.setVisibility(View.VISIBLE);
		
		mWebView = (WebView) findViewById(R.id.web_view);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);
		
		TAG_TITLE = getResources().getString(R.string.JSON_tag_title);
		//TAG_IMAGE = getResources().getString(R.string.JSON_tag_image);
		TAG_CONTENT = getResources().getString(R.string.JSON_tag_content);		
		
		//String articleURL_API = getIntent().getStringExtra("URL_API");
		//new HttpGetTask().execute(articleURL_API);
		
		String enqueryContent = getIntent().getStringExtra(TAG_CONTENT);
		String html_string = "<body>" + enqueryContent + "</body>";
		
		mWebView.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
		
	}
	
	/*
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
				//String sImgURL = responseObject.get(TAG_IMAGE).toString();
				String sContent = responseObject.get(TAG_CONTENT).toString();
				String html_string = null;
				if (sContent != null){
					html_string = "<body>" +  sContent + "</body>";
					String pattern = "(<img.*?[jp][pn]g.*?\")(.?)(>)";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
					html_string = html_string.replaceAll(pattern, "$1 width=\"100%\" alt=\"\"$3");
				}
				
				// -- put the data together and display on the UI 
				Map<String, String> results = new HashMap<String, String>();
				results.put(HTML_TITLE, sTitle);
				results.put(HTML_CONTENT, html_string);
				// result.put("more contents", more contents);
				
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
			
			//String image_url = tokens[5];
			//mImageView.setTag(image_url);			
			//DownLoadImageTask loadImageTask = new DownLoadImageTask();
			//loadImageTask.execute(mImageView);
			
			mProgBarView.setVisibility(View.GONE);
			
			
		}
	}*/
}
