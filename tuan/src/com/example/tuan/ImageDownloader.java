package com.example.tuan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.example.tuan.net.NetUtils;

public class ImageDownloader {
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

		String small_image_url;
		WeakReference<ImageView> imageViewRef;
		String fileName;
		
		public BitmapDownloaderTask(String small_image_url, ImageView imageView,String fileName) {
			this.small_image_url = small_image_url;
			imageViewRef = new WeakReference<ImageView>(imageView);
			this.fileName = fileName;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			Bitmap bitmap = NetUtils.downloadBitmap(url);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (isCancelled()) {
				result = null;
			}

			// hardedImageCache.put(small_image_url, new
			// WeakReference<Bitmap>(result));
			addBitmapToCache(small_image_url, result);
			if (imageViewRef != null) {
				ImageView imageView = imageViewRef.get();
				BitmapDownloaderTask task = getBitmapDownloaderTask(imageView);
				if (this == task) {
					storeBitmapToStorage(fileName, result);
					imageView.setImageBitmap(result);
				}
			}
		}
	}

	class DownloadedDrawable extends BitmapDrawable {
		AsyncTask<String, Void, Bitmap> task;

		public DownloadedDrawable(AsyncTask<String, Void, Bitmap> task) {
			this.task = task;
		}
	}

	final static int CAPACITY = 10;

	HashMap<String, Bitmap> hardedImageCache;
	ConcurrentHashMap<String, SoftReference<Bitmap>> softedImageCache;

	public ImageDownloader() {
		hardedImageCache = new LinkedHashMap<String, Bitmap>(CAPACITY / 2, 0.75f, true) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
				if (size() > CAPACITY) {
					softedImageCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
					return true;
				}
				return false;
			}

		};
		softedImageCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(CAPACITY / 2);


	}

	public void download(String url, ImageView imageView,String fileName) {
		Bitmap bitmap = retrieveBitmapFromStorage(fileName);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return;
		}

		bitmap = getBitmapFromCache(url);
		if (bitmap == null) {
			Log.d("ImageLoader", "forceDownload " + url);
			forceDownload(url, imageView,fileName);
		} else {
			//Log.d("ImageLoader", "cache not null " + url);
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	void forceDownload(String url, ImageView imageView,String fileName) {
		if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(url, imageView,fileName);
			DownloadedDrawable drawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(drawable);
			task.execute(url);
		}
	}

	boolean cancelPotentialDownload(String url, ImageView imageView) {
		BitmapDownloaderTask task = getBitmapDownloaderTask(imageView);
		if (task != null) {
			String _url = task.small_image_url;
			if (_url == null || !_url.equals(url)) {
				task.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				BitmapDownloaderTask task = (BitmapDownloaderTask) ((DownloadedDrawable) drawable).task;
				return task;
			}
		}
		return null;
	}

	private void addBitmapToCache(String url, Bitmap bitmap) {
		if(bitmap!=null)
		synchronized (hardedImageCache) {
			hardedImageCache.put(url, bitmap);
			
		}
	
	}

	private Bitmap getBitmapFromCache(String url) {

		synchronized (hardedImageCache) {

			Bitmap bitmap = hardedImageCache.get(url);
			if (bitmap != null) {
				hardedImageCache.remove(url);
				hardedImageCache.put(url, bitmap);
				return bitmap;
			}
		}

		SoftReference<Bitmap> bitmapRef = softedImageCache.get(url);
		if (bitmapRef != null) {
			Bitmap bitmap = bitmapRef.get();
			if (bitmap != null)
				return bitmap;
			else
				softedImageCache.remove(url);
		}
		return null;

	}

	Bitmap retrieveBitmapFromStorage(String fileName) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return null;

		File externalCacheDir = Environment.getExternalStorageDirectory();
		File dir = new File(externalCacheDir, "tuan");
		if (!dir.exists())
			return null;
		File file = new File(dir.getAbsoluteFile() + File.separator + fileName);
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				return bitmap;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return null;
	}

	boolean storeBitmapToStorage(String fileName, Bitmap bitmap) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return false;

		File externalCacheDir = Environment.getExternalStorageDirectory();
		if (externalCacheDir == null)
			return false;
		// Log.d(TAG, externalCacheDir.getAbsolutePath());
		File dir = new File(externalCacheDir, "tuan");
		if (!dir.exists()) {
			dir.mkdirs();
			// Log.d(TAG, dir.getAbsolutePath()+"-----"+bool);
		}
		File file = new File(dir + File.separator + fileName);
		// Log.d(TAG, file.getAbsolutePath());
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		if (bitmap == null)
			return false;

		BufferedOutputStream bos = null;
		try {

			bos = new BufferedOutputStream(new FileOutputStream(file));
			boolean bool = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			return bool;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
