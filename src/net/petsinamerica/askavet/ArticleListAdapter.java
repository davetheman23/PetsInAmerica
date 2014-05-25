package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/*
 *  listAdapter to display article summaries
 *  layout file list_item.xml 
 */
public class ArticleListAdapter extends ArrayAdapter<Map<String,String>> {
	private final Context mContext;
	private final List<Map<String, String>> mArticleSummaries;
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
	private static String TAG_TAG;
	
	static class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		static TextView[] tv_tags;
		RelativeLayout RL_layout;
		int articleID;
	}
	
	/*
	 *  Standard constructer
	 */
	public ArticleListAdapter(Context context, int header, int resource,
			List<Map<String, String>> objects) {
		super(context,  header, resource, objects);
		
		mContext = context;
		mArticleSummaries = objects;
		mHeader = header;
		mResource = resource;
		mAttributes = getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
		TAG_TITLE = mContext.getResources().getString(R.string.JSON_tag_title);
		TAG_IMAGE = mContext.getResources().getString(R.string.JSON_tag_image);
		TAG_ID = mContext.getResources().getString(R.string.JSON_tag_id);
		TAG_TAG = mContext.getResources().getString(R.string.JSON_tag_tag);
		
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
				// create several textviews, all under the RL_layout view 
				// for each textview, can set an onclicklistener too
				viewHolder.RL_layout = (RelativeLayout) rowview.findViewById(R.id.list_secondline);
				ViewHolder.tv_tags = new TextView[MAX_NUM_TAGS_DISPLAY];
				for (int tagidx = 0; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
					// create a textview from a template file, which attribute is obtained in the constructor
					ViewHolder.tv_tags[tagidx] = new TextView(mContext, mAttributes);
					ViewHolder.tv_tags[tagidx].setId(tagidx+1);
					// create some layout parameters for the relative layout
					LayoutParams laypar = new LayoutParams(LayoutParams.WRAP_CONTENT, 
											  			   LayoutParams.WRAP_CONTENT);
					if (tagidx ==0)
						laypar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					else
						laypar.addRule(RelativeLayout.RIGHT_OF, ViewHolder.tv_tags[tagidx-1].getId());		
					laypar.addRule(RelativeLayout.CENTER_VERTICAL);
					viewHolder.RL_layout.addView(ViewHolder.tv_tags[tagidx], laypar);
				}
			}
			
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{

			viewHolder = (ViewHolder) rowview.getTag();
			viewHolder.iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
		}
		
		
		
		Map<String,String> articlesummary = mArticleSummaries.get(position); 
		String sTitle = articlesummary.get(TAG_TITLE);
		String sImgURL = articlesummary.get(TAG_IMAGE);
		String sArticleID = articlesummary.get(TAG_ID);
		//String sOwner = articlesummary.get("owner");
		//String tag = articlesummary.get(TAG_TAG);
		//tag = tag.replaceAll("\\#\\*", ";");
		//tag = tag.replaceAll("\\#|\\*", "");
		//String[] tags = tag.split(";");
		
		
		viewHolder.articleID = Integer.parseInt(sArticleID);
		viewHolder.tv_firstline.setText(sTitle);
		//if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
		//	viewHolder.tv_secondline.setText("作者：" + sOwner);
		//}
		
		/*int tagstoshow = tags.length < MAX_NUM_TAGS_DISPLAY 
				         ? tags.length : MAX_NUM_TAGS_DISPLAY;
		if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
			for (int tagidx = 0; tagidx < tagstoshow; tagidx++){
				ViewHolder.tv_tags[tagidx].setVisibility(View.VISIBLE);
				ViewHolder.tv_tags[tagidx].setText(tags[tagidx]);
			}
			for (int tagidx = tagstoshow; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
				ViewHolder.tv_tags[tagidx].setVisibility(View.GONE);
			}
		}*/
		
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
	

}
