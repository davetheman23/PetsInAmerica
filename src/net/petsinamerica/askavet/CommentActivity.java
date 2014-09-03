package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class CommentActivity extends FragmentActivity{
	
	private int itemId; 
	private boolean isUserComments = false;
	private boolean isArticleComments = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// inflate the layouts
		setContentView(R.layout.activity_comments);
		
		// get article id from the extra that was set when the activity was started
		itemId = getIntent().getIntExtra("ArticleId", 0);
		//articleId = 406;
		if (itemId == 0){
			//isUserComments = true;
			//itemId = getIntent().getIntExtra(Constants.KEY_USERID, 0);
		}else{
			isArticleComments = true;
		}
		
		if (itemId == 0){
			// TODO do something more here
			GeneralHelpers.showMessage(this, "无法获取评论列表 ID");
			return;
		}
		
		// setup the comment list fragment based on what comment it is refering to
		CommentListFragment commentFrag = new CommentListFragment();
		if (isUserComments){		
			commentFrag.setParameters(itemId, CommentListFragment.TYPE_USERCOMMENTS);
		}else if (isArticleComments){
			commentFrag.setParameters(itemId, CommentListFragment.TYPE_ARTICLECOMMENTS);
		}
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_comments_container, commentFrag)
			.commit();
	}
	
	public static class CommentListFragment extends ListFragment{
		private Context mContext;
		
		private int mItemId;
		private int mCommentType;
		
		private static final int TYPE_USERCOMMENTS = 0;
		private static final int TYPE_ARTICLECOMMENTS = 1;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
		}
		
		public void setParameters(int articleId, int type){
			mItemId = articleId;
			mCommentType = type;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_comments, null, false);
			
			// get references to each widget
			final EditText et_Comment = (EditText) rootView.findViewById(R.id.frag_comments_comment_content);
			final LinearLayout ll_NewCommentControls = (LinearLayout) rootView.findViewById(R.id.frag_comments_controls);
			final Button btn_Comment = (Button) rootView.findViewById(R.id.frag_comments_btn_comment);

			btn_Comment.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					GeneralHelpers.showMessage(mContext, "新评论功能还在完善中");
				}
			});
			
			String url = "";
			if (mCommentType == TYPE_USERCOMMENTS){
				url = Constants.URL_USER_COMMENTS;
				// when the activity is to show all user comments, then there is no need for the user to comment on 
				// any item, so just simply set those controls not visible
				et_Comment.setVisibility(View.GONE);
				ll_NewCommentControls.setVisibility(View.GONE);
				btn_Comment.setVisibility(View.GONE);
			}else if (mCommentType == TYPE_ARTICLECOMMENTS) {
				url = Constants.URL_COMMENT + Integer.toString(mItemId);
			}
			// call api in background
			new GetCommentsInBackground()
				.setParameters(getActivity(), CallPiaApiInBackground.TYPE_RETURN_LIST)
				.execute(url);
			
			return rootView;
		}
		
		private class GetCommentsInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Integer result) {}
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null){
					
					CommentListAdapter commentlist = new CommentListAdapter(mContext, 
							R.layout.list_comment_item, result);
					
					setListAdapter(commentlist);
				}
			}
		}
	}

}
