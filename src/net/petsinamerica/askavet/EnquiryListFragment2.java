package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * This fragment class manages two sets of variables, including page number, 
 * and adapter variables. So when user requests for different kinds of enquiry
 * lists, swapping adapter to the existing listview is all that is needed.  
 * It adds overhead to BaselistFragment, by requiring the setter and getter methods
 * for each private variables in the BaseListFragment class.
 * 
 * @author David Zeng
 * @since 6/8/2014
 */
public class EnquiryListFragment2 extends BaseListFragment {

	public static enum EnquiryType{
		ALL_ENQUIRY,
		MY_ENQUIRY
	};
	
	private static String sTAG_CONTENT;
	private static String sTAG_RESULT;
	private static final String sTAG = "EnquiryListFragment";
	
	/*
	 * default URL type, use setUrl() to change type
	 */
	private EnquiryType mEnquiryType = EnquiryType.ALL_ENQUIRY;		
	
	
	EnquiryListAdapter mELAdapter;
	private static Context mContext;
	private View mRootView = null;
	private Button mBtnMyQuery;
	private Button mBtnAllQuery;
	private TextView mTvEmpty;
	private ProgressBar mProgBar;
	
	
	
	private int mPageMyEnqury = 1;				// page number for list
	private int mPageAllEnqury = 1;				// page number for list
	private ArrayAdapter<Map<String, Object>> mMyEnquiryAdapter = null;
	private ArrayAdapter<Map<String, Object>> mAllEnquiryAdapter = null;
	

	@Override
	protected void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray) {
		EnquiryListAdapter adapter = new EnquiryListAdapter(mContext,
								R.layout.list_enquiry_item, resultArray);
		setCustomAdapter(adapter);
		
		// keep two instances of the adapter, so that the showing of enquiry
		// lists can be swapped at ease
		if (mEnquiryType == EnquiryType.MY_ENQUIRY){
			mMyEnquiryAdapter = adapter;
		}else if(mEnquiryType == EnquiryType.ALL_ENQUIRY){
			mAllEnquiryAdapter = adapter;
		}
	}

	@Override
	protected void onItemClickAction(View v, int position, long id) {
		
		String enqueryContent = ((EnquiryListAdapter)getListAdapter()).getEnqueryContent(v);
		Intent newIntent = new Intent(mContext, EnquiryActivity.class);
		newIntent.putExtra(sTAG_CONTENT, enqueryContent);
		startActivity(newIntent);
		
	}
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		sTAG_CONTENT = mContext.getResources().getString(R.string.JSON_tag_content);
		sTAG_RESULT = getResources().getString(R.string.JSON_tag_result);
		
		setParameters(Constants.URL_ENQUIRY, sTAG_RESULT,true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRootView = inflater.inflate(R.layout.fragment_enquirylist,container,false);
		

		// set up all query button, and listener load new list if necessary
		mBtnAllQuery = (Button) mRootView.findViewById(R.id.frag_enquirylist_allquery);
		mBtnAllQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEnquiryType(EnquiryType.ALL_ENQUIRY);
			}
		});

		// set up my query button, and listener load new list if necessary
		mBtnMyQuery = (Button) mRootView.findViewById(R.id.frag_enquirylist_myquery);
		mBtnMyQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEnquiryType(EnquiryType.MY_ENQUIRY);				
			}
		});
		
		// get a reference to progress bar
		//mProgBar = (ProgressBar) mRootView.findViewById(R.id.frag_enquirylist_progressbar);
				
		// get a refernce to the textview when no data available to list
		//mTvEmpty = (TextView) mRootView.findViewById(android.R.id.empty);
		
		return mRootView;
	}

	/**
	 *  Determine which query list to show, either 
	 */
	public void setEnquiryType(EnquiryType enquiryType){
		
		if (enquiryType == EnquiryType.ALL_ENQUIRY){
			setParameters(Constants.URL_ENQUIRY, sTAG_RESULT,true);
			setUserDataFlag(false);
			mPageMyEnqury = getPage();
			setPage(mPageAllEnqury);
			if (mAllEnquiryAdapter != null){
				setCustomAdapter(mAllEnquiryAdapter);
			}else{
				loadListInBackground();
			}
		}else if (enquiryType == EnquiryType.MY_ENQUIRY){
			setParameters(Constants.URL_MYENQUIRY, sTAG_RESULT,true);
			setUserDataFlag(true);
			mPageAllEnqury = getPage();
			setPage(mPageMyEnqury);
			if (mMyEnquiryAdapter != null){
				setCustomAdapter(mMyEnquiryAdapter);
			}else{
				loadListInBackground();
			}
		}
		mEnquiryType = enquiryType;
	}

	
}
