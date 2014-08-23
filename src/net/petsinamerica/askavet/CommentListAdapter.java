package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

class CommentListAdapter extends ArrayAdapter<Map<String,Object>> {
	private final Context mContext;
	
	private int mResource;
	
	private class ViewHolder{
		ImageView iv_authorpic;
		TextView tv_authorname;
		TextView tv_commentcontent;
		TextView tv_publishdate;
	}
	
	/*
	 *  Standard constructer
	 */
	public CommentListAdapter(Context context, int resource_regular, 
					List<Map<String, Object>> objects) {
		super(context, resource_regular, objects);
		mContext = context;
		mResource = resource_regular;
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
			viewHolder.iv_authorpic = (ImageView) rowview.findViewById(R.id.list_comment_author_pic);
			viewHolder.tv_authorname =(TextView) rowview.findViewById(R.id.list_comment_author_name);	
			viewHolder.tv_commentcontent = (TextView) rowview.findViewById(R.id.list_comment_content);
			viewHolder.tv_publishdate = (TextView) rowview.findViewById(R.id.list_comment_author_date);
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) rowview.getTag();
		}
		Map<String, Object> queryInfo = getItem(position);
		
		String authorname = queryInfo.get(Constants.KEY_NAME).toString();
		String userAvatarURL = queryInfo.get(Constants.KEY_AVATAR).toString();
		String details = queryInfo.get("comment").toString();
		String date = queryInfo.get("date").toString();
		if (userAvatarURL != null){
			userAvatarURL = Constants.URL_CLOUD_STORAGE + userAvatarURL;
		}
		
		viewHolder.tv_authorname.setText(authorname);
		viewHolder.tv_commentcontent.setText(Html.fromHtml(details));
		viewHolder.tv_publishdate.setText(date);

		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 
		Picasso.with(mContext)
				.load(userAvatarURL)
				.placeholder(R.drawable.someone)
				.resize(60, 60)
				.into(viewHolder.iv_authorpic);
		
		if (userAvatarURL ==null || userAvatarURL.endsWith("someone.png")){			
			// cancel request when download is not needed
			Picasso.with(mContext)
				.cancelRequest(viewHolder.iv_authorpic);
		}
		return rowview;
	}

}
