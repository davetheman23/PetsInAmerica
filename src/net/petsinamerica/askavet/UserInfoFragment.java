package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.UserInfoManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class UserInfoFragment extends Fragment implements UserInfoManager.Listener {
	
	private static String sKEY_CONTENT = App.appContext.getString(R.string.JSON_tag_content);
	private static String sKEY_RESULT = App.appContext.getString(R.string.JSON_tag_result);
	
	private static final String sTAG = "Profile Activity";

	private static Context mContext;
	private TextView tv_username;
	private TextView tv_email;
	private TextView tv_weiboUsername;
	private TextView tv_city;
	public static ImageView iv_avatar;
	
	private static PetListFragment mPetListFragment = null;
	private boolean firstAdd = true;	// first time the petlist fragment is added

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		
		UserInfoManager.registerWeiboInfoListener(this);
		UserInfoManager.registerPiaInfoListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.fragment_userinfo, container,false);
		
		// setup page header that includes basic userinfo
		tv_username = (TextView) rootview.findViewById(R.id.frag_userinfo_username);
		tv_email = (TextView) rootview.findViewById(R.id.frag_userinfo_email);
		tv_weiboUsername = (TextView) rootview.findViewById(R.id.frag_userinfo_weibousername);
		tv_city = (TextView) rootview.findViewById(R.id.frag_userinfo_city);
		iv_avatar = (ImageView) rootview.findViewById(R.id.frag_userinfo_image);
		
		return rootview;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadProfilePic();
		loadUserInfo();
	}

	@Override
	public void onWeiboInfoStateChange() {
		loadProfilePic();
	}

	@Override
	public void onPiaInfoStateChange() {
		loadUserInfo();
	}
	
	private void loadProfilePic(){
		if (UserInfoManager.isWeiboInfoAvailable()){
			Picasso.with(mContext)
					.load(UserInfoManager.weiboUser.avatar_large)
					.resize(70, 70)
					.into(iv_avatar);
		}
	}
	private void loadUserInfo(){
		if (UserInfoManager.isInfoAvailable()){
			tv_username.setText(UserInfoManager.userDisplayName);
			tv_email.setText(UserInfoManager.email);
			tv_weiboUsername.setText(UserInfoManager.weiboUsername);
			tv_city.setText(UserInfoManager.city);
			if (UserInfoManager.avatar != null){
				iv_avatar.setImageBitmap(UserInfoManager.avatar);
			}
			
			if (mPetListFragment == null){
				mPetListFragment = new PetListFragment();
				mPetListFragment.setParameters(Constants.URL_USERPETS, sKEY_RESULT,false);
				mPetListFragment.setPage(Integer.parseInt(UserInfoManager.userid));
				mPetListFragment.setUserDataFlag(true);
			}
			if (firstAdd){
				getFragmentManager()
					.beginTransaction()
					.add(R.id.frag_userinfo_petlist_container, mPetListFragment)
					.commit();
				firstAdd = false;
			}else{
				getFragmentManager()
					.beginTransaction()
					.replace(R.id.frag_userinfo_petlist_container, mPetListFragment)
					.addToBackStack(null)
					.commit();
			}
		}
	}
	
	public static class PetListFragment extends BaseListFragment{

		@Override
		protected void onHttpDoneSetAdapter(
				List<Map<String, Object>> resultArray) {
			setCustomAdapter(new PetListAdapter(mContext, 
						R.layout.pet_list_item_with_selection, resultArray));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			return;
			
		}
		
	}

}
