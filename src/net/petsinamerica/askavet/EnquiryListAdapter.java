package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.Constants;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/*
 *  listAdapter to display article summaries
 *  layout file list_item.xml 
 */
public class EnquiryListAdapter extends ArrayAdapter<Map<String,Object>> {
	private final Context mContext;
	//private final List<Map<String, String>> mEnquiries;
	private final int mResource;
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	//private static String TAG_IMAGE;
	private static String TAG_ID;
	//private static String TAG_TAG;
	private static String TAG_AVATAR;
	private static String TAG_OWNERNAME;
	private static String TAG_CONTENT;
	private static String TAG_DATE;
	private static String TAG_STATUS;
	
	
	private static class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		TextView tv_status;
		String enQueryContent = "";
		int enqueryID;
		int ownerId;
	}
	
	/*
	 *  Standard constructer
	 */
	public EnquiryListAdapter(Context context, int resource,
			List<Map<String, Object>> objects) {
		super(context, resource, objects);
		
		mContext = context;
		mResource = resource;
		
		TAG_TITLE = mContext.getResources().getString(R.string.JSON_tag_title);
		//TAG_IMAGE = mContext.getResources().getString(R.string.JSON_tag_image);
		TAG_ID = mContext.getResources().getString(R.string.JSON_tag_queryid);
		TAG_OWNERNAME = mContext.getResources().getString(R.string.JSON_tag_ownername);
		TAG_AVATAR = mContext.getResources().getString(R.string.JSON_tag_avatar);
		TAG_DATE = mContext.getResources().getString(R.string.JSON_tag_date);
		TAG_STATUS = mContext.getResources().getString(R.string.JSON_tag_status);
		TAG_CONTENT = mContext.getResources().getString(R.string.JSON_tag_content);
		
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
			viewHolder.iv = (ImageView) rowview.findViewById(R.id.enquiry_list_icon);
			viewHolder.tv_firstline =(TextView) rowview.findViewById(R.id.enquiry_list_1stline);	
			viewHolder.tv_secondline = (TextView) rowview.findViewById(R.id.enquiry_list_2ndline);
			viewHolder.tv_status = (TextView) rowview.findViewById(R.id.enquiry_list_status);
			
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) rowview.getTag();
			//viewHolder.iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.someone));
		}
		
		Map<String,Object> enquiry = getItem(position);
		
		String ownerId = enquiry.get("owner").toString();
		String title = enquiry.get(TAG_TITLE).toString();
		String userAvatarURL = null;
		if (enquiry.containsKey(TAG_AVATAR)){
			userAvatarURL = enquiry.get(TAG_AVATAR).toString();
		}
		String ownerName = null;
		if (enquiry.containsKey(TAG_OWNERNAME)){
			ownerName = enquiry.get(TAG_OWNERNAME).toString();
		}
		String queryID = enquiry.get(TAG_ID).toString();
		String date = enquiry.get(TAG_DATE).toString();
		String content = enquiry.get(TAG_CONTENT).toString();
		int status = Integer.parseInt(enquiry.get(TAG_STATUS).toString());
		int status_color = android.R.color.black;
		String sStatus = null;
		switch (status){
		case Constants.STATUS_ONGOING:
			sStatus = "进行中";
			status_color = android.graphics.Color.GREEN;
			break;
		case Constants.STATUS_UNKNOWN:
			sStatus = "未知状态";
			status_color = android.R.color.darker_gray;
			break;
		case Constants.STATUS_NEWANSWER:
			sStatus = "新回答";
			status_color = android.graphics.Color.MAGENTA;
			break;
		case Constants.STATUS_SOLVED:
			sStatus = "已解决";
			status_color = android.graphics.Color.BLUE;
			break;
		case Constants.STATUS_CLOSED:
			sStatus = "已关闭";
			status_color = android.graphics.Color.BLACK;
			break;
		}
		
		viewHolder.ownerId = Integer.parseInt(ownerId);
		viewHolder.enqueryID = Integer.parseInt(queryID);
		viewHolder.enQueryContent = content;
		viewHolder.tv_firstline.setText(title);
		viewHolder.tv_secondline.setText(ownerName + " 提问于 " + date);
		viewHolder.tv_status.setText(sStatus);
		viewHolder.tv_status.setTextColor(status_color);
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 
		if (userAvatarURL != null && !userAvatarURL.contains("http")){
			userAvatarURL = Constants.URL_CLOUD_STORAGE + userAvatarURL;
		}
		Picasso.with(mContext)
				.load(userAvatarURL)
				.placeholder(R.drawable.someone)
				.resize(70, 70)
				.into(viewHolder.iv);
		
		if (userAvatarURL ==null || userAvatarURL.endsWith("someone.png")){			
			// cancel request when download is not needed
			Picasso.with(mContext)
				.cancelRequest(viewHolder.iv);
		}

		return rowview;
	}
	
	/**
	 * return a enquiry ID from view selected
	 * @param v view of a listview item
	 */
	public int getQueryID(View v) {
		// this assumes the view is the row view so it has a viewholder
		ViewHolder vh = (ViewHolder) v.getTag();

		return vh.enqueryID;
	}
	
	public String getEnqueryContent(View v){
		ViewHolder vh = (ViewHolder) v.getTag();
		return vh.enQueryContent;
	}
	
	/**
	 * Get the id of the owner that posted the enquiry
	 * @param v view of a listview item
	 */
	public int getOwnerId(View v){
		// this assumes the view is the row view so it has a viewholder
		ViewHolder vh = (ViewHolder) v.getTag();
		return vh.ownerId;
	}
	

}
