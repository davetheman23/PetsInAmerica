package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.List;

import net.petsinaermica.askavet.utils.DownLoadImageTask;
import net.petsinaermica.askavet.utils.MemoryCache;
import net.petsinamerica.askavet2.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
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

/*
 *  listAdapter to display article summaries
 *  layout file list_item.xml 
 */
public class ArticleListAdapter extends ArrayAdapter<String> {
	private final Context mContext;
	private final List<String> mObjects;
	private final int mResource;
	private final int mHeader;
	private final static int HEADER_POSITION  = 0;
	private final static int LIST_VIEW_TYPE_HEADER = 0;
	private final static int LIST_VIEW_TYPE_REGULAR = 1;
	private final static int MAX_NUM_TAGS_DISPLAY = 5;
	private MemoryCache mMemCache;
	
	private AttributeSet mAttributes;
	
	static class ViewHolder{
		ImageView iv;
		TextView tv_firstline;
		TextView tv_secondline;
		static TextView[] tv_tags;
		RelativeLayout RL_layout;
		int position;
	}
	
	/*
	 *  Standard constructer
	 */
	public ArticleListAdapter(Context context, int header, int resource,
			List<String> objects) {
		super(context,  header, resource, objects);
		
		mContext = context;
		mObjects = objects;
		mHeader = header;
		mResource = resource;
		mMemCache = new MemoryCache();	// set aside some cache memory to store bitmaps, for fast loading of image
		
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
					LayoutParams laypar = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
			for (int tagidx = 0; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
				ViewHolder.tv_tags[tagidx].setText("");
				ViewHolder.tv_tags[tagidx].setVisibility(View.GONE);
			}
		}
		
		// parse the JSON string, obtained elsewhere
		// may need a better logic here instead of hard coding 
		String s = mObjects.get(position).toString();
		String[] tokens = s.split(";;");

		
		viewHolder.tv_firstline.setText(tokens[2]);
		//if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
//			viewHolder.tv_secondline.setText("作者：" + tokens[4]);
	//	}
		String[] subtokens = tokens[7].split(";");
		
		int tagstoshow = subtokens.length < MAX_NUM_TAGS_DISPLAY 
				         ? subtokens.length : MAX_NUM_TAGS_DISPLAY;
		if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
			for (int tagidx = 0; tagidx < tagstoshow; tagidx++){
				ViewHolder.tv_tags[tagidx].setVisibility(View.VISIBLE);
				ViewHolder.tv_tags[tagidx].setText(subtokens[tagidx]);
			}
			for (int tagidx = tagstoshow; tagidx < MAX_NUM_TAGS_DISPLAY; tagidx++){
				ViewHolder.tv_tags[tagidx].setVisibility(View.GONE);
			}
		}
		
		
		// setup image loading procedure
		String url = tokens[5];
		final Bitmap bitmap = mMemCache.getBitmapFromMemCache(url);	// try first see if image in cache
		if (bitmap != null){
			// if in cache, display immediately
			viewHolder.iv.setImageBitmap(bitmap);
		}else{
			// if not in cache, setup an async task to download from web
			viewHolder.iv.setTag(url);
			DownLoadImageTask  loadimage = new DownLoadImageTask();
			int scale = 0;
			if (getItemViewType(position) == LIST_VIEW_TYPE_HEADER){
				scale = 1;
			}else if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
				scale = 4;
			}
			loadimage.SetMemCache(mMemCache, scale);
			loadimage.execute(viewHolder.iv);
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
	 * return a article ID from the position selected
	 */
	public String getArticleID(int position) {
		// the ID position in the original JSON array is hard-coded
		String s = mObjects.get(position).toString();
		String[] tokens = s.split(";;");
		return tokens[1];
	}
	

}
