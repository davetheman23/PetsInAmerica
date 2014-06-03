package net.petsinamerica.askavet;

import java.io.IOException;

import com.squareup.picasso.Picasso;

import net.petsinamerica.askavet.utils.UserInfoManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PetListFragment extends Fragment implements UserInfoManager.Listener {
	
	private static final String sTAG = "Profile Activity";

	private Context mContext;
	private TextView tv_username;
	private TextView tv_email;
	public static ImageView iv_avatar;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mContext = activity.getApplicationContext();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.fragment_petlist, container,false);
		
		// setup page header that includes basic userinfo
		tv_username = (TextView) rootview.findViewById(R.id.frag_petlist_username);
		tv_email = (TextView) rootview.findViewById(R.id.frag_petlist_email);
		iv_avatar = (ImageView) rootview.findViewById(R.id.frag_petlist_image);
		
		UserInfoManager.registerListener(this);
		
		return rootview;
	}



	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (UserInfoManager.isInfoAvailable()){
			tv_username.setText(UserInfoManager.userDisplayName);
			tv_email.setText(UserInfoManager.email);
			if (UserInfoManager.avatar != null){
				iv_avatar.setImageBitmap(UserInfoManager.avatar);
			}
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		
	}

	@Override
	public void onWeiboInfoStateChange() {
		if (UserInfoManager.isWeiboInfoAvailable()){
			Picasso.with(mContext)
					.load(UserInfoManager.weiboUser.avatar_large)
					.resize(70, 70)
					.into(iv_avatar);
		}		
	}
	
	
	



	
	
}
