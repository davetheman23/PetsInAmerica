package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import android.content.Context;
import android.text.Html;
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
public class MyArticleListAdapter extends ArrayAdapter<Map<String, Object>> {
	private final Context mContext;
	private final int mResource;
	
	// these tags are those for reading the JSON objects
	private static String TAG_TITLE  = App.appContext.getString(R.string.JSON_tag_title);
	private static String TAG_IMAGE = App.appContext.getString(R.string.JSON_tag_image);
	private static String TAG_ID = App.appContext.getString(R.string.JSON_tag_id);
	private static final String TAG_CONTENT = App.appContext.getString(R.string.JSON_tag_content);
	
	private class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		int articleID;
	}
	
	/*
	 *  Standard constructer
	 */
	public MyArticleListAdapter(Context context, int resource,
			List<Map<String, Object>> objects) {
		super(context,  resource, objects);
		
		mContext = context;

		mResource = resource;
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
			viewHolder.iv = (ImageView) rowview.findViewById(R.id.article_list_icon);
			viewHolder.tv_firstline =(TextView) rowview.findViewById(R.id.article_list_firstLine);	
			viewHolder.tv_secondline = (TextView) rowview.findViewById(R.id.article_list_secondline);
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
		
		//content = content.replaceAll("<br />", "");
		sTitle = sTitle.trim().replaceAll("<.*?>", "");
		//sTitle = sTitle.replaceAll("<br />", "");
		content = content.trim().replaceAll("<.*?>", "");
		
		
		viewHolder.articleID = Integer.parseInt(sArticleID);
		viewHolder.tv_firstline.setText(Html.fromHtml(sTitle));
		viewHolder.tv_secondline.setText(Html.fromHtml(content));
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 

		Picasso.with(mContext)
			.load(sImgURL)
			.placeholder(R.drawable.ic_pia_logo)
			.resize(150, 120)
			.into(viewHolder.iv);

		return rowview;
		
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
