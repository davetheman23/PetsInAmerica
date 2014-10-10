package net.petsinamerica.askavet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class EnquiryActivity extends FragmentActivity {
	
	private static final int SHARE_TO_WEIBO = 1;
	private static final int SHARE_TO_WEIXIN = 2;
	private static final int SHARE_TO_TWITTER = 3;
	private static final int SHARE_TO_FACEBOOK = 4;
	
	private Uri mShareText = null;
	private static Uri mShareImage = null;
	private static String mShareSnapshotUrl = null;

	private SlidingUpPanelLayout mSlideUpPanelLayout;
	
	private Button mShareButton;
	private Button mReplyButton;
	private Button mCommentButton;
	private ImageView mWeiboShareIcon;
	private ImageView mWeixinShareIcon;
	private ImageView mFacebookShareIcon;
	private ImageView mShareMoreIcon;
	
	private int enquiryOwnerId;
	
	private int queryId;
	
	
	/*private static ProgressBar progressbar;*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_detail);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		// get query id from the extra that was set when the activity was started
		queryId = getIntent().getIntExtra(Constants.KEY_QUERYID, 0);
		enquiryOwnerId = getIntent().getIntExtra(Constants.KEY_ENQUIRY_OWNERID, 0);
				
		EnquiryDetailFragment enquiryfrag = new EnquiryDetailFragment();
		enquiryfrag.setParameters(queryId, enquiryOwnerId);
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_enquiry_details_content_container, enquiryfrag)
			.commit();
		/*
		progressbar = (ProgressBar) findViewById(R.id.activity_enquiry_details_load_progressbar);
		progressbar.setVisibility(View.VISIBLE);*/
		
		// get a screen height
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		
        mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mSlideUpPanelLayout.setOverlayed(false);
		mSlideUpPanelLayout.setMaxSlideRange((int)(height * 0.3));
		
		// setup the button click events within the slideup panel
		mShareButton = (Button) findViewById(R.id.activity_enquiry_details_btn_share);
		mShareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSlideUpPanelLayout.expandPanel();
			}
		});
		mCommentButton = (Button) findViewById(R.id.activity_enquiry_details_btn_comment);
		mCommentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralHelpers.showMessage(EnquiryActivity.this, "评论功能还在完善中！");
			}
		});
		mReplyButton = (Button) findViewById(R.id.activity_enquiry_details_btn_reply);
		mReplyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralHelpers.showMessage(EnquiryActivity.this, "回复功能还在完善中！");
			}
		});
		
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
		
		// set up the weixin share icon, share with text and image
		mShareMoreIcon = (ImageView) findViewById(R.id.article_activity_more_share);
		mShareMoreIcon.setOnClickListener(new ShareIconClickListener(null));
		mShareMoreIcon.setTag(999);
		
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
				GeneralHelpers.showMessage(EnquiryActivity.this, "页面还未完全读取完成，请稍后再试");
			}else{
				Intent intent = GeneralHelpers.shareByApp(appName, shareTextUri, mShareImage);
				startActivity(intent);
			}
			
			// close up the share panel
			if (mSlideUpPanelLayout != null){
				mSlideUpPanelLayout.collapsePanel();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mSlideUpPanelLayout != null && mSlideUpPanelLayout.isPanelExpanded()){
			mSlideUpPanelLayout.collapsePanel();
			return;
		}else{
			super.onBackPressed();
		}
	}


	@Override
	protected void onDestroy() {
		Log.d("EnquiryActivity", "onDestroy() is called");
		super.onDestroy();
	}




	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mSlideUpPanelLayout != null && mSlideUpPanelLayout.isPanelExpanded()){
			mSlideUpPanelLayout.collapsePanel();
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	
	public static class EnquiryDetailFragment extends ListFragment{
		private Context mContext;
		
		private int mQueryId = 0;
		private int mQueryOwnerId = 0;
		
		private GetEnquiryInBackground getEnquiryInBackground;
		
		private SubmitReplyInBackground submitReplyInBackground;
		
		private Button btn_SubmitReply;
		
		private EditText et_ReplyContent;
		
		private LinearLayout ll_ctrls;
		
		private EnquiryDetailListAdapter detailList;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
		}
		
		public void setParameters(int queryId, int ownerId){
			mQueryId = queryId;
			mQueryOwnerId = ownerId;
		}
		
		private void setReplyControlsVisibility(int visibility){
			ll_ctrls.setVisibility(visibility);
			btn_SubmitReply.setVisibility(visibility);
			et_ReplyContent.setVisibility(visibility);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_enquirydetails, null, false);
			
			btn_SubmitReply = (Button) rootView.findViewById(R.id.frag_enquiry_details_btn_reply);
			btn_SubmitReply.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!checkIsInputValid()){
						return;
					}
					if (submitReplyInBackground == null){
						submitReplyInBackground = (SubmitReplyInBackground) new SubmitReplyInBackground()
							.setParameters(getActivity(),CallPiaApiInBackground.TYPE_RETURN_LIST,true)
							.setProgressDialog(true,"回复正在提交中，请稍后...")
							.setErrorDialog(true)
							.execute(Constants.URL_ENQUIRY_REPLY);
					}else{
						if (!submitReplyInBackground.isIdle()){
							GeneralHelpers.showAlertDialog(getActivity(), null, "问题提交正在处理中，请稍后再试");
						}else{
							// submit another reply 
							submitReplyInBackground.execute(Constants.URL_ENQUIRY_REPLY);
						}
					}
				}
			});
			et_ReplyContent = (EditText) rootView.findViewById(R.id.frag_enquiry_details_reply_content);
			
			ll_ctrls = (LinearLayout) rootView.findViewById(R.id.frag_enquiry_details_controls);
			
			setReplyControlsVisibility(View.GONE);
			
			// call api in background 
			String queryURL_API = Constants.URL_ENQUIRY_DETAILS + Integer.toString(mQueryId);
			getEnquiryInBackground = (GetEnquiryInBackground) new GetEnquiryInBackground()
				.setParameters(getActivity(),CallPiaApiInBackground.TYPE_RETURN_LIST,true)
				.execute(queryURL_API);
			
			return rootView;
		}
		
		private boolean checkIsInputValid(){
			String  replyContent = et_ReplyContent.getText().toString();
			
			if (replyContent.equals("")){
				GeneralHelpers.showAlertDialog(getActivity(), null, "您还没有输入任何问题");
				return false;
			}
			
			if (mQueryId == 0){
				GeneralHelpers.showAlertDialog(getActivity(), "应用错误", "无法获取询问单 ID");
				return false;
			}
			
			return true;
		}
		
		
		@Override
		public void onDestroyView() {
			if (getEnquiryInBackground!=null){
				getEnquiryInBackground.cancel(true);
			}
			if (submitReplyInBackground != null){
				submitReplyInBackground.cancel(true);
			}
			super.onDestroyView();
		}

		private class GetEnquiryInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null){
					if (result.size() > 0 && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
						// if no error
						Map<String, Object> enquiry = result.get(0);
						if (enquiry.containsKey("status")){
							// check to see if:
							// 1. the enquiry is asked by the user, if not, only allow viewing
							// 2. the answer status is new answer
							if (Integer.parseInt(enquiry.get("status").toString()) == Constants.STATUS_NEWANSWER
								&& mQueryOwnerId == AccessTokenManager.getUserId(App.appContext)){
								
								// set up the footer to allow the user to mark the problem as solved
								LayoutInflater inflater = (LayoutInflater) mContext
										.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								View footerview = (View) inflater.inflate(R.layout.list_enquiry_details_footer_item, null);
								getListView().addFooterView(footerview);
								getListView().setFooterDividersEnabled(true);
								
								// set up a alert dialog to double check with the user if enquiry should be marked solved
								Button btn_submitSolved = (Button) footerview.findViewById(R.id.list_enquiry_details_footer_submitsolved);
								btn_submitSolved.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
										builder.setTitle("请确认")
										.setMessage("您的问题已经得到解决了么？")
										.setCancelable(false)
										.setPositiveButton("是的", new DialogInterface.OnClickListener(){
											@Override
											public void onClick(DialogInterface dialog, int which) {
												new SubmitSolvedInBackground()
													.setParameters(mContext, CallPiaApiInBackground.TYPE_RETURN_MAP, true)
													.setErrorDialog(true)
													.setProgressDialog(true, null)
													.execute(Constants.URL_ENQUIRY_MARKSOLVED);
												dialog.cancel();
											}
											
										})
										.setNegativeButton("还没有", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// allow user to reply if it is his/her own question 
												setReplyControlsVisibility(View.VISIBLE);
												dialog.cancel();
											}
										});
										AlertDialog dialog = builder.create();
										dialog.show();
									}
								});
							}
						}
						
						// load the enquiry questions in the list
						detailList = new EnquiryDetailListAdapter(mContext, 
								R.layout.list_enquiry_details_header_item, 
								R.layout.list_enquiry_details_item, result);
				
						setListAdapter(detailList);
					}
				}
			}
		}
		
		private class SubmitSolvedInBackground extends CallPiaApiInBackground{
			
			@Override
			protected HttpPost addParamstoPost(HttpPost post, Context context)
					throws UnsupportedEncodingException, IOException {
				
				// get the parameters already exited in the post, normally are user Id and token
				List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(post.getEntity());
				
				// add more parameters to the post
				nameValuePairs.add(new BasicNameValuePair("queryid", Integer.toString(mQueryId)));
				
				// reset a new entity with all parameters 
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				
				return post;
			}

			@Override
			protected void onCallCompleted(Map<String, Object> result) {
				if (result != null){
					if (!result.containsKey(Constants.KEY_ERROR_MESSAGE)){
						// if no error
						// show success message
						GeneralHelpers.showAlertDialog(getActivity(), "解决了！", "谢谢您的提问，问题已解决！如果问题有新发展，请按新问题提问。");
						// empty the reply box
						ll_ctrls.setVisibility(View.GONE);
					}
				}
				
			}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		private class SubmitReplyInBackground extends CallPiaApiInBackground{

			@Override
			protected HttpPost addParamstoPost(HttpPost post, Context context)
					throws UnsupportedEncodingException, IOException {
				
				// here assume that user inputs are all valid, checks need to be performed before 
				String content = et_ReplyContent.getText().toString();
				
				// get the parameters already exited in the post, normally are user Id and token
				List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(post.getEntity());
				
				// add more parameters to the post
				nameValuePairs.add(new BasicNameValuePair("queryid", Integer.toString(mQueryId)));
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_CONTENT, content));
				
				// reset a new entity with all parameters 
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				
				return post;
			}

			@Override
			protected void onCallCompleted(Map<String, Object> result) {
			}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				if (result != null){
					if (result.size() > 0 && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
						// if no error
						// update the list of all Q&A
						if (detailList != null){
							detailList.clear();
							detailList.addAll(result);
							detailList.notifyDataSetChanged();
						}
						// show success message
						GeneralHelpers.showAlertDialog(getActivity(), "成功", "您的回复已成功提交，请耐心等候兽医的回复");
						// empty the reply box
						et_ReplyContent.setText("");
					}
				}
			}
		}
	}
	
}
