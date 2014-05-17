package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.petsinaermica.askavet.utils.JSONResponseHandler;
import net.petsinamerica.askavet2.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
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

/*
 *  Need to combine this with the Article List activity,
 *  and make this just a fragment
 */

public class ArticleActivity extends Activity {

	private WebView mWebView;
	private TextView mTitleTextView;
	private ProgressBar mProgBarView;
	//private ImageView mImageView = null;
	
	
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
		
		// view port doesn't see useful in this case
		//mWebView.getSettings().setUseWideViewPort(true);
		//mWebView.getSettings().setLoadWithOverviewMode(true);
		
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);
		
		
		String articleURL_API = getIntent().getStringExtra("URL_API");
		
		new HttpGetTask().execute(articleURL_API);
		
		
	}
	
	
	private class HttpGetTask extends AsyncTask<String, Void, String> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mProgBarView.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(request);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				return JSONResponse;
			} catch (HttpResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return null;
		}

		@Override
		protected void onPostExecute(String JSONResponse) {
			if (null != mClient)
				mClient.close();
			//String s = result.get(0);
			// parse the JSON object to get the right content for display
			String sTitle = null, sImgURL = null, sContent = null;
			try {
				JSONObject responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				sTitle = responseObject.get("title").toString();
				sImgURL = responseObject.get("img").toString();
				sContent = responseObject.get("content").toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//String s = "";
			//String[] tokens = s.split(";;");
			
			if (sTitle != null){
				mTitleTextView.setText(sTitle);
			}else{
				mTitleTextView.setText("No Content Available");
			}
			
			String html_string =null;
			if (sImgURL != null && sContent != null){
				html_string = "<body>" + "<img src=\"" + sImgURL + "\">" + sContent + "</body>";
			}else{
				html_string = "<body>" + "No Content Available" + "</body>";
			}
			
			
			String pattern = "(<img.*?[jp][pn]g.*?\")(.?)(>)";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
			html_string = html_string.replaceAll(pattern, "$1 width=\"100%\" alt=\"\"$3");
			
			
			mWebView.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
			
			//String image_url = tokens[5];
			//mImageView.setTag(image_url);			
			//DownLoadImageTask loadImageTask = new DownLoadImageTask();
			//loadImageTask.execute(mImageView);
			
			mProgBarView.setVisibility(View.GONE);
			
			
		}
	}

}
