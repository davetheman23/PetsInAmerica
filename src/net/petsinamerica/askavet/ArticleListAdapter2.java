package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.Html;
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
public class ArticleListAdapter2 extends ArrayAdapter<Map<String, Object>> {
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
	private static String TAG_TITLE  = App.appContext.getString(R.string.JSON_tag_title);
	private static String TAG_IMAGE = App.appContext.getString(R.string.JSON_tag_image);
	private static String TAG_ID = App.appContext.getString(R.string.JSON_tag_id);
	private static String TAG_TAGS  = App.appContext.getString(R.string.JSON_tag_tags);
	private static final String TAG_CONTENT = App.appContext.getString(R.string.JSON_tag_content);
	
	private class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		TextView[] tv_tags;
		LinearLayout linearLayout;
		int articleID;
		int comment_num;
		int like_num;
	}
	
	/*
	 *  Standard constructer
	 */
	public ArticleListAdapter2(Context context, int header, int resource,
			List<Map<String, Object>> objects) {
		super(context,  header, resource, objects);
		
		mContext = context;
		//mArticleSummaries = objects;
		mHeader = header;
		mResource = resource;
		mAttributes = getAttributeSet(mContext, R.layout.list_tag_template, "TextView");
		
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
				viewHolder.iv = (ImageView) rowview.findViewById(R.id.article_list_icon);
				viewHolder.tv_firstline =(TextView) rowview.findViewById(R.id.article_list_firstLine);	
				viewHolder.tv_secondline = (TextView) rowview.findViewById(R.id.article_list_secondline);
			}
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{

			viewHolder = (ViewHolder) rowview.getTag();
		}
		
		Map<String, Object> articlesummary = getItem(position); 
		String sTitle,sImgURL, sArticleID;
		sTitle = (String) articlesummary.get(TAG_TITLE);
		sImgURL = (String) articlesummary.get(TAG_IMAGE);
		sArticleID = (String) articlesummary.get(TAG_ID);
		String content = (String) articlesummary.get(TAG_CONTENT);
		String sCommentNum = (String)articlesummary.get(Constants.KEY_ARTICLE_COMMENTS);
		String sLikeNum = (String)articlesummary.get(Constants.KEY_ARTICLE_LIKES);
		
		//content = content.replaceAll("<br />", "");
		sTitle = sTitle.trim().replaceAll("<.*?>", "");
		//sTitle = sTitle.replaceAll("<br />", "");
		content = content.trim().replaceAll("<.*?>", "");
		
		
		viewHolder.articleID = Integer.parseInt(sArticleID);
		viewHolder.comment_num = Integer.parseInt(sCommentNum);
		viewHolder.like_num = Integer.parseInt(sLikeNum);
		viewHolder.tv_firstline.setText(Html.fromHtml(sTitle));
		if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
			viewHolder.tv_secondline.setText(Html.fromHtml(content));
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
				.placeholder(R.drawable.ic_pia_logo)
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

	/**
	 * return a article ID from view selected
	 */
	public int getArticleID(View v) {
		// this assumes the view is the row view so it has a viewholder
		ViewHolder vh = (ViewHolder) v.getTag();

		return vh.articleID;
	}
	
	/**
	 * Get the number of comments on the current article
	 */
	public int getCommentNum(View v){
		ViewHolder vh = (ViewHolder) v.getTag();
		return vh.comment_num;
	}
	
	/**
	 * Get the number of likes on the current article
	 */
	public int getLikeNum(View v){
		ViewHolder vh = (ViewHolder) v.getTag();
		return vh.like_num;
	}
	
	
	/**
	 * Get Chinese tags for each article
	 * @param object
	 * @return
	 */
	/*private String[] getCnTags(Object object){
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
	}*/

}
