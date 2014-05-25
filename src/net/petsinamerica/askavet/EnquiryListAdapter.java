package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.DownLoadImageTask;
import net.petsinamerica.askavet.utils.MemoryCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 *  listAdapter to display article summaries
 *  layout file list_item.xml 
 */
public class EnquiryListAdapter extends ArrayAdapter<Map<String,String>> {
	private final Context mContext;
	private final List<Map<String, String>> mEnquiries;
	private final int mResource;
	
	private MemoryCache mMemCache;
	private AttributeSet mAttributes;
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	private static String TAG_IMAGE;
	private static String TAG_ID;
	private static String TAG_TAG;
	private static String TAG_AVATAR;
	private static String TAG_OWNERNAME;
	private static String TAG_CONTENT;
	private static String TAG_DATE;
	private static String TAG_STATUS;
	
	
	static class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		TextView tv_status;
		String enQueryContent = "";
		int enqueryID;
	}
	
	/*
	 *  Standard constructer
	 */
	public EnquiryListAdapter(Context context, int resource,
			List<Map<String, String>> objects) {
		super(context, resource, objects);
		
		mContext = context;
		mEnquiries = objects;
		mResource = resource;
		mMemCache = new MemoryCache();	// set aside some cache memory to store bitmaps, for fast loading of image
		
		TAG_TITLE = mContext.getResources().getString(R.string.JSON_tag_title);
		TAG_IMAGE = mContext.getResources().getString(R.string.JSON_tag_image);
		TAG_ID = mContext.getResources().getString(R.string.JSON_tag_id);
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
		}
		
		
		Map<String,String> enquiry = mEnquiries.get(position); 
		String title = enquiry.get(TAG_TITLE);
		String userAvatarURL = enquiry.get(TAG_AVATAR);
		String ownerName = enquiry.get(TAG_OWNERNAME);
		String queryID = enquiry.get(TAG_ID);
		String date = enquiry.get(TAG_DATE);
		String content = enquiry.get(TAG_CONTENT);
		int status = Integer.parseInt(enquiry.get(TAG_STATUS));
		int status_color = android.R.color.black;
		// TODO need to convert these hard-coded strings 
		String sStatus = null;
		switch (status){
		case 0:
			sStatus = "进行中";
			status_color = android.graphics.Color.GREEN;
			break;
		case 1:
			sStatus = "未知状态";
			status_color = android.R.color.darker_gray;
			break;
		case 2:
			sStatus = "新回答";
			status_color = android.graphics.Color.MAGENTA;
			break;
		case 3:
			sStatus = "已解决";
			status_color = android.graphics.Color.BLUE;
			break;
		}
		
		viewHolder.enqueryID = Integer.parseInt(queryID);
		viewHolder.enQueryContent = content;
		viewHolder.tv_firstline.setText(title);
		viewHolder.tv_secondline.setText(ownerName + " 提问于 " + date);
		viewHolder.tv_status.setText(sStatus);
		viewHolder.tv_status.setTextColor(status_color);
		
		//String urlPattern = "(http.*/)(.*?)(\\.[jp][pn]g)";
		/*String avatarFile = userAvatarURL.replaceAll(urlPattern, "$2");
		
		if (!avatarFile.equals("someone")){
			final Bitmap bitmap = mMemCache.getBitmapFromMemCache(userAvatarURL);	// try first see if image in cache
			if (bitmap != null){
				// if in cache, display immediately
				viewHolder.iv.setImageBitmap(bitmap);
			}else{
				// if not in cache, setup an async task to download from web
				viewHolder.iv.setTag(userAvatarURL);
				DownLoadImageTask  loadimage = new DownLoadImageTask();
				int scale = 4;
				loadimage.SetMemCache(mMemCache, scale);
				loadimage.execute(viewHolder.iv);
			}
		}*/
		
		return rowview;
	}
	
	/*
	 * return a article ID from view selected
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
	

}
