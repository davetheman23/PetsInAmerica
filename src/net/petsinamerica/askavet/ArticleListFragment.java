package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.petsinaermica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.ArticleListAdapter.ViewHolder;
import net.petsinamerica.askavet2.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ArticleListFragment extends ListFragment{
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String PREFS_NAME = "PetInAmerica_ListArticles";
	public static final String KEY_ARTICLE_READ = "Article_Read";

	private static final String URL_BLOGCN = "http://petsinamerica.net/new/api/blogCN/";
	private static final String URL_ARTICLE_API = "http://petsinamerica.net/new/api/article/";
	//private static final String URL_ARTICLE = "http://petsinamerica.net/new/blog/article/";
	
	
	
	private int mPage = 1;
	boolean mflag_addData = false;		// false-don't add data
	ArticleListAdapter mALAdapter;
	private Context mContext;
	
	Set<String> mReadArticleList = null;		// the article list that has been read by the user
	
	SharedPreferences mUsageData;
	private AttributeSet mAttributes;
	

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		mAttributes = getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// may create custom view here
		//super.onCreateView(inflater, container, savedInstanceState);
		//View customFragmentView = inflater.inflate(R.layout.fragment_home,
//				container, false);
	//	return customFragmentView;
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// fetch article list from the website
		new HttpGetTask().execute(URL_BLOGCN + Integer.toString(mPage));
		
		mReadArticleList = new HashSet<String>();
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);		
		
		// monitor scroll activity, add more articles when scroll close to bottom
		getListView().setOnScrollListener(new OnScrollListener() {
			/*
			 * this listener is used to continuously load more article when scroll down to bottom
			 */
		@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0)
				{
					// when the visible item reaches the last item, 
					if (mflag_addData == false)
					{
						mPage += 1;
						mflag_addData = true;
						String url = URL_BLOGCN + Integer.toString(mPage);
						new HttpGetTask().execute(url);
					}
				}
			}
		});
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		// obtain the article ID clicked
		String articleID = mALAdapter.getArticleID(position);
		
		// store the article ID clicked
		//Record_Usage(articleID);
		
		
		// start a new activity 
		String articleURL_API = URL_ARTICLE_API + articleID;
		Intent newIntent = new Intent(mContext, ArticleActivity.class);
		newIntent.putExtra("URL_API", articleURL_API);
		startActivity(newIntent);
	}
	
	private class HttpGetTask extends AsyncTask<String, Void, String> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			//JSONResponseHandler responseHandler = new JSONResponseHandler();
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(request);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				return JSONResponse;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String JSONResponse) {
			if (null != mClient)
				mClient.close();
			if (isAdded()){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				
				
				// -- Parse Json object, 
				JSONObject responseObject = null;
				JSONArray articleSummaries = null;
				List<Map<String, String>> articleList = new ArrayList<Map<String, String>>();
				try {
					responseObject = (JSONObject) new JSONTokener(
							JSONResponse).nextValue();
					articleSummaries = responseObject.getJSONArray("list");
					if (articleSummaries != null){
						JsonHelper jhelper = new JsonHelper(); 
						articleList = jhelper.toList(articleSummaries);
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				if (mflag_addData == false){
					mALAdapter = new ArticleListAdapter(
										mContext, R.layout.article_list_header,
										R.layout.article_list_item, articleList);
					
					//setup an asynctask to batch download the images, based on all urls from results
					
					//getListView().addHeaderView(BuildHeaderView());
					setListAdapter(mALAdapter);
				}else{
					if (articleSummaries.length() > 0 ){
						mALAdapter.addAll(articleList);
						mflag_addData = false;
					}
				}
			}
		}
	}
	private View BuildHeaderView(){
		RelativeLayout rl = new RelativeLayout(mContext);
		TextView[] tv = new TextView[3];
		for (int i = 0; i < 3; i++){
			tv[i] = new TextView(mContext, mAttributes);
			tv[i].setId(i);
			tv[i].setText("test1");
			LayoutParams laypar = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if (i == 0)
				laypar.addRule(RelativeLayout.CENTER_IN_PARENT);
			else
				laypar.addRule(RelativeLayout.RIGHT_OF, tv[i-1].getId());
			rl.addView(tv[i],laypar);
		}		
		return rl;
	}
	
	private AttributeSet getAttributeSet(Context context, int layoutResource, String ViewName){
		// function to fetch the attribute set defined in layoutResource element ViewName
		AttributeSet Attributes = null;
		XmlPullParser parser = context.getResources().getLayout(layoutResource);
		int state = 0;
	    do {
	        try {
	            state = parser.next();
	        } catch (XmlPullParserException e1) {
	            e1.printStackTrace();
	        } catch (IOException e1) {
	            e1.printStackTrace();
	        }       
	        if (state == XmlPullParser.START_TAG) {
	            if (parser.getName().equals(ViewName)) {
	            	Attributes = Xml.asAttributeSet(parser);
	                break;
	            }
	        }
	    } while(state != XmlPullParser.END_DOCUMENT);
	    return Attributes;
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
		private static final String MAINTAG_TAG = "maintag";
		private static final String TAGS_TAG = "tag";

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

				// Extract value of "LIST_TAG" key -- a List
				JSONArray articleSummaries = responseObject
						.getJSONArray(LIST_TAG);
				
				// Iterate over earthquakes list
				for (int idx = 0; idx < articleSummaries.length(); idx++) {

					// Get a single article data - a Map
					JSONObject articleSummary = (JSONObject) articleSummaries.get(idx);
					
					String tags = articleSummary.get(TAGS_TAG).toString();
					tags = tags.replaceAll("\\#\\*", ";");
					tags = tags.replaceAll("\\#|\\*", "");
					
					// Summarize article data into a string and add to result
					result.add(articleSummary.get(TIME_TAG) + ";;"
							 + articleSummary.get(ID_TAG) + ";;" 
							 + articleSummary.get(TITLE_TAG) + ";;"
							 + articleSummary.get(CONTENT_TAG) + ";;"
							 + articleSummary.get(OWNER_TAG) + ";;"
							 + articleSummary.get(IMAGE_URL_TAG) + ";;"
							 + articleSummary.get(MAINTAG_TAG) + ";;"
							 + tags);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}
