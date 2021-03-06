package net.petsinamerica.askavet;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.protocol.HTTP;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.sax.StartElementListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

class EnquiryDetailListAdapter extends ArrayAdapter<Map<String,Object>> {
	private final Context mContext;
	//private final List<Map<String, String>> mEnquiries;
	private final int mResource, mResource_header;
	
	private final static int HEADER_POSITION  = 0;
	private final static int LIST_VIEW_TYPE_HEADER = 0;
	private final static int LIST_VIEW_TYPE_REGULAR = 1;
	
	private TextView tv_petName;
	private TextView tv_petSpecies;
	private TextView tv_petBreed;
	private TextView tv_petAge;
	private TextView tv_petNeuterAge;
	private TextView tv_petSex;
	private ImageView iv_petPic;
	
	private TextView tv_title;
	private TextView tv_petDiet;
	private TextView tv_petDietType;
	private TextView tv_petWeight;
	private TextView tv_petResponsiveness;
	private TextView tv_petStool;
	private TextView tv_petAppetite;
	
	private WebView wv_enquirydetails;
	
	private TextView tv_author;
	private TextView tv_authordate;
	private TextView tv_enquirydetails;
	private ImageView iv_authorPic;
	
	
	private static class ViewHolder{
		ImageView iv_authorpic;
		TextView tv_authorname;
		TextView tv_enquirydetails;
		TextView tv_publishdate;
		WebView wv_details;
	}
	
	/*
	 *  Standard constructer
	 */
	public EnquiryDetailListAdapter(Context context, int resource_header, 
			int resource_regular, List<Map<String, Object>> objects) {
		super(context, resource_regular, objects);
		mContext = context;
		mResource = resource_regular;
		mResource_header = resource_header;
		
		
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
			
			if (getItemViewType(position) == LIST_VIEW_TYPE_HEADER){
				rowview = inflater.inflate(mResource_header, parent, false);
				
				tv_title = (TextView) rowview.findViewById(R.id.frag_enquiry_details_title);
				tv_petName = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petName);
				tv_petSpecies = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petspecies);
				tv_petBreed = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petbreed);
				tv_petAge = (TextView) rowview.findViewById(R.id.frag_enquiry_details_pet_age);
				tv_petNeuterAge = (TextView) rowview.findViewById(R.id.frag_enquiry_details_pet_neuterage);
				tv_petSex = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petsex);
				iv_petPic = (ImageView) rowview.findViewById(R.id.frag_enquiry_details_pet_pic);
				
				tv_title = (TextView) rowview.findViewById(R.id.frag_enquiry_details_title);
				tv_petName = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petName);
				tv_petSpecies = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petspecies);
				tv_petBreed = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petbreed);
				tv_petAge = (TextView) rowview.findViewById(R.id.frag_enquiry_details_pet_age);
				tv_petNeuterAge = (TextView) rowview.findViewById(R.id.frag_enquiry_details_pet_neuterage);
				tv_petSex = (TextView) rowview.findViewById(R.id.frag_enquiry_details_petsex);
				iv_petPic = (ImageView) rowview.findViewById(R.id.frag_enquiry_details_pet_pic);
				
				tv_petDiet = (TextView) rowview.findViewById(R.id.frag_enquiry_details_diet);
				tv_petDietType = (TextView) rowview.findViewById(R.id.frag_enquiry_details_diet_type);
				tv_petWeight = (TextView) rowview.findViewById(R.id.frag_enquiry_details_weight);
				tv_petResponsiveness = (TextView) rowview.findViewById(R.id.frag_enquiry_details_responsiveness);
				tv_petStool = (TextView) rowview.findViewById(R.id.frag_enquiry_details_stool);
				tv_petAppetite = (TextView) rowview.findViewById(R.id.frag_enquiry_details_appetite);
				
				tv_author = (TextView) rowview.findViewById(R.id.frag_enquiry_details_author_name);
				tv_authordate = (TextView) rowview.findViewById(R.id.frag_enquiry_details_author_date);
				tv_enquirydetails = (TextView) rowview.findViewById(R.id.frag_enquiry_details_details);
				iv_authorPic = (ImageView) rowview.findViewById(R.id.frag_enquiry_details_author_pic);
				
				wv_enquirydetails = (WebView) rowview.findViewById(R.id.frag_enquiry_details_webview_details);
				
			}else if (getItemViewType(position) == LIST_VIEW_TYPE_REGULAR){
				// inflate the layout view, and get individual views
				rowview = inflater.inflate(mResource, parent, false);
				viewHolder.iv_authorpic = (ImageView) rowview.findViewById(R.id.list_enquiry_details_author_pic);
				viewHolder.tv_authorname =(TextView) rowview.findViewById(R.id.list_enquiry_details_author_name);	
				viewHolder.tv_enquirydetails = (TextView) rowview.findViewById(R.id.list_enquiry_details_details);
				viewHolder.tv_publishdate = (TextView) rowview.findViewById(R.id.list_enquiry_details_author_date);
				viewHolder.wv_details = (WebView) rowview.findViewById(R.id.list_enquiry_details_webview_details);
				
				
			}
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) rowview.getTag();
		}
		Map<String, Object> queryInfo = getItem(position);
		
		if (getItemViewType(position) == LIST_VIEW_TYPE_HEADER){
			
			// load the enquiry info
			tv_title.setText(queryInfo.get(Constants.KEY_TITLE).toString());
			tv_petDiet.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_DIETDESCR).toString());
			tv_petDietType.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_DIETTYPE).toString());
			tv_petWeight.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_WEIGHT).toString());
			tv_petStool.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_STOOL).toString());
			tv_petAppetite.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_APPETITE).toString());
			tv_petResponsiveness.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_RESPONSIVE).toString());
			
			tv_author.setText(queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORNAME).toString());
			tv_authordate.setText(queryInfo.get("date").toString());
			tv_enquirydetails.setText(Html.fromHtml(
					queryInfo.get(Constants.KEY_CONTENT).toString()));
			
			// load the user info
			String urlAuthorPic = queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORAVATAR).toString();
			Picasso.with(App.appContext)
				   .load(urlAuthorPic)
				   .placeholder(R.drawable.someone)
				   .into(iv_authorPic);
			if (urlAuthorPic ==null || urlAuthorPic.endsWith("someone.png")){			
				// cancel request when download is not needed
				Picasso.with(App.appContext)
					.cancelRequest(iv_authorPic);
			}
			// load the webview
			wv_enquirydetails.setWebViewClient(new WebViewClient());
			wv_enquirydetails.getSettings().setBuiltInZoomControls(false);
			wv_enquirydetails.getSettings().setSupportZoom(false);
			
			// replace the url back to proper image url
			String html_string = queryInfo.get(Constants.KEY_CONTENT).toString();
			String pattern1 = "(" + Pattern.quote("[img]")+")(.*?)("+ Pattern.quote("[/img]")+ ")";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
			html_string = html_string.replaceAll(pattern1, "<img src = $2 width=\"100%\" alt=\"\">");
			wv_enquirydetails.loadDataWithBaseURL(null, html_string, "text/html", HTTP.UTF_8, null);
			
		}else{
			String authorname = queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORNAME).toString();
			String userAvatarURL = queryInfo.get(Constants.KEY_ENQUIRY_PET_AUTHORAVATAR).toString();
			String details = queryInfo.get(Constants.KEY_CONTENT).toString();
			String date = queryInfo.get("date").toString();
			if (userAvatarURL != null){
				userAvatarURL = Constants.URL_CLOUD_STORAGE + userAvatarURL;
			}
			
			// -- allow some text within a webview to be clickable, especially those that will direct to another article
			String str_articleId = "0";
			// 1. first detect if there are any text should be clickable
			// 1-a. match article url with pattern "[url=article]180[/url]"
			String pattern_str1 = "(\\[url=)(.*?article/)([0-9]+)(\\])(.*?)(\\[/url\\])";
			Pattern pattern1 = Pattern.compile(pattern_str1);
			Matcher matcher1 = pattern1.matcher(details);
			if (matcher1.find()){
				str_articleId = matcher1.group(3);
				details = details.replaceAll(pattern_str1, "<a href = \"loadarticle:$3\">$5</a>");
			}
			// 1-b. match article url with pattern "[article]180[/article]"
			String pattern_str2 = "(\\[article\\])([0-9]+)(\\[/article\\])";
			Pattern pattern2 = Pattern.compile(pattern_str2);
			Matcher matcher2 = pattern2.matcher(details);
			if (matcher2.find()){
				str_articleId = matcher2.group(2);
				details = details.replaceAll(pattern_str2, "<a href = \"loadarticle:$3\">article $2</a>");
			}
			
			final int article_id = Integer.parseInt(str_articleId);
			// 1-b. match the image url
			String pattern3 = "(" + Pattern.quote("[img]")+")(.*?)("+ Pattern.quote("[/img]")+ ")";	// see http://www.vogella.com/tutorials/JavaRegularExpressions/article.html for further info
			details = details.replaceAll(pattern3, "<img src = $2 width=\"100%\" alt=\"\">");
			
			viewHolder.tv_authorname.setText(authorname);
			//viewHolder.tv_enquirydetails.setText(Html.fromHtml(details));
			viewHolder.tv_publishdate.setText(date);
			viewHolder.wv_details.loadDataWithBaseURL(null, details, "text/html", HTTP.UTF_8, null);
			
			// overwrite how to load url
			viewHolder.wv_details.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.startsWith("loadarticle")){
						Intent intent = new Intent(mContext, ArticleActivity.class);
						intent.putExtra(Constants.KEY_ARTICALID, article_id);
						mContext.startActivity(intent);
					}else{
						// if not any of the cases above, it will open up the browser
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						mContext.startActivity(intent);
					}
					return true;
				}
			});
			
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
		}
		return rowview;
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

}
