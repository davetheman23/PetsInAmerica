package net.petsinaermica.askavet.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONResponseHandler implements ResponseHandler<List<String>> {
	
	
	ArrayList<String> mTags;
	public static final int TAG_GROUP_1	= 1;
	public static final int TAG_GROUP_2	= 2;
	
	private boolean mSecondLevel = false;	// whether second level is to be extracted
	
	public void SetTags(int tagGroupID){
		if (tagGroupID == TAG_GROUP_1){
			mTags.add("img");
			mTags.add("time");
			mTags.add("id");
			mTags.add("author");
			mTags.add("owner");
			mTags.add("title");
			mTags.add("list");
			mTags.add("content");
			mTags.add("avatar");
		}else if (tagGroupID == TAG_GROUP_2){
			mTags.add("img");
			mTags.add("time");
			mTags.add("id");
			mTags.add("author");
			mTags.add("owner");
			mTags.add("title");
			mTags.add("list");
			mTags.add("content");
			mTags.add("maintag");
			mTags.add("tag");
			mSecondLevel = true;
		}
	}

	@Override
	public List<String> handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		//List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		List<String> result = new ArrayList<String>();
		String JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
		try {

			// Get top-level JSON Object - a Map
			JSONObject responseObject = (JSONObject) new JSONTokener(
					JSONResponse).nextValue();
			
			for (String tag : mTags){
				result.add(responseObject.get(tag) + ";;");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}