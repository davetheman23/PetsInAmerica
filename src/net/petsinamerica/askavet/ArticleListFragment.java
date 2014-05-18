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
import net.petsinamerica.askavet.R;

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
	
	private static String TAG_ARTICLE_LIST;
	
	
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
		
		TAG_ARTICLE_LIST = mContext.getResources().getString(R.string.JSON_tag_list);
		
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
		int articleID = mALAdapter.getArticleID(v);
		
		// store the article ID clicked
		//Record_Usage(articleID);
		
		// start a new activity 
		String articleURL_API = URL_ARTICLE_API + articleID;
		Intent newIntent = new Intent(mContext, ArticleActivity.class);
		newIntent.putExtra("URL_API", articleURL_API);
		startActivity(newIntent);
	}
	
	private class HttpGetTask extends AsyncTask<String, Void, List<Map<String, String>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected List<Map<String, String>> doInBackground(String... params) {
			String url = params[0];
			HttpGet request = new HttpGet(url);
			//JSONResponseHandler responseHandler = new JSONResponseHandler();
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(request);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				
				// -- Parse Json object, 
				JSONObject responseObject = null;
				JSONArray articleSummaries = null;
				List<Map<String, String>> articleList = null;
				
				responseObject = (JSONObject) new JSONTokener(
						JSONResponse).nextValue();
				articleSummaries = responseObject.getJSONArray(TAG_ARTICLE_LIST);
				if (articleSummaries != null){
					JsonHelper jhelper = new JsonHelper(); 
					articleList = jhelper.toList(articleSummaries);
				}			
				return articleList;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, String>> resultArray) {
			if (null != mClient)
				mClient.close();
			if (isAdded() && resultArray != null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				
				if (mflag_addData == false){
					mALAdapter = new ArticleListAdapter(
										mContext, R.layout.article_list_header,
										R.layout.article_list_item, resultArray);
					
					//setup an asynctask to batch download the images, based on all urls from results
					
					//getListView().addHeaderView(BuildHeaderView());
					setListAdapter(mALAdapter);
				}else{
					if (resultArray.size() > 0 ){
						mALAdapter.addAll(resultArray);
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

}
