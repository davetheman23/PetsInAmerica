package net.petsinamerica.askavet.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/*
 * load a Bitmap onto the Imageview, which was given as input param
 * image is download from the url stored in the imageview tag,
 * the downloaded image is resized to a small file size, and is 
 * stored in the mMemCache
 */	
public class DownLoadImageTask extends AsyncTask<ImageView, Integer, Bitmap>{
	
	ImageView imageview = null;
	MemoryCache mMemCache = null;
	private int mScale = 1;


	@Override
	protected Bitmap doInBackground(ImageView... params) {
		this.imageview = params[0];
		String url = imageview.getTag().toString();
		return download_image(url);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// crop the image view that is returned 
		// from doInBackground method 
	
		// set the bitmap to imageView 
		imageview.setImageBitmap(result);

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
			//opt.inPurgeable = true;
			
			// decode inputstream from url as bitmp given above options
			bmp = BitmapFactory.decodeStream(is_backup, null, opt);

			if(null != bmp){
				if (mMemCache != null)	// if Memchace has been set
					mMemCache.addBitMapToMemoryCache(url, bmp);	// add the bitmap to cache when first downloaded
				return bmp;
			}
		}catch(Exception e){
			
		}
		return bmp;
	}
	
	
	/*
	 * set aside some amount of cache for fast image loading
	 * @ memCache, an instance of MemoryCahce
	 */
	public void SetMemCache(MemoryCache memCache){
		mMemCache = memCache;
	}
	
	/*
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