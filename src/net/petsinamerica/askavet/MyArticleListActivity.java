package net.petsinamerica.askavet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class MyArticleListActivity extends FragmentActivity{
	
	public static final int LIST_TYPE_USERLIKED = 1;
	public static final int LIST_TYPE_USERCOMMENTED = 2;
	
	private static int mListType;
	
	private static String mUrl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mListType = getIntent().getIntExtra("ListType", 0);
		switch (mListType){
		case LIST_TYPE_USERCOMMENTED:
			mUrl = Constants.URL_USER_COMMENTS;
			break;
		case LIST_TYPE_USERLIKED:
			mUrl = Constants.URL_USER_LIKES;
			break;
		}
		
		ArticleListFragment articleListFragment = new ArticleListFragment();
		articleListFragment.setParameters(mUrl, false, false, false, true, false);
		articleListFragment.setUserDataFlag(true);
		
		getSupportFragmentManager().beginTransaction()
			.add(android.R.id.content, articleListFragment)
			.commit();
		
	}
	
	
	public static class ArticleListFragment extends BaseListFragment {
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
			setCustomAdapter(new MyArticleListAdapter(
					this.getActivity(), R.layout.list_article_item, emptyList));
			
		}
		

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setStyle(Style.card);
		}
		
		@Override
		protected void handleEmptyList() {
			//super.handleEmptyList();
			TextView tv = (TextView)getView().findViewById(android.R.id.empty);
			switch (mListType){
			case LIST_TYPE_USERCOMMENTED:
				tv.setText("你还没有评论过的文章哟！");
				break;
			case LIST_TYPE_USERLIKED:
				tv.setText("你还没有赞过的文章哟！");
				break;
			}
			//tv.setText(getResources().getString(R.string.no_content_available));
		}


		@Override
		protected void onItemClickAction(View v, int position, long id) {
			// obtain the article ID clicked
			int articleID = ((MyArticleListAdapter)this.getListAdapter()).getArticleID(v);
			
			// start a new activity 
			Intent newIntent = new Intent(this.getActivity(), ArticleActivity.class);
			newIntent.putExtra("ArticleId", articleID);
			startActivity(newIntent);
			
		}

	
	}
	

}
