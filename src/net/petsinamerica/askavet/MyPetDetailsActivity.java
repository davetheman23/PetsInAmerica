package net.petsinamerica.askavet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MyPetDetailsActivity extends FragmentActivity {

	private int mPetId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_petinfo);
		
		mPetId = getIntent().getIntExtra(Constants.KEY_PET_ID, -1);
		if (mPetId == -1){
			// TODO report problem
			return;
		}
		
		PetDetailFragment petDetailFragment ;
		// setup the list fragment that shows a list of all pets of the user
		petDetailFragment = new PetDetailFragment();
		petDetailFragment.setPetId(mPetId);
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_petinfo_container, petDetailFragment)
			.commit();
		
		
	}
	
	
	public static class PetDetailFragment extends ListFragment{
		
		private static final int NOT_INITIALIZED = -1;
		
		TextView tvPetName;
		TextView tvPetSex;
		TextView tvPetBreed;
		TextView tvPetSpecies;
		TextView tvPetBDay;
		TextView tvPetNeuterAge;
		TextView tvPetInsurance;
		TextView tvPetDeworm;
		TextView tvPetFlea;
		TextView tvPetVaccination;
		
		ImageView ivPetAvatar;
		
		
		private int mPetid = NOT_INITIALIZED;
		
		private GetPetInfoTask getPetInfoTask;
		
		MyPetQueryListFragment myPetQueryListFragment;
		
		static PetDetailFragment mContext;
		
		/**
		 * Set Pet ID immediately after created a new instance of the class
		 * This is a required parameter. 
		 * @param id	the pet id to be set
		 */
		public void setPetId(int id){
			mPetid = id;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootview = inflater.inflate(R.layout.fragment_petdetails, container,false);
			
			// reference all views 
			tvPetName = (TextView) rootview.findViewById(R.id.frag_pet_details_petName);
			tvPetSex = (TextView) rootview.findViewById(R.id.frag_pet_details_petsex);
			tvPetBDay = (TextView) rootview.findViewById(R.id.frag_pet_details_pet_bday);
			tvPetBreed = (TextView) rootview.findViewById(R.id.frag_pet_details_petbreed);
			tvPetNeuterAge = (TextView) rootview.findViewById(R.id.frag_pet_details_pet_neuterage);
			tvPetSpecies = (TextView) rootview.findViewById(R.id.frag_pet_details_petspecies);
			tvPetInsurance = (TextView) rootview.findViewById(R.id.frag_pet_details_petinsurance);
			tvPetDeworm = (TextView) rootview.findViewById(R.id.frag_pet_details_deworm);
			tvPetFlea = (TextView) rootview.findViewById(R.id.frag_pet_details_flea);
			tvPetVaccination = (TextView) rootview.findViewById(R.id.frag_pet_details_vaccination);
			ivPetAvatar = (ImageView) rootview.findViewById(R.id.frag_pet_details_pet_pic);
			
			// this has to be initialized first 
			if (mPetid != NOT_INITIALIZED){
				getPetInfoTask = (GetPetInfoTask) new GetPetInfoTask()
					.setParameters(getActivity(), CallPiaApiInBackground.TYPE_RETURN_MAP, true)
					.setErrorDialog(true)
					.execute(Constants.URL_PETINFO + mPetid);
			}
			
			myPetQueryListFragment = new MyPetQueryListFragment();
			myPetQueryListFragment.setParameters(Constants.URL_PET_ENQURIES, false, true, false, true, false);
			myPetQueryListFragment.setPage(mPetid);	
			
			getChildFragmentManager().beginTransaction()
				.add(myPetQueryListFragment, "petQueryList")
				.commit();
			
			mContext = this;

			return rootview;
		}
		
		@Override
		public void onDestroyView() {
			if (getPetInfoTask!= null){
				getPetInfoTask.cancel(true);
			}
			super.onDestroyView();
		}
		
		private class GetPetInfoTask extends CallPiaApiInBackground{

			@Override
			protected void onCallCompleted(List<Map<String, Object>> result) {}
			
			@Override
			protected void onCallCompleted(Map<String, Object> result) {
				
				if (result != null && !result.containsKey(Constants.KEY_ERROR_MESSAGE)){
					String petName = result.get(Constants.KEY_PET_NAME).toString();
					String petSex = result.get(Constants.KEY_PET_SEX).toString();
					String petBDay = result.get(Constants.KEY_PET_BDAY).toString();
					String petBreed = result.get(Constants.KEY_PET_BREED).toString();
					String petNeuterAge = result.get(Constants.KEY_PET_NEUTERAGE).toString();
					String petInsurance = result.get(Constants.KEY_PET_INSURANCE).toString();
					String petSpecies = result.get(Constants.KEY_PET_SPECIES).toString();
					String petFlea = result.get(Constants.KEY_PET_FLEA).toString();
					String petVaccination = result.get(Constants.KEY_PET_VACCINATION).toString();
					String petDeworm = result.get(Constants.KEY_PET_DEWORM).toString();
					String petImageUrl = result.get(Constants.KEY_PET_PIC).toString();
					if (petImageUrl!= null && !petImageUrl.startsWith("http")){
						petImageUrl = Constants.URL_CLOUD_STORAGE + petImageUrl;
					}
					
					tvPetName.setText(petName);
					tvPetSex.setText("(" + petSex + ")");
					tvPetBDay.setText(petBDay);
					tvPetBreed.setText(petBreed);
					tvPetNeuterAge.setText(petNeuterAge + " 个月");
					tvPetSpecies.setText(petSpecies);
					tvPetInsurance.setText(petInsurance);
					tvPetFlea.setText(petFlea);
					tvPetVaccination.setText(petVaccination);
					tvPetDeworm.setText(petDeworm);
					
					// load the data into the image view
					Picasso.with(App.appContext)
						   .load(petImageUrl)
						   .into(ivPetAvatar);
				}else{
					// maybe do something here
				}
			}
		}
		
		public static class MyPetQueryListFragment extends BaseListFragment {
			
			@Override
			public void onAttach(Activity activity) {
				super.onAttach(activity);			
				List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
				setCustomAdapter(new EnquiryListAdapter(getActivity(), 
						R.layout.list_enquiry_item, emptyList));
			}


			@Override
			protected void onHttpSuccess(List<Map<String, Object>> resultArray) {
				if (mContext != null){
					mContext.setListAdapter(getListAdapter());
				}
			}


			@Override
			protected void onItemClickAction(View v, int position, long id) {
			}
			
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			Intent newIntent = new Intent(getActivity(), EnquiryActivity.class);
			int queryId = ((EnquiryListAdapter)this.getListAdapter()).getQueryID(v);
			newIntent.putExtra(Constants.KEY_QUERYID, queryId);
			startActivity(newIntent);
		}
		
		
	}
	
	
}
