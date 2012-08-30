package com.example.tuan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

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
	class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

		String small_image_url;
		WeakReference<ImageView> imageViewRef;

		public ImageLoaderTask(String small_image_url, ImageView imageView) {
			this.small_image_url = small_image_url;
			imageViewRef = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			Bitmap bitmap = NetUtils.downloadBitmap(url);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			imageCache.put(small_image_url, new WeakReference<Bitmap>(result));
			if (imageViewRef != null) {
				ImageView imageView = imageViewRef.get();
				ImageLoaderTask task = findTask(imageView);
				if (this == task) {
					storeCacheImage(fileName, result);
					imageView.setImageBitmap(result);
				}
			}
		}
	}

	class DrawableWithTask extends BitmapDrawable {
		AsyncTask<String, Void, Bitmap> task;

		public DrawableWithTask(AsyncTask<String, Void, Bitmap> task) {
			this.task = task;
		}
	}

	HashMap<String, WeakReference<Bitmap>> imageCache;
	String fileName;

	public ImageDownloader(String fileName) {
		imageCache = new HashMap<String, WeakReference<Bitmap>>();
		this.fileName = fileName;

	}

	public void download(String url, ImageView imageView) {
		Bitmap bitmap = retrieveCacheImage(fileName);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return ;
		} 
		
		bitmap = getImageCacheBitmap(url);
		if (bitmap == null) {
			Log.d("ImageLoader","cache null "+url);
			forceDownload(url, imageView);
		} else {
		
				cancelproteionTask(url, imageView);
				imageView.setImageBitmap(bitmap);
		}
	}

	void forceDownload(String url, ImageView imageView) {
		ImageLoaderTask task = new ImageLoaderTask(url, imageView);
		DrawableWithTask drawable = new DrawableWithTask(task);
		imageView.setImageDrawable(drawable);
		task.execute(url);
	}

	boolean cancelproteionTask(String url, ImageView imageView) {
		ImageLoaderTask task = findTask(imageView);
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

	ImageLoaderTask findTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable != null && drawable instanceof DrawableWithTask) {
				ImageLoaderTask task = (ImageLoaderTask) ((DrawableWithTask) drawable).task;
				return task;
			}
		}
		return null;
	}

	private Bitmap getImageCacheBitmap(String url) {
		WeakReference<Bitmap> ref = imageCache.get(url);
		if (ref == null)
			return null;
		else {
			Bitmap bitmap = ref.get();
			return bitmap;
		}
	}

	Bitmap retrieveCacheImage(String fileName) {
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

	boolean storeCacheImage(String fileName, Bitmap bitmap) {
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
