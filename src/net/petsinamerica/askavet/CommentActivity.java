package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CommentActivity extends FragmentActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// inflate the layouts
		setContentView(R.layout.activity_comments);
		
		// get query id from the extra that was set when the activity was started
		int articleId = getIntent().getIntExtra("ArticleId", 0);
		//articleId = 406;
		
		
		CommentListFragment commentFrag = new CommentListFragment();
		
		commentFrag.setParameters(articleId);
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_comments_container, commentFrag)
			.commit();
		
		
	}
	
	public static class CommentListFragment extends ListFragment{
		private Context mContext;
		
		private int mArticleId;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
		}
		
		public void setParameters(int articleId){
			mArticleId = articleId;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_comments, null, false);
			
			// call api in background 
			String queryURL_API = Constants.URL_COMMENT + Integer.toString(mArticleId);
			GetCommentsInBackground getComments = new GetCommentsInBackground();
			getComments.setResultType(CallPiaApiInBackground.TYPE_RETURN_LIST);
			getComments.execute(queryURL_API);
			
			return rootView;
		}
		
		private class GetCommentsInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null){
					
					CommentListAdapter commentlist = new CommentListAdapter(mContext, 
							R.layout.list_comment_item, result);
					
					setListAdapter(commentlist);
					
					// TODO notify user that they can be the first to comment. 
					
					//Map<String, Object> queryInfo = result.get(0);
					
					/* set up a background task to load the snapshot url into the target
				 	in case user will share it, so it can be ready after user read */	
					
					
					//mShareSnapshotUrl = queryInfo.get(Constants.KEY_SNAPSHOT).toString();
					/*Picasso.with(App.appContext)
						.load(Uri.parse(mShareSnapshotUrl))
						.into(target);*/
					int i = 0;
					i= i +1;
				}
			}
		}
	}

}
