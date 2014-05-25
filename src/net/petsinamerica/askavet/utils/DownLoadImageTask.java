package net.petsinamerica.askavet.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * @deprecated use picasso instead
 * load a Bitmap onto the Imageview, which was given as input param
 * image is download from the url stored in the imageview tag,
 * the downloaded image is resized to a small file size, and is 
 * stored in the mMemCache
 *  
 */	
public class DownLoadImageTask extends AsyncTask<ImageView, Integer, Bitmap>{
	
	ImageView imageview = null;
	MemoryCache mMemCache = null;
	private int mScale = 1;
	private String mUrl;
	
	private static final String TAG = "DownloadImageTask";


	@Override
	protected Bitmap doInBackground(ImageView... params) {
		this.imageview = params[0];
		mUrl = imageview.getTag().toString();
		return download_image(mUrl);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// crop the image view that is returned 
		// from doInBackground method 
	
		// NOTE: due to the use of viewholder in getView() in the custom adapter
		// users may scroll passed the row before the download of image can be
		// finished, so the viewholder is recyled by another row, in this case
		// set the image to the recycled viewholder will cause the image be 
		// set to a new row not the row that initiated the download, a check below 
		// is performed to ensure this
		if (imageview.getTag().toString().equals(mUrl)){
			// set the bitmap to imageView 
			imageview.setImageBitmap(result);
		}else{
			Log.i(TAG, "Current URL has changed!");
		}

	}
	
	private Bitmap download_image(String url){
		Bitmap bmp = null;
		
		try{
			// open an internet connection to read data stream from the url
			URL urln = new URL(url);
			HttpURLConnection con = (HttpURLConnection) urln.openConnection();
			InputStream is = con.getInputStream();
			InputStream is_backup = is;			
			
			// set options when decoding, this is to reduce the size of the image
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inSampleSize = mScale;		// scale it down in file size
			bmp = BitmapFactory.decodeStream(is_backup, null, opt);

			
			if(null != bmp){
				if (mMemCache != null)	// if Memchace has been set
					mMemCache.addBitMapToMemoryCache(url, bmp);	// add the bitmap to cache when first downloaded
				return bmp;
			}
		}catch(Exception e){
			Log.e(TAG, "Download Image failed!");
		}
		return bmp;
	}
	
	
	/**
	 * set aside some amount of cache for fast image loading
	 * @ memCache, an instance of MemoryCahce
	 */
	public void SetMemCache(MemoryCache memCache){
		mMemCache = memCache;
	}
	
	/**
	 * set aside some amount of cache for fast image loading
	 * @ memCache, an instance of MemoryCahce
	 * @ scale > 1, the factor to scale the image down in file size 
	 */
	public void SetMemCache(MemoryCache memCache, int scale){
		mMemCache = memCache;
		mScale = scale;
	}
	
	public MemoryCache GetMemCache(){
		return mMemCache;
	}

	

}