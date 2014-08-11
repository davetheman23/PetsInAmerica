package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.GeneralHelpers.CallPiaApiInBackground;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EnquiryActivity extends FragmentActivity {

	private static TextView tv_petName;
	private static TextView tv_petDiet;
	private static TextView tv_petDietType;
	private static TextView tv_petWeight;
	private static TextView tv_petBreed;
	private static TextView tv_petAge;
	private static TextView tv_petNeuterAge;
	private static TextView tv_petResponsiveness;
	private static TextView tv_petStool;
	private static TextView tv_petDesire;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_detail);
		
		EnquiryDetailFragment enquiryfrag = new EnquiryDetailFragment();
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_enquiry_details_content_container, enquiryfrag)
			.commit();
		
		// get query id from the extra that was set when the activity was started
		int queryId = getIntent().getIntExtra("QueryId", 0);
		//queryId = 1439;
		if (queryId != 0){
			String queryURL_API = Constants.URL_ENQUIRY_DETAILS + Integer.toString(queryId);
			GetEnquiryInBackground getEuquiry = new GetEnquiryInBackground();
			getEuquiry.setResultType(CallPiaApiInBackground.Type.list);
			getEuquiry.execute(queryURL_API);
			
			Toast.makeText(this, "页面设计还未完成", Toast.LENGTH_LONG).show();
		}else{
			//TODO notify the user
		}
		
	}
	
	public static class EnquiryDetailFragment extends ListFragment{

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_enquirydetails, null, false);
			
			tv_petName = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petName);
			tv_petDiet = (TextView) rootView.findViewById(R.id.frag_enquiry_details_diet);
			tv_petDietType = (TextView) rootView.findViewById(R.id.frag_enquiry_details_diet_type);
			tv_petWeight = (TextView) rootView.findViewById(R.id.frag_enquiry_details_weight);
			tv_petBreed = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petbreed);
			tv_petAge = (TextView) rootView.findViewById(R.id.frag_enquiry_details_pet_age);
			tv_petNeuterAge = (TextView) rootView.findViewById(R.id.frag_enquiry_details_pet_neuterage);
			tv_petResponsiveness = (TextView) rootView.findViewById(R.id.frag_enquiry_details_responsiveness);
			tv_petStool = (TextView) rootView.findViewById(R.id.frag_enquiry_details_stool);
			tv_petDesire = (TextView) rootView.findViewById(R.id.frag_enquiry_details_desire);
			
			return rootView;
		}
		
		
		
	}
	
	class GetEnquiryInBackground extends GeneralHelpers.CallPiaApiInBackground{
		
		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			// get the query information
			if (result != null){
				int i = 0;
				i = i + 1;
			}
		}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
			// get the query information
			if (result != null){
				int i = 0;
				i = i + 1;
			}
		}
	}
	
	
	
	
}
