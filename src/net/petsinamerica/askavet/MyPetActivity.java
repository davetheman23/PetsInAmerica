package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.UserInfoManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MyPetActivity extends FragmentActivity{
	
	public FragmentManager fm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_petinfo);
		
		if (UserInfoManager.userid == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("用户信息不完全")
			.setMessage("请稍后再重新读取该页面，正在获取用户信息")
			.setCancelable(false)
			.setPositiveButton("好的", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}else{
			// setup the list fragment that shows a list of all pets of the user		
			PetListFragment petListFragment = new PetListFragment();
			
			fm = getSupportFragmentManager(); 
			fm.beginTransaction()
				.add(R.id.activity_petinfo_container, petListFragment)
				.commit();
		}
		
	}

	
	
	public static class PetListFragment extends ListFragment{
		
		private Context mContext;
		
		private GetPetListInBackground getPetListInBackground;
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
		}
		
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			
			if (position != getListView().getCount()-getListView().getFooterViewsCount()){
				// if not the footer clicked
				int petId = ((PetListAdapter2)getListAdapter()).getPetId(v);			
				Intent intent = new Intent(getActivity(), MyPetDetailsActivity.class);
				intent.putExtra(Constants.KEY_PET_ID, petId);
				startActivity(intent);			
			}else{
				// if footer is clicked
				/*Intent intent = new Intent(getActivity(), MyPetDetailsActivity.class);
				startActivity(intent);*/
				Toast.makeText(getActivity(), "功能还在完善中，暂时仅支持网上添加宠物", Toast.LENGTH_LONG).show();;
			}
			
		}

		@Override
		public void onDestroyView() {
			if (getPetListInBackground!= null){
				getPetListInBackground.cancel(true);
			}
			super.onDestroyView();
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			getPetListInBackground = (GetPetListInBackground) new GetPetListInBackground()
				.setParameters(getActivity(),CallPiaApiInBackground.TYPE_RETURN_LIST)
				.setErrorDialog(true)
				.execute(Constants.URL_USERPETS + "/" + UserInfoManager.userid);
			
		}

		private class GetPetListInBackground extends CallPiaApiInBackground{
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {}

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {
				// get the query information
				if (result != null && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
					// if no error
					LayoutInflater inflater = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);										
					View footerview = (View) inflater.inflate(R.layout.list_footer_pet_item_addpet, null);
					getListView().addFooterView(footerview);
					getListView().setFooterDividersEnabled(true);
					
					PetListAdapter2 petlist = new PetListAdapter2(mContext, 
							R.layout.list_pet_item, result);
					
					setListAdapter(petlist);
				}else{
					// do something here if necessary
				}
			}
		}
		
	}
	
	private static class PetListAdapter2 extends ArrayAdapter<Map<String, Object>> {
		private final Context mContext;
		private final int mResource;
		
		private class ViewHolder{
			ImageView ivPetAvatar;
			TextView tvPetName;
			TextView tvPetSpecies;
			TextView tvPetBreed;
			TextView tvPetSex;
			TextView tvPetBday;
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

		}

		/*
		 *  each row in the list will call getView, this implementation deterimes
		 *  the behavior and layout of each row of the list
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

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
				viewHolder.ivPetAvatar = (ImageView) rowview.findViewById(R.id.pet_list_pic);
				viewHolder.tvPetName = (TextView) rowview.findViewById(
														R.id.pet_list_pet_name);
				viewHolder.tvPetSpecies = (TextView) rowview.findViewById(
														R.id.pet_list_petspecies);
				viewHolder.tvPetBreed = (TextView) rowview.findViewById(
														R.id.pet_list_petbreed);
				viewHolder.tvPetSex = (TextView) rowview.findViewById(
														R.id.pet_list_petsex);
				viewHolder.tvPetBday = (TextView) rowview.findViewById(
														R.id.pet_list_pet_bday);
				
				// set tag for future reuse of the view
				rowview.setTag(viewHolder);
			}else{

				viewHolder = (ViewHolder) rowview.getTag();
			}
			
			Map<String, Object> listItem = getItem(position); 
			String sName, sImgURL, sItemId;
			sName = (String) listItem.get(Constants.KEY_PET_NAME);
			sName = sName.trim();
			sImgURL = (String) listItem.get(Constants.KEY_PET_PIC);
			sItemId = (String) listItem.get(Constants.KEY_PET_ID);
			String bDay = (String) listItem.get(Constants.KEY_PET_BDAY);
			String breed = (String) listItem.get(Constants.KEY_PET_BREED);
			String sex = (String) listItem.get(Constants.KEY_PET_SEX);
			String species = (String) listItem.get(Constants.KEY_PET_SPECIES);
			
			viewHolder.petId = Integer.parseInt(sItemId);
			viewHolder.tvPetName.setText(sName);
			viewHolder.tvPetBday.setText(bDay);
			viewHolder.tvPetBreed.setText(breed);
			viewHolder.tvPetSpecies.setText(species ); 
			viewHolder.tvPetSex.setText("(" + sex + ")");
			
			if (sImgURL!= null && !sImgURL.startsWith("http")){
				sImgURL = Constants.URL_CLOUD_STORAGE + sImgURL;
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
		 * return a pet ID from view selected, if item id is not 
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
