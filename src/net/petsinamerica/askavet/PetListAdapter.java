package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 *  listAdapter to display product list
 *  layout file list_item.xml 
 */
public class PetListAdapter extends ArrayAdapter<Map<String, Object>> {
	private final Context mContext;
	private final int mResource;
	
	// these tags are those for reading the JSON objects
	private static String KEY_AVATAR;
	private static String KEY_ID;
	private static String KEY_NAME;
	
	private class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		int itemId;
	}
	
	/**
	 *  Standard constructer
	 */
	public PetListAdapter(Context context, int resource,
			List<Map<String, Object>> objects) {
		super(context, resource, objects);
		
		mContext = context;
		mResource = resource;
		
		KEY_AVATAR = mContext.getResources().getString(R.string.JSON_tag_petavatar);
		KEY_ID = mContext.getResources().getString(R.string.JSON_tag_id);
		KEY_NAME = mContext.getResources().getString(R.string.JSON_tag_petname);
		
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
			viewHolder.iv = (ImageView) rowview.findViewById(R.id.pet_list_item_image);
			viewHolder.tv_firstline = (TextView) rowview
											.findViewById(R.id.pet_list_item_name);
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{

			viewHolder = (ViewHolder) rowview.getTag();
		}
		
		Map<String, Object> listItem = getItem(position); 
		String sName, sImgURL, sItemId;
		sName = (String) listItem.get(KEY_NAME);
		sName = sName.trim();
		sImgURL = (String) listItem.get(KEY_AVATAR);
		sItemId = (String) listItem.get(KEY_ID);
		
		viewHolder.itemId = Integer.parseInt(sItemId);
		viewHolder.tv_firstline.setText(sName);
		
		if (sImgURL!= null && !sImgURL.startsWith("http")){
			sImgURL = "http://petsinamerica.net/new/../upload/" + sImgURL;
		}
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead
		Picasso.with(mContext)
			.load(sImgURL)
			.placeholder(R.drawable.somepet)
			.into(viewHolder.iv);
		if (sImgURL== null || sImgURL.endsWith("somepet.png")){
			Picasso.with(mContext)
				.cancelRequest(viewHolder.iv);
		}
		return rowview;
		
	}
	
	/**
	 * return a item ID from view selected, if item id is not 
	 * available, -1 will be returned
	 */
	public int getItemID(View v) {
		// this assumes the view is the row view so it has a viewholder
		ViewHolder vh = (ViewHolder) v.getTag();
		if (vh != null){
			return vh.itemId;
		}
		return -1;	
	}
}
