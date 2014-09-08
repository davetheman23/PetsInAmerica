package net.petsinamerica.askavet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
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
	private boolean isEnquiryReply = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// inflate the layouts
		setContentView(R.layout.activity_comments);
		
		// get article id from the extra that was set when the activity was started
		itemId = getIntent().getIntExtra(Constants.KEY_ARTICALID, 0);
		//articleId = 406;
		if (itemId == 0){
			//isUserComments = true;
			//itemId = getIntent().getIntExtra(Constants.KEY_USERID, 0);
			/*itemId = getIntent().getIntExtra(Constants.KEY_QUERYID, 0);
			isEnquiryReply = true;*/
			isArticleComments = false;
		}else{
			isArticleComments = true;
			//isEnquiryReply = false;
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
		}else if (isEnquiryReply){
			
		}
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_comments_container, commentFrag)
			.commit();
	}
	
	public static class CommentListFragment extends ListFragment{
		private Context mContext;
		
		private int mItemId;
		private int mCommentType;
		private String mNewComment;
		
		private static final int TYPE_USERCOMMENTS = 0;
		private static final int TYPE_ARTICLECOMMENTS = 1;
		
		CommentListAdapter commentlist = null;
		
		private GetCommentsInBackground getCommentsInBackground;
		
		private SubmitNewComments submitNewComments;
		
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
			final EditText et_comment = (EditText) rootView.findViewById(R.id.frag_comments_comment_content);
			final LinearLayout ll_NewCommentControls = (LinearLayout) rootView.findViewById(R.id.frag_comments_controls);
			final Button btn_Comment = (Button) rootView.findViewById(R.id.frag_comments_btn_comment);

			btn_Comment.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// first have to check if new comment is empty
					mNewComment = et_comment.getText().toString();
					if (mNewComment.equals("")){
						GeneralHelpers.showAlertDialog(mContext, "错误操作", "您还没有填入任何评论");
						return;
					}
					
					//GeneralHelpers.showMessage(mContext, "新评论功能还在完善中");
					String url = Constants.URL_NEW_COMMENT;
					// call api in background
					submitNewComments = (SubmitNewComments) new SubmitNewComments()
						.setParameters(getActivity(), CallPiaApiInBackground.TYPE_RETURN_LIST)
						.setProgressDialog(true, "请稍等，正在提交您的评论！")
						.setErrorDialog(true)
						.execute(url);
				}
			});
			
			String url = "";
			if (mCommentType == TYPE_USERCOMMENTS){
				url = Constants.URL_USER_COMMENTS;
				// when the activity is to show all user comments, then there is no need for the user to comment on 
				// any item, so just simply set those controls not visible
				et_comment.setVisibility(View.GONE);
				ll_NewCommentControls.setVisibility(View.GONE);
				btn_Comment.setVisibility(View.GONE);
			}else if (mCommentType == TYPE_ARTICLECOMMENTS) {
				url = Constants.URL_COMMENT + Integer.toString(mItemId);
			}
			// call api in background
			getCommentsInBackground = (GetCommentsInBackground) new GetCommentsInBackground()
				.setParameters(getActivity(), CallPiaApiInBackground.TYPE_RETURN_LIST)
				.setErrorDialog(true)
				.execute(url);
			
			return rootView;
		}
		
		@Override
		public void onDestroyView() {
			if (getCommentsInBackground != null){
				getCommentsInBackground.cancel(true);
			}
			if (submitNewComments != null){
				submitNewComments.cancel(true);
			}
			super.onDestroyView();
		}
		
		private class GetCommentsInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
					if (commentlist == null){
						commentlist = new CommentListAdapter(mContext, 
								R.layout.list_comment_item, result);
						setListAdapter(commentlist);
					}else{
						commentlist.addAll(result);
						commentlist.notifyDataSetChanged();
					}
				}
			}
		}
		private class SubmitNewComments extends CallPiaApiInBackground{

			@Override
			protected void addParamstoPost(HttpPost post, Context context)
					throws UnsupportedEncodingException, IOException {
				
				List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(post.getEntity());
				
				nameValuePairs.add(new BasicNameValuePair("pid", Integer.toString(mItemId)));
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_CONTENT, mNewComment));
				
				HttpParams httpParams = mClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
				
				// add the params into the post, make sure to include encoding UTF_8 as follows
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));				
				
			}

			@Override
			protected void onCallCompleted(Map<String, Object> result) {
			}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				if (result != null && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
					// if no error
					GeneralHelpers.showMessage(getActivity(), 
							"您的评论已成功发表！");
					if (commentlist != null){
						commentlist.clear();
						commentlist.addAll(result);
						commentlist.notifyDataSetChanged();
					}
				}else{
					GeneralHelpers.showAlertDialog(getActivity(), 
							"评论出错", "对不起，服务器出错啦！错误原因未知，请稍后再试。");
				}
				
			}
			
		}
	}
}
