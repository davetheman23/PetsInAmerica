package net.petsinamerica.askavet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This fragment class uses the FragmentManager to swap the two child fragments
 * in and out of the context. Comparing to EnquiryListFragment2, this way do not 
 * require managing all the private variables in the BaseListFragment class. 
 * If more fragments are needed to be swapped in and out at will, this method of 
 * coding is better. 
 * 
 * @author David Zeng
 * @since 6/8/2014
 */

public class EnquiryListFragment1 extends Fragment {

	private static Context mContext;
	private Button mBtnAsk;
	private Button mBtnMyQuery;
	private Button mBtnAllQuery;
	private TextView mTvEmpty;
	private ProgressBar mProgBar;
	
	private BaseListFragment mAllListFragment = null;
	private BaseListFragment mMyListFragment = null;
	private boolean firstAdd = true; 
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;		
		
		// creating the two fragments and keep them in memory for fast swapping
		mAllListFragment = new AllEnquiryListFragment();
		
		mMyListFragment = new MyEnquiryListFragment();
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View rootView = inflater.inflate(R.layout.fragment_enquirylist,container,false);

		// a fragment only needs to be added once, then it can be replaced
		if (firstAdd){
			getChildFragmentManager().beginTransaction()
				.add(R.id.frag_enquirylist_listframe, mAllListFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
			firstAdd = false;
		}else{
			getChildFragmentManager().beginTransaction()
				.replace(R.id.frag_enquirylist_listframe, mAllListFragment)
				.addToBackStack(null)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
		}
		
		
		// set up all query button, and listener load new list if necessary
		mBtnAllQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_allquery);
		mBtnAllQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getChildFragmentManager()
					.beginTransaction()
					.replace(R.id.frag_enquirylist_listframe, mAllListFragment)
					.addToBackStack(null)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
				
			}
		});
		
		
		// set up my query button, and listener load new list if necessary
		mBtnMyQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_myquery);
		mBtnMyQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getChildFragmentManager()
					.beginTransaction()
					.replace(R.id.frag_enquirylist_listframe, mMyListFragment)
					.addToBackStack(null)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
				
			}
		});
		
		// set up the ask button and bring up the ask activity
		mBtnAsk = (Button) rootView.findViewById(R.id.frag_enquirylist_ask);
		mBtnAsk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), EnquiryFormActivity.class);
				startActivity(intent);
			}
		});
		
		// get a reference to progress bar
		mProgBar = (ProgressBar) rootView.findViewById(R.id.frag_enquirylist_progressbar);
				
		// get a refernce to the textview when no data available to list
		mTvEmpty = (TextView) rootView.findViewById(android.R.id.empty);
		
		return rootView;
	}
	

	
	public static class AllEnquiryListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_ENQUIRY, true, false,true);
			setPage(1);
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new EnquiryListAdapter(mContext, 
					R.layout.list_enquiry_item, emptyList));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			//int articleID = ((ArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			//String enqueryContent = ((EnquiryListAdapter)getListAdapter()).getEnqueryContent(v);
			Intent newIntent = new Intent(mContext, EnquiryActivity.class);
			//newIntent.putExtra(KEY_CONTENT, enqueryContent);
			int queryId = ((EnquiryListAdapter)this.getListAdapter()).getQueryID(v);
			newIntent.putExtra("QueryId", queryId);
			startActivity(newIntent);
		}
	}
	
	public static class MyEnquiryListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_MYENQUIRY,false,false,true);
			setUserDataFlag(true);
			
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new EnquiryListAdapter(mContext, 
					R.layout.list_enquiry_item, emptyList));
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			//int articleID = ((ArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			Intent newIntent = new Intent(mContext, EnquiryActivity.class);
			int queryId = ((EnquiryListAdapter)this.getListAdapter()).getQueryID(v);
			newIntent.putExtra("QueryId", queryId);
			startActivity(newIntent);
			
		}
	}
	
	
}
