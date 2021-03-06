package net.petsinamerica.askavet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;

public class MyEnquiryListActivity extends FragmentActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyEnquiryListFragment myEnquiries = new MyEnquiryListFragment();
		myEnquiries.setParameters(Constants.URL_MYENQUIRY,false,false,false,true,false);
		myEnquiries.setUserDataFlag(true);
		
		getSupportFragmentManager().beginTransaction()
			.add(android.R.id.content, myEnquiries)
			.commit();
	}
	
	public static class MyEnquiryListFragment extends BaseListFragment {
		
		private Context mContext;
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mContext = activity;
			
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new EnquiryListAdapter(mContext, 
					R.layout.list_enquiry_item, emptyList));
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			
			Intent newIntent = new Intent(mContext, EnquiryActivity.class);
			int queryId = ((EnquiryListAdapter)this.getListAdapter()).getQueryID(v);
			int ownerId = ((EnquiryListAdapter)this.getListAdapter()).getOwnerId(v);
			newIntent.putExtra(Constants.KEY_QUERYID, queryId);
			newIntent.putExtra(Constants.KEY_ENQUIRY_OWNERID, ownerId);
			startActivity(newIntent);
			
		}


		@Override
		protected void refreshList(List<Map<String, Object>> result) {
			setListData(result);
			ArrayAdapter<Map<String, Object>> adapter = (ArrayAdapter<Map<String, Object>>) getListAdapter();
			adapter.clear();
			adapter.addAll(result);
		}
		
		

	}

}
