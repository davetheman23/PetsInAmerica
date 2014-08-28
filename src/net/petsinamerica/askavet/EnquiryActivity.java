package net.petsinamerica.askavet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

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
	
	
	/*private static ProgressBar progressbar;*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_detail);
		
		// get query id from the extra that was set when the activity was started
		int queryId = getIntent().getIntExtra("QueryId", 0);
		//queryId = 1439;
				
		EnquiryDetailFragment enquiryfrag = new EnquiryDetailFragment();
		
		enquiryfrag.setParameters(queryId);
		
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
		mCommentButton.setOnClickListener(new View.OnClickListener() {
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
		
		private int mQueryId;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
		}
		
		public void setParameters(int queryId){
			mQueryId = queryId;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_enquirydetails, null, false);
			
			// call api in background 
			String queryURL_API = Constants.URL_ENQUIRY_DETAILS + Integer.toString(mQueryId);
			GetEnquiryInBackground getEuquiry = new GetEnquiryInBackground();
			getEuquiry.setResultType(CallPiaApiInBackground.TYPE_RETURN_LIST);
			getEuquiry.execute(queryURL_API);
			
			return rootView;
		}
		
		private class GetEnquiryInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null){
					
					EnquiryDetailListAdapter detailList = new EnquiryDetailListAdapter(mContext, 
									R.layout.list_enquiry_details_header_item, 
									R.layout.list_enquiry_details_item, result);
					
					setListAdapter(detailList);
					
					
					Map<String, Object> queryInfo = result.get(0);
					
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
	
	/**
	 * define a private variable of the class Target to be used for Picasso
	 */
	private static Target target = new Target() {
		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
		}
		
		@Override
		public void onBitmapLoaded(final Bitmap bitmap, LoadedFrom from) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					File tmpFile = GeneralHelpers.getOutputMediaFile(
							GeneralHelpers.MEDIA_TYPE_IMAGE, true);					
					try 
					{
						tmpFile.createNewFile();
						FileOutputStream ostream = new FileOutputStream(tmpFile);
						bitmap.compress(CompressFormat.JPEG, 75, ostream);
						ostream.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					mShareImage = Uri.fromFile(tmpFile);
					
				}
			}).start();

		}
		
		@Override
		public void onBitmapFailed(Drawable errorDrawable) {

		}
	};
	
}
