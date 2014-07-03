package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.UserInfoManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
			setCustomAdapter(new PetListAdapter2(mContext, 
						R.layout.pet_list_item, resultArray));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			int petId = ((PetListAdapter2)this.getListAdapter()).getPetId(v);
			Intent intent = new Intent(mContext, MyPetActivity.class);
			intent.putExtra("id", petId);
			startActivity(intent);
		}
		
	}
	
	private static class PetListAdapter2 extends ArrayAdapter<Map<String, Object>> {
		private final Context mContext;
		private final int mResource;
		private ViewGroup mParent;
		
		// these tags are those for reading the JSON objects
		private String KEY_AVATAR;
		private String KEY_ID;
		private String KEY_NAME;
		
		private class ViewHolder{
			ImageView ivPetAvatar;
			TextView tvPetName;
			int petId;
		}
		
		/**
		 *  Standard constructer
		 */
		public PetListAdapter2(Context context, int resource,
				List<Map<String, Object>> objects) {
			super(context, resource, objects);
			
			mContext = context;
			mResource = resource;
			
			KEY_AVATAR = mContext.getResources().getString(R.string.JSON_tag_petavatar);
			KEY_ID = mContext.getResources().getString(R.string.JSON_tag_id);
			KEY_NAME = mContext.getResources().getString(R.string.JSON_tag_petname);
			
		}

		/*
		 *  each row in the list will call getView, this implementation deterimes
		 *  the behavior and layout of each row of the list
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (mParent == null){
				mParent = parent;
			}

			// reuse views - for faster loading, avoid inflation everytime
			ViewHolder viewHolder = null;
			View rowview = convertView;
			if (rowview == null){
				LayoutInflater inflater = (LayoutInflater) mContext.
							getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				// if no rowview before, new viewholder is created
				viewHolder = new ViewHolder();
				
				// inflate the layout view, and get individual views
				rowview = inflater.inflate(mResource, parent, false);
				viewHolder.ivPetAvatar = (ImageView) rowview.findViewById(R.id.pet_list_item_image);
				viewHolder.tvPetName = (TextView) rowview.findViewById(
														R.id.pet_list_item_name);
				
				// set tag for future reuse of the view
				rowview.setTag(viewHolder);
			}else{

				viewHolder = (ViewHolder) rowview.getTag();
			}
			
			Map<String, Object> listItem = getItem(position); 
			String sName, sImgURL, sItemId;
			sName = (String) listItem.get(KEY_NAME);
			sName = sName.trim();
			sImgURL = (String) listItem.get(KEY_AVATAR);
			sItemId = (String) listItem.get(KEY_ID);
			
			viewHolder.petId = Integer.parseInt(sItemId);
			viewHolder.tvPetName.setText(sName);
			
			if (sImgURL!= null && !sImgURL.startsWith("http")){
				sImgURL = Constants.URL_FILE_STORAGE + sImgURL;
			}
			
			// image loading procedure:
			// 1. check if image available in memory / disk
			// 2. set image if not in memory then fetch from URL
			// Note: currently, use picasso instead
			Picasso.with(mContext)
				.load(sImgURL)
				.placeholder(R.drawable.somepet)
				.into(viewHolder.ivPetAvatar);
			if (sImgURL== null || sImgURL.endsWith("somepet.png")){
				Picasso.with(mContext)
					.cancelRequest(viewHolder.ivPetAvatar);
			}
			return rowview;
			
		}
		
		/**
		 * return a item ID from view selected, if item id is not 
		 * available, -1 will be returned
		 */
		public int getPetId(View v) {
			// this assumes the view is the row view so it has a viewholder
			ViewHolder vh = (ViewHolder) v.getTag();
			if (vh != null){
				return vh.petId;
			}
			return -1;	
		}
	}
}
