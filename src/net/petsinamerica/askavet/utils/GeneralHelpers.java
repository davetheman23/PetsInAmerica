package net.petsinamerica.askavet.utils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class GeneralHelpers {
	
	private static final String TEMP_SUBDIR = "temp";
	
	/*
	 * define the media type int, for 
	 */
	public final static int MEDIA_TYPE_IMAGE = 1;
	
	public final static int MEDIA_TYPE_VIDEO = 2;

	
	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
		String storageDir = App.EXTERNAL_DIRECTORY;
		if (storageDir == null){
			storageDir = App.INTERNAL_DIRECTORY;
		}

	    File mediaStorageDir = new File(storageDir + File.separator +
        		TEMP_SUBDIR);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d(App.PIA_ROOT_DIR, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    	String filePath = mediaStorageDir.getPath() + File.separator + "tmp_Image.jpg";
	        mediaFile = new File(filePath);
	    } else if(type == MEDIA_TYPE_VIDEO) {
	    	String filePath = mediaStorageDir.getPath()  + File.separator + "tmp_video.mp4";
	        mediaFile = new File(filePath);
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

	/*
	 * load a Bitmap onto the Imageview, which was given as input param
	 * image is download from the url stored in the imageview tag,
	 * the downloaded image is resized to a small file size, and is 
	 * stored in the mMemCache
	 */	
	public abstract static class DownLoadImageTask extends AsyncTask<String, Integer, Bitmap>{
		
		MemoryCache mMemCache = null;
		private int mScale = 1;


		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			return download_image(url);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			onDownloaded(result);
		}
		/**
		 * Implement action to be performed for the downloaded image
		 * @param result the bitmap stored in memcache if set 
		 */
		protected abstract void onDownloaded(Bitmap result);
		
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
		
		
		/**
		 * set aside some amount of cache for fast image loading
		 * @param memCache an instance of MemoryCahce
		 */
		public void SetMemCache(MemoryCache memCache){
			mMemCache = memCache;
		}
		
		/**
		 * set aside some amount of cache for fast image loading
		 * @param memCache an instance of MemoryCahce
		 * @param scale > 1, the factor to scale the image down in file size 
		 */
		public void SetMemCache(MemoryCache memCache, int scale){
			mMemCache = memCache;
			mScale = scale;
		}
		
		public MemoryCache GetMemCache(){
			return mMemCache;
		}
		
		

	}
	
	  /**
	   * this class to store an bitmap image in the memory cache
	   * the data only persist until application is killed
	   */
	public static class MemoryCache {
		private LruCache<String, Bitmap> mMemoryCache;
		
		private int maxMemory;
		private int cacheSize;
		
		public MemoryCache(){
			maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
			cacheSize = maxMemory / 8;
			
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
				@Override
		        protected int sizeOf(String key, Bitmap bitmap) {
		            // The cache size will be measured in kilobytes rather than
		            // number of items.
		            return bitmap.getByteCount() / 1024;
		        }
			};
				
		}
		
		public void addBitMapToMemoryCache(String key, Bitmap bitmap){
			if (getBitmapFromMemCache(key) == null)
				mMemoryCache.put(key, bitmap);
		}

		public Bitmap getBitmapFromMemCache(String key) {
			return mMemoryCache.get(key);
		}

	}

}
