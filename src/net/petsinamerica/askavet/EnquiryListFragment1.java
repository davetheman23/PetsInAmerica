package net.petsinamerica.askavet;

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
	
	private static String sTAG_CONTENT;
	private static String sTAG_RESULT;
	private static final String sTAG = "EnquiryListFragment";

	private static Context mContext;
	private Button mBtnMyQuery;
	private Button mBtnAllQuery;
	private TextView mTvEmpty;
	private ProgressBar mProgBar;
	
	private BaseListFragment mAllListFragment = null;
	private BaseListFragment mMyListFragment = null;
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		sTAG_CONTENT = mContext.getResources().getString(R.string.JSON_tag_content);
		sTAG_RESULT = getResources().getString(R.string.JSON_tag_result);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View rootView = inflater.inflate(R.layout.fragment_enquirylist,container,false);

		
		if (mAllListFragment == null){
			mAllListFragment = new AllEnquiryListFragment();
		}
		if (mMyListFragment == null){
			mMyListFragment = new MyEnquiryListFragment();
		}
		
		getFragmentManager().beginTransaction()
			.add(R.id.frag_enquirylist_listframe, mAllListFragment)
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			.commit();
		
		
		// set up all query button, and listener load new list if necessary
		mBtnAllQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_allquery);
		mBtnAllQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager()
					.beginTransaction()
					.replace(R.id.frag_enquirylist_listframe, mAllListFragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			}
		});
		
		
		// set up my query button, and listener load new list if necessary
		mBtnMyQuery = (Button) rootView.findViewById(R.id.frag_enquirylist_myquery);
		mBtnMyQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				getFragmentManager()
					.beginTransaction()
					.replace(R.id.frag_enquirylist_listframe, mMyListFragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			}
		});
		
		// get a reference to progress bar
		mProgBar = (ProgressBar) rootView.findViewById(R.id.frag_enquirylist_progressbar);
				
		// get a refernce to the textview when no data available to list
		mTvEmpty = (TextView) rootView.findViewById(android.R.id.empty);
		
		return rootView;
	}
	

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		// since every time onCreateView will add a new fragment to the parent 
		// the fragments have to be removed when view is destroyed
		getFragmentManager()
			.beginTransaction()
			.remove(mAllListFragment)
			.remove(mMyListFragment)
			.commit();
		
	}

	
	public static class AllEnquiryListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_ENQUIRY, sTAG_RESULT,true);
		}

		@Override
		protected void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray) {
			setCustomAdapter(new EnquiryListAdapter(mContext,
					R.layout.enquiry_list_item, resultArray));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			//int articleID = ((ArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			String enqueryContent = ((EnquiryListAdapter)getListAdapter()).getEnqueryContent(v);
			Intent newIntent = new Intent(mContext, EnquiryActivity.class);
			newIntent.putExtra(sTAG_CONTENT, enqueryContent);
			startActivity(newIntent);
		}
	}
	
	public static class MyEnquiryListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			setParameters(Constants.URL_MYENQUIRY, sTAG_RESULT,true);
			setUserDataFlag(true);
		}

		@Override
		protected void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray) {
			setCustomAdapter(new EnquiryListAdapter(mContext,
					R.layout.enquiry_list_item, resultArray));
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			//int articleID = ((ArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// store the article ID clicked
			//Record_Usage(articleID);
			
			String enqueryContent = ((EnquiryListAdapter)getListAdapter()).getEnqueryContent(v);
			Intent newIntent = new Intent(mContext, EnquiryActivity.class);
			newIntent.putExtra(sTAG_CONTENT, enqueryContent);
			startActivity(newIntent);
			
		}
	}
	
	
}
