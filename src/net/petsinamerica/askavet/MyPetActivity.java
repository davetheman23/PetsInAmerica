package net.petsinamerica.askavet;

import java.util.ArrayList;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MyPetActivity extends FragmentActivity{
	
	public FragmentManager fm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_petinfo);
		
		// setup the list fragment that shows a list of all pets of the user		
		PetListFragment petListFragment = new PetListFragment();
		
		fm = getSupportFragmentManager(); 
		fm.beginTransaction()
			.add(R.id.activity_petinfo_container, petListFragment)
			.commit();
		
	}

	
	
	
	public static class PetListFragment extends BaseListFragment{
		
		/*OnPetItemSelectedListener mCallback;*/
		
		PetListAdapter2 adapter;
		
		/*// a map to retain the reference of all the detail fragments
		// that are ever been created
		Map<Integer, PetDetailFragment> petDetails = 
							new HashMap<Integer, MyPetActivity.PetDetailFragment>();*/
		
		// Container Activity must implement this interface
	    /*public interface OnPetItemSelectedListener {
	        	public void onPetSelected(int position);
	    }*/
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			/*// This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (OnPetItemSelectedListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnPetItemSelectedListener");
	        }*/
			
			setParameters(Constants.URL_USERPETS,false,true,true);
			setPage(Integer.parseInt(UserInfoManager.userid));
			setUserDataFlag(true);
			
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			adapter = new PetListAdapter2(this.getActivity(), 
						R.layout.list_pet_item, emptyList);
			setCustomAdapter(adapter);
		}
		


		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setStyle(Style.card);
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			if (position != getListView().getCount()-1){
				// if not the footer clicked
				int petId = adapter.getPetId(v);			
				Intent intent = new Intent(getActivity(), MyPetDetailsActivity.class);
				intent.putExtra(Constants.KEY_PET_ID, petId);
				startActivity(intent);			
			}else{
				// if footer is clicked
				/*Intent intent = new Intent(getActivity(), MyPetDetailsActivity.class);
				startActivity(intent);*/
				Toast.makeText(getActivity(), "功能还在完善中，暂时仅支持网上添加宠物", Toast.LENGTH_LONG).show();;
			}
			return;
		}

		@Override
		protected void setUpFooterView() {
			if (mfooterview != null){
				getListView().removeFooterView(mfooterview);
				mfooterview = null;
			}
			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View footer = (View) inflater.inflate(R.layout.list_pet_item_addpet, null);
			getListView().addFooterView(footer);
			getListView().setFooterDividersEnabled(true);
			// set the footer as invisible only make it visible when needed
			footer.setVisibility(View.VISIBLE);
			
		}
		
		@Override
		protected void handleEmptyList() {
		}



		@Override
		protected void handleEndofList() {
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
