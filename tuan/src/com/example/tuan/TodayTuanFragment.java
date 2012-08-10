package com.example.tuan;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.tuan.net.NetUtils;
import com.example.tuan.parser.Display;
import com.example.tuan.parser.XMLParser;

public class TodayTuanFragment extends SherlockFragment implements
		LoaderManager.LoaderCallbacks<String> {

	// /**
	// * This class holds the per-item data in our Loader.
	// */
	// public static class DisplayEntry {
	// public DisplayEntry(DisplayListLoader loader, ApplicationInfo info) {
	// mLoader = loader;
	// mInfo = info;
	// mApkFile = new File(info.sourceDir);
	// }
	//
	// public ApplicationInfo getApplicationInfo() {
	// return mInfo;
	// }
	//
	// public String getLabel() {
	// return mLabel;
	// }
	//
	// public Drawable getIcon() {
	// if (mIcon == null) {
	// if (mApkFile.exists()) {
	// mIcon = mInfo.loadIcon(mLoader.mPm);
	// return mIcon;
	// } else {
	// mMounted = false;
	// }
	// } else if (!mMounted) {
	// // If the app wasn't mounted but is now mounted, reload
	// // its icon.
	// if (mApkFile.exists()) {
	// mMounted = true;
	// mIcon = mInfo.loadIcon(mLoader.mPm);
	// return mIcon;
	// }
	// } else {
	// return mIcon;
	// }
	//
	// return mLoader.getContext().getResources().getDrawable(
	// android.R.drawable.sym_def_app_icon);
	// }
	//
	// @Override public String toString() {
	// return mLabel;
	// }
	//
	// void loadLabel(Context context) {
	// if (mLabel == null || !mMounted) {
	// if (!mApkFile.exists()) {
	// mMounted = false;
	// mLabel = mInfo.packageName;
	// } else {
	// mMounted = true;
	// CharSequence label = mInfo.loadLabel(context.getPackageManager());
	// mLabel = label != null ? label.toString() : mInfo.packageName;
	// }
	// }
	// }
	//
	// private final DisplayListLoader mLoader;
	// private final ApplicationInfo mInfo;
	// private final File mApkFile;
	// private String mLabel;
	// private Drawable mIcon;
	// private boolean mMounted;
	// }

	// /**
	// * Helper for determining if the configuration has changed in an
	// interesting
	// * way so we need to rebuild the app list.
	// */
	// public static class InterestingConfigChanges {
	// final Configuration mLastConfiguration = new Configuration();
	// int mLastDensity;
	//
	// boolean applyNewConfig(Resources res) {
	// int configChanges =
	// mLastConfiguration.updateFrom(res.getConfiguration());
	// boolean densityChanged = mLastDensity !=
	// res.getDisplayMetrics().densityDpi;
	// if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
	// |ActivityInfoCompat.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) !=
	// 0) {
	// mLastDensity = res.getDisplayMetrics().densityDpi;
	// return true;
	// }
	// return false;
	// }
	// }

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {
		final DisplayListLoader mLoader;

		public PackageIntentReceiver(DisplayListLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class DisplayListLoader extends AsyncTaskLoader<String> {
		// final InterestingConfigChanges mLastConfig = new
		// InterestingConfigChanges();
		// final PackageManager mPm;

		String mlists;
		PackageIntentReceiver mPackageObserver;
		int start ;
		int size;
		
		
		public DisplayListLoader(Context context,Bundle bundle) {
			super(context);
			start = bundle.getInt("start");
			size = bundle.getInt("size");
			// Retrieve the package manager for later use; note we don't
			// use 'context' directly but instead the save global application
			// context returned by getContext().
			// mPm = getContext().getPackageManager();
		}

		/**
		 * This is where the bulk of our work is done. This function is called
		 * in a background thread and should generate a new set of data to be
		 * published by the loader.
		 */
		@Override
		public String loadInBackground() {
			Log.i("loader", "loadInBackground");
			// Retrieve all known applications.
			// List<ApplicationInfo> apps = mPm.getInstalledApplications(
			// PackageManager.GET_UNINSTALLED_PACKAGES |
			// PackageManager.GET_DISABLED_COMPONENTS);
			// if (apps == null) {
			// apps = new ArrayList<ApplicationInfo>();
			// }

			String content = NetUtils.downloadData(start,size);
			// XMLParser parser = new XMLParser();
			// List<Display> lists = parser.parseXML(content);
			// // Log.i("loadInBackground", lists.size()+"");
			//
			// // final Context context = getContext();
			//
			// // Create corresponding array of entries and load their labels.
			// List<DisplayEntry> entries = new ArrayList<DisplayEntry>();
			// for (int i=0; i<lists.size(); i++) {
			// DisplayEntry entry = new DisplayEntry(lists.get(i));
			// // entry.loadLabel(context);
			// //entries.add(entry);
			// entries.add(entry);
			// Log.i("loadInBackground", entry.getTitle()+"");
			// // Log.i("loadInBackground", entry.getAddr()+"");
			// }
			// Log.i("loadInBackground", content);

			// Done!
			return content;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(String apps) {
			// Log.i("loader", "deliverResult "+apps);
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					// onReleaseResources(apps);
				}
			}
			String oldApps = apps;
			mlists = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			Log.i("loader", "onStartLoading ");
			if (mlists != null) {
				// If we currently have a result available, deliver it
				// immediately.
				// deliverResult(mlists);
			}

			// Start watching for changes in the app data.
			if (mPackageObserver == null) {
				mPackageObserver = new PackageIntentReceiver(this);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			// boolean configChange =
			// mLastConfig.applyNewConfig(getContext().getResources());

			if (takeContentChanged() || mlists == null /* || configChange */) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

		/**
		 * Handles a request to stop the Loader.
		 */
		@Override
		protected void onStopLoading() {
			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		/**
		 * Handles a request to cancel a load.
		 */
		@Override
		public void onCanceled(String apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			// onReleaseResources(apps);
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mlists != null) {
				onReleaseResources(mlists);
				mlists = null;
			}

			// Stop monitoring for changes.
			if (mPackageObserver != null) {
				getContext().unregisterReceiver(mPackageObserver);
				mPackageObserver = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(String apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}
	}

	public static class AppListAdapter extends ArrayAdapter<Display> {
		private final LayoutInflater mInflater;

		public AppListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List<Display> data) {
			clear();
			if (data != null) {
				for (Display appEntry : data) {
					add(appEntry);
				}
			}
		}

		/**
		 * Populate new items in the list.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			Display item = getItem(position);

			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_item_icon_text, parent,
						false);
				view.setTag(item.getWap_url());
			} else {
				view = convertView;
			}
			ImageView imageView = ((ImageView) view.findViewById(R.id.icon));

			String url = (String) imageView.getTag();
			if (url == null) {
				url = item.getSmall_image_url();
				imageView.setTag(url);
				// asyncwork
				ImageLoader loader = new ImageLoader(imageView);
				loader.execute(url);
			}

			// Log.i("getView",position+","+item.getTitle());
			((TextView) view.findViewById(R.id.text)).setText(item.getTitle());

			return view;
		}

		class ImageLoader extends AsyncTask<String, Void, Drawable> {

			ImageView view;

			public ImageLoader(ImageView view) {
				this.view = view;
			}

			@Override
			protected Drawable doInBackground(String... params) {
				String url = params[0];
				Bitmap bitmap = NetUtils.downloadBitmap(url);
				Drawable drawable = new BitmapDrawable(bitmap);
				return drawable;
			}

			@Override
			protected void onPostExecute(Drawable result) {
				view.setImageDrawable(result);
			}
		}

	}

	AppListAdapter mAdapter;
	ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_today_tuan, container,
				false);
		listView = (ListView) view.findViewById(R.id.listView1);
		View footView = inflater.inflate(R.layout.list_item_foot_view, null,
				false);
		listView.addFooterView(footView);
//		listView.setOnScrollListener(new OnScrollListener() {
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				Log.i("onScrollStateChanged", scrollState + "");
//			}
//
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				Log.i("onScrollStateChanged", firstVisibleItem+","+visibleItemCount + "/"
//						+ totalItemCount);
//				if(firstVisibleItem+visibleItemCount==totalItemCount){ //µ½µ×
//					Bundle bundle = new Bundle();
//					bundle.putInt("start", totalItemCount-1);
//					bundle.putInt("size", 10);
//					
//					getLoaderManager().initLoader(0, bundle, TodayTuanFragment.this);
//					
//				}
//				
//				
//			}
//		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String wap_url = (String) view.getTag();
				if (wap_url != null) {
					Intent intent = new Intent(getActivity(),
							DisplayActivity.class);
					intent.putExtra("wap_url", wap_url);
					startActivity(intent);
				}
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new AppListAdapter(getActivity());
		listView.setAdapter(mAdapter);

		// Start out with a progress indicator.
		// listView.setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		
		Bundle bundle = new Bundle();
		bundle.putInt("start", 1);
		bundle.putInt("size", 10);
		
		getLoaderManager().initLoader(0, bundle, this);
	}

	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader with no arguments, so it is simple.
		return new DisplayListLoader(getActivity(),args);
	}

	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		// Set the new data in the adapter.
		// Log.i("onLoadFinished", data);
		XMLParser parser = new XMLParser();
		List<Display> lists = parser.parseXML(data);
		// Log.i("onLoadFinished",lists.get(0).getTitle());
		// Log.i("onLoadFinished",lists.get(6).getTitle());
		mAdapter.setData(lists);

		// The list should now be shown.
		if (isResumed()) {
			// setListShown(true);
		} else {
			// setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}
}
