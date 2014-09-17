package net.petsinamerica.askavet;

import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.UserInfoManager;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class UserInfoFragment extends Fragment implements UserInfoManager.Listener {
	
	
	
	//private static final String sTAG = "Profile Activity";

	private static Context mContext;
	private TextView tv_nickname;
	private TextView tv_weiboUsername;
	private TextView tv_city;
	private Button btn_edit;
	/*private static HorizontalListView hv_PetList;*/
	public static ImageView iv_avatar;
	
	/*private static PetListAdapter2 petAdapter;
	
	private static PetListFragment mPetListFragment = null;*/
	/*private boolean firstAdd = true;	// first time the petlist fragment is added
*/	
	static final int[] linearLayouts = new int[]{
		R.id.frag_userinfo_ll_commentedarticles, R.id.frag_userinfo_ll_favorites,
		R.id.frag_userinfo_ll_mypets, R.id.frag_userinfo_ll_myenquiries,
		R.id.frag_userinfo_ll_likedarticles, R.id.frag_userinfo_ll_settings,
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		
		UserInfoManager.registerWeiboInfoListener(this);
		UserInfoManager.registerPiaInfoListener(this);
		
	}
	
	LayoutTransition layoutTransition;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.fragment_userinfo, container,false);
		
		// setup page header that includes basic userinfo
		tv_nickname = (TextView) rootview.findViewById(R.id.frag_userinfo_nickname);
		tv_weiboUsername = (TextView) rootview.findViewById(R.id.frag_userinfo_weibousername);
		tv_city = (TextView) rootview.findViewById(R.id.frag_userinfo_city);
		iv_avatar = (ImageView) rootview.findViewById(R.id.frag_userinfo_image);
		btn_edit = (Button) rootview.findViewById(R.id.frag_userinfo_edit);
		
		btn_edit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(getActivity(), UserProfileActivity.class);
				startActivity(intent);
			}
		});
		
		
		for (final int linearLayout : linearLayouts){
			LinearLayout lls = (LinearLayout)rootview.findViewById(linearLayout);
			lls.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = null;
					switch (linearLayout){
						case R.id.frag_userinfo_ll_mypets:
							intent = new Intent(getActivity(), MyPetActivity.class);
							startActivity(intent);
							break;
						case R.id.frag_userinfo_ll_commentedarticles:
							intent = new Intent(getActivity(), MyArticleListActivity.class);
							intent.putExtra("ListType", MyArticleListActivity.LIST_TYPE_USERCOMMENTED);
							startActivity(intent);
							break;
						case R.id.frag_userinfo_ll_likedarticles:
							intent = new Intent(getActivity(), MyArticleListActivity.class);
							intent.putExtra("ListType", MyArticleListActivity.LIST_TYPE_USERLIKED);
							startActivity(intent);
							break;
						case R.id.frag_userinfo_ll_myenquiries:
							intent = new Intent(getActivity(), MyEnquiryListActivity.class);
							startActivity(intent);
							break;
						default:
							Toast.makeText(mContext, "功能还在完善中" + Integer.toString(linearLayout), Toast.LENGTH_LONG).show();
					}
					
				}
			});
		}
		
		return rootview;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadUserInfo();
		loadPiaProfilePic();
	}

	@Override
	public void onWeiboInfoStateChange() {
		//loadWeiboProfilePic();
	}

	@Override
	public void onPiaInfoStateChange() {
		loadUserInfo();
		loadPiaProfilePic();
	}
	
	private void loadWeiboProfilePic(){
		if (UserInfoManager.isWeiboInfoAvailable()){
			Picasso.with(mContext)
					.load(UserInfoManager.weiboUser.avatar_large)
					.resize(70, 70)
					.into(iv_avatar);
		}
	}
	
	private void loadPiaProfilePic(){
		if (UserInfoManager.isInfoAvailable()){
			String userAvatarURL = UserInfoManager.avatarURL;
			if (userAvatarURL != null && !userAvatarURL.startsWith("http")){
				userAvatarURL = Constants.URL_CLOUD_STORAGE + userAvatarURL;
			}
			Picasso.with(mContext)
			.load(userAvatarURL)
			.resize(70, 70)
			.into(iv_avatar);
			if (null == userAvatarURL || userAvatarURL.endsWith("someone.png")){
				// cancel request when download is not needed
				Picasso.with(mContext)
					.cancelRequest(iv_avatar);
				// instead try load profile pic from weibo
				loadWeiboProfilePic();
			}
		}
	}
	private void loadUserInfo(){
		if (UserInfoManager.isInfoAvailable()){
			tv_nickname.setText(UserInfoManager.userDisplayName);
			tv_weiboUsername.setText(UserInfoManager.weiboUsername);
			tv_city.setText(UserInfoManager.city);
			if (UserInfoManager.avatar != null){
				iv_avatar.setImageBitmap(UserInfoManager.avatar);
			}
		}
	}
	
	
	
	
}
