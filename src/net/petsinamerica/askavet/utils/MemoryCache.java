package net.petsinamerica.askavet.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

  /**
   * @deprecated use picasso instead
   * this class to store an bitmap image in the memory cache
   * the data only persist until application is killed
   */
public class MemoryCache {
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
	            return sizeOf_kb(bitmap);
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
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf_kb(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight() / 1024;
        } else {
            return data.getByteCount() / 1024;
        }
    }

}
