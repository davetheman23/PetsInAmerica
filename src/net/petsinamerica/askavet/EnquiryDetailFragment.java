package net.petsinamerica.askavet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.GeneralHelpers.CallPiaApiInBackground;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class EnquiryDetailFragment extends ListFragment{
	
	private static TextView tv_petName;
	private static TextView tv_petSpecies;
	private static TextView tv_petBreed;
	private static TextView tv_petAge;
	private static TextView tv_petNeuterAge;
	private static TextView tv_petSex;
	private static ImageView iv_petPic;
	
	private TextView tv_title;
	private TextView tv_petDiet;
	private TextView tv_petDietType;
	private TextView tv_petWeight;
	private TextView tv_petResponsiveness;
	private TextView tv_petStool;
	private TextView tv_petAppetite;
	
	private TextView tv_author;
	private TextView tv_authordate;
	private TextView tv_enquirydetails;
	private ImageView iv_authorPic;
	
	private int mQueryId;
	
	private List<Map<String,Object>> enquiryResult = new ArrayList<Map<String,Object>>();
	
	private EnquiryDetailListAdapter detailList = null;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	public void setParameters(int queryId){
		mQueryId = queryId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_enquirydetails, null, false);
		
		tv_title = (TextView) rootView.findViewById(R.id.frag_enquiry_details_title);
		tv_petName = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petName);
		tv_petSpecies = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petspecies);
		tv_petBreed = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petbreed);
		tv_petAge = (TextView) rootView.findViewById(R.id.frag_enquiry_details_pet_age);
		tv_petNeuterAge = (TextView) rootView.findViewById(R.id.frag_enquiry_details_pet_neuterage);
		tv_petSex = (TextView) rootView.findViewById(R.id.frag_enquiry_details_petsex);
		iv_petPic = (ImageView) rootView.findViewById(R.id.frag_enquiry_details_pet_pic);
		
		tv_petDiet = (TextView) rootView.findViewById(R.id.frag_enquiry_details_diet);
		tv_petDietType = (TextView) rootView.findViewById(R.id.frag_enquiry_details_diet_type);
		tv_petWeight = (TextView) rootView.findViewById(R.id.frag_enquiry_details_weight);
		tv_petResponsiveness = (TextView) rootView.findViewById(R.id.frag_enquiry_details_responsiveness);
		tv_petStool = (TextView) rootView.findViewById(R.id.frag_enquiry_details_stool);
		tv_petAppetite = (TextView) rootView.findViewById(R.id.frag_enquiry_details_appetite);
		
		tv_author = (TextView) rootView.findViewById(R.id.frag_enquiry_details_author_name);
		tv_authordate = (TextView) rootView.findViewById(R.id.frag_enquiry_details_author_date);
		tv_enquirydetails = (TextView) rootView.findViewById(R.id.frag_enquiry_details_details);
		iv_authorPic = (ImageView) rootView.findViewById(R.id.frag_enquiry_details_author_pic);

		// setup the list adapter
		/*detailList = new EnquiryDetailListAdapter(getActivity(), 
						R.layout.list_enquiry_details_item, enquiryResult);*/
		//setListAdapter(detailList);
		
		// call api in background 
		String queryURL_API = Constants.URL_ENQUIRY_DETAILS + Integer.toString(mQueryId);
		GetEnquiryInBackground getEuquiry = new GetEnquiryInBackground();
		getEuquiry.setResultType(CallPiaApiInBackground.Type.list);
		getEuquiry.execute(queryURL_API);
		
		return rootView;
	}
	
	class GetEnquiryInBackground extends GeneralHelpers.CallPiaApiInBackground{
		
		@Override
		protected void onCallCompleted(Map<String, Object> result) {}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
			// get the query information
			if (result != null){
				Map<String, Object> queryInfo = result.get(0);
				
				//progressbar.setVisibility(View.GONE);
				
				
				int petId = Integer.parseInt(queryInfo.get(Constants.KEY_ENQUIRY_PETID).toString());
				//tv_petName.setText(queryInfo.get(Constants.KEY_TITLE).toString());
				//tv_petBreed.setText(queryInfo.get(Constants.KEY_PET_BREED).toString());
				//tv_petSex.setText(queryInfo.get(Constants.KEY_PET_SEX).toString());
				//tv_petAge.setText(queryInfo.get(Constants.KEY_TITLE).toString());
				//tv_petNeuterAge.setText(queryInfo.get(Constants.KEY_TITLE).toString());
				//tv_petSpecies.setText(queryInfo.get(Constants.KEY_PET_SPECIES).toString());
				
				tv_title.setText(queryInfo.get(Constants.KEY_TITLE).toString());
				tv_petDiet.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_DIETDESCR).toString());
				tv_petDietType.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_DIETTYPE).toString());
				tv_petWeight.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_WEIGHT).toString());
				tv_petStool.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_STOOL).toString());
				tv_petAppetite.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_APPETITE).toString());
				tv_petResponsiveness.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_RESPONSIVE).toString());
				
				tv_author.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORNAME).toString());
				tv_authordate.setText(queryInfo.get("date").toString());
				tv_enquirydetails.setText(Html.fromHtml(
						queryInfo.get(Constants.KEY_CONTENT).toString()));
				String urlAuthorPic = queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORAVATAR).toString();
				Picasso.with(App.appContext)
					   .load(urlAuthorPic)
					   .placeholder(R.drawable.someone)
					   .into(iv_authorPic);
				if (urlAuthorPic ==null || urlAuthorPic.endsWith("someone.png")){			
					// cancel request when download is not needed
					Picasso.with(App.appContext)
						.cancelRequest(iv_authorPic);
				}
				
				result.remove(0);
				enquiryResult = result;
				//detailList.notifyDataSetChanged();
				
				detailList = new EnquiryDetailListAdapter(getActivity(), 
						R.layout.list_enquiry_details_item, result);
				
				setListAdapter(detailList);
			}
		}
	}
}


