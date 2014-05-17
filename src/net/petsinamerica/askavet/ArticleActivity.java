package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.petsinamerica.askavet2.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	
	
	private class HttpGetTask extends AsyncTask<String, Void, List<String>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mProgBarView.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<String> doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			JSONResponseHandler responseHandler = new JSONResponseHandler();
			
			try {
				return mClient.execute(request, responseHandler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			if (null != mClient)
				mClient.close();
			String s = result.get(0);
			String[] tokens = s.split(";;");
			
			mTitleTextView.setText(tokens[2]);
			
			// view port doesn't see useful in this case
			//String html_head = "<head><title>test</title><meta name=\"viewport\" content=\"width=device-width, user-scalable=no\"/></head>";
			String html_string = "<body>" + "<img src=\"" + tokens[5] + "\">" + tokens[3] + "</body>";
			
			
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

	
	
	private class JSONResponseHandler implements ResponseHandler<List<String>> {

		private static final String IMAGE_URL_TAG = "img";
		private static final String TIME_TAG = "time";
		private static final String ID_TAG = "id";
		private static final String AUTHOR_TAG = "author";
		private static final String OWNER_TAG = "owner";
		private static final String TITLE_TAG = "title";
		private static final String LIST_TAG = "list";
		private static final String CONTENT_TAG = "content";
		private static final String AVATAR_TAG = "avatar";

		@Override
		public List<String> handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			//List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
			List<String> result = new ArrayList<String>();
			String JSONResponse = new BasicResponseHandler()
					.handleResponse(response);
			try {

				// Get top-level JSON Object - a Map
				JSONObject responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				
				result.add(responseObject.get(TIME_TAG) + ";;"
						 + responseObject.get(ID_TAG) + ";;" 
						 + responseObject.get(TITLE_TAG) + ";;"
						 + responseObject.get(CONTENT_TAG) + ";;"
						 + responseObject.get(OWNER_TAG) + ";;"
						 + responseObject.get(IMAGE_URL_TAG));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
	}

}
