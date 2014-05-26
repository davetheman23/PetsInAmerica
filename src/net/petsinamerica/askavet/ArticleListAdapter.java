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

/*
 *  listAdapter to display article summaries
 *  layout file list_item.xml 
 */
public class ArticleListAdapter extends ArrayAdapter<Map<String, Object>> {
	private final Context mContext;
	//private final List<Map<String, Object>> mArticleSummaries;
	private final int mResource;
	private final int mHeader;
	private final static int HEADER_POSITION  = 0;
	private final static int LIST_VIEW_TYPE_HEADER = 0;
	private final static int LIST_VIEW_TYPE_REGULAR = 1;
	private final static int MAX_NUM_TAGS_DISPLAY = 5;
	
	private AttributeSet mAttributes;
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE;
	private static String TAG_IMAGE;
	private static String TAG_ID;
	private static String TAG_TAGS;
	
	private class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		//TextView tv_secondline;
		TextView[] tv_tags;
		LinearLayout linearLayout;
		int articleID;
	}
	
	/*
	 *  Standard constructer
	 */
	public ArticleListAdapter(Context context, int header, int resource,
			List<Map<String, Object>> objects) {
		super(context,  header, resource, objects);
		
		mContext = context;
		//mArticleSummaries = objects;
		mHeader = header;
		mResource = resource;
		mAttributes = getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
		TAG_TITLE = mContext.getResources().getString(R.string.JSON_tag_title);
		TAG_IMAGE = mContext.getResources().getString(R.string.JSON_tag_image);
		TAG_ID = mContext.getResources().getString(R.string.JSON_tag_id);
		TAG_TAGS = mContext.getResources().getString(R.string.JSON_tag_tags);
		
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
			if (getItemViewType(position) == LIST_VIEW_TYPE_HEADER){
				rowview = inflater.inflate(mHeader, parent, false);
				viewHolder.iv = (ImageView) rowview.findViewById(R.id.list_header_icon);
				viewHolder.tv_firstline = (TextView) rowview.findViewById(R.id.list_header_title);
			}else{
				rowview = inflater.inflate(mResource, parent, false);
				viewHolder.iv = (ImageView) rowview.findViewById(R.id.list_icon);
				viewHolder.tv_firstline =(TextView) rowview.findViewById(R.id.list_firstLine);	
				// create several textviews, all inside the linear layout view 
				// for each textview, can set an onclicklistener too
				viewHolder.linearLayout = (LinearLayout) rowview.findViewById(R.id.list_secondline);
				viewHolder.tv_tags = new TextView[MAX_NUM_TAGS_DISPLAY];
				for (int tagidx = 0; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
					// create a textview from a template file, which attribute is obtained in the constructor
					viewHolder.tv_tags[tagidx] = new TextView(mContext, mAttributes);
					viewHolder.linearLayout.addView(viewHolder.tv_tags[tagidx]);
				}
			}
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{

			viewHolder = (ViewHolder) rowview.getTag();
		}
		
		Map<String, Object> articlesummary = getItem(position); 
		String sTitle,sImgURL, sArticleID;
		sTitle = (String) articlesummary.get(TAG_TITLE);
		sTitle = sTitle.trim();
		sImgURL = (String) articlesummary.get(TAG_IMAGE);
		sArticleID = (String) articlesummary.get(TAG_ID);
		String[] tags_cn = getCnTags(articlesummary.get(TAG_TAGS));
		
		viewHolder.articleID = Integer.parseInt(sArticleID);
		viewHolder.tv_firstline.setText(sTitle);
		//if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
		//	viewHolder.tv_secondline.setText("作者：" + sOwner);
		//}
		
		// adding tags to in the second line of the view
		int tagstoshow = (tags_cn.length) < MAX_NUM_TAGS_DISPLAY 
				         ? (tags_cn.length) : MAX_NUM_TAGS_DISPLAY;
		if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
			for (int tagidx = 1; tagidx < tagstoshow; tagidx++){
				viewHolder.tv_tags[tagidx].setVisibility(View.VISIBLE);
				viewHolder.tv_tags[tagidx].setText("["+tags_cn[tagidx]+"]");
			}
			for (int tagidx = tagstoshow; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
				viewHolder.tv_tags[tagidx].setVisibility(View.INVISIBLE);
			}
		}
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 
		if (getItemViewType(position) == LIST_VIEW_TYPE_HEADER){
			Picasso.with(mContext)
				.load(sImgURL)
				.into(viewHolder.iv);
		}else if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
			Picasso.with(mContext)
				.load(sImgURL)
				.placeholder(R.drawable.ic_launcher)
				.resize(150, 120)
				.into(viewHolder.iv);
		}

		return rowview;
		
	}
	
	private AttributeSet getAttributeSet(Context context, int layoutResource, String ViewName){
		// function to fetch the attribute set defined in layoutResource element ViewName
		AttributeSet Attributes = null;
		XmlPullParser parser = context.getResources().getLayout(layoutResource);
		int state = 0;
	    do {
	        try {
	            state = parser.next();
	        } catch (XmlPullParserException e1) {
	            e1.printStackTrace();
	        } catch (IOException e1) {
	            e1.printStackTrace();
	        }       
	        if (state == XmlPullParser.START_TAG) {
	            if (parser.getName().equals(ViewName)) {
	            	Attributes = Xml.asAttributeSet(parser);
	                break;
	            }
	        }
	    } while(state != XmlPullParser.END_DOCUMENT);
	    return Attributes;
	}
	
	@Override
	public int getItemViewType(int position) {
		// determine what the item view type is
		// in this case, header or regular
		return position == HEADER_POSITION ? LIST_VIEW_TYPE_HEADER : LIST_VIEW_TYPE_REGULAR;
	}

	@Override
	public int getViewTypeCount() {
		// tell the adapter there are two types of layouts
		return 2;
	}

	/*
	 * return a article ID from view selected
	 */
	public int getArticleID(View v) {
		// this assumes the view is the row view so it has a viewholder
		ViewHolder vh = (ViewHolder) v.getTag();

		return vh.articleID;
	}
	
	/**
	 * Get Chinese tags for each article
	 * @param object
	 * @return
	 */
	private String[] getCnTags(Object object){
		String[] tagArray = null;
		
		try{
			JSONArray jsonArray = (JSONArray)JsonHelper.toJSON(object);
			int numTags = jsonArray.length();
			tagArray = new String[numTags];
			JSONObject jsonObject;
			for (int i = 0; i < numTags; i++){
				jsonObject = (JSONObject) jsonArray.get(i);
				tagArray[i] = jsonObject.getString("cn");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return tagArray;
	}

}
