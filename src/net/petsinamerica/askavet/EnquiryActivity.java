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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class EnquiryActivity extends FragmentActivity {

	/*private static TextView tv_petName;
	private static TextView tv_petSpecies;
	private static TextView tv_petBreed;
	private static TextView tv_petAge;
	private static TextView tv_petNeuterAge;
	private static TextView tv_petSex;
	private static ImageView iv_petPic;
	
	TextView tv_title;
	private static TextView tv_petDiet;
	private static TextView tv_petDietType;
	private static TextView tv_petWeight;
	private static TextView tv_petResponsiveness;
	private static TextView tv_petStool;
	private static TextView tv_petAppetite;
	
	private static TextView tv_author;
	private static TextView tv_authordate;
	private static TextView tv_enquirydetails;
	private static ImageView iv_authorPic;*/
	
	private static ProgressBar progressbar;
	
	/*private static FragmentManager fm;
	private static EnquiryDetailFragment enquiryfrag;*/
	
	/*private static List<Map<String,Object>> enquiryResult = new ArrayList<Map<String,Object>>();
	
	private static EnquiryDetailListAdapter detailList = null;*/
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_detail);
		
		//enquiryfrag = new EnquiryDetailFragment();
		
		/*fm = getSupportFragmentManager();
		
		fm.beginTransaction()
			.add(R.id.activity_enquiry_details_content_container, enquiryfrag)
			.commit();*/
		//mContext = getApplicationContext();
		
		// get query id from the extra that was set when the activity was started
		int queryId = getIntent().getIntExtra("QueryId", 0);
		//queryId = 1439;
				
		EnquiryDetailFragment enquiryfrag = new EnquiryDetailFragment();
		
		enquiryfrag.setParameters(queryId);
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.activity_enquiry_details_content_container, enquiryfrag)
			.commit();
		
		
		
		
		
		progressbar = (ProgressBar) findViewById(R.id.activity_enquiry_details_load_progressbar);
		progressbar.setVisibility(View.VISIBLE);
		
		
		
		/*if (queryId != 0){
			String queryURL_API = Constants.URL_ENQUIRY_DETAILS + Integer.toString(queryId);
			GetEnquiryInBackground getEuquiry = new GetEnquiryInBackground();
			getEuquiry.setResultType(CallPiaApiInBackground.Type.list);
			getEuquiry.execute(queryURL_API);
			
			Toast.makeText(this, "页面设计还未完成", Toast.LENGTH_LONG).show();
		}else{
			//TODO notify the user
		}
		*/
	}
	
	
	
}
