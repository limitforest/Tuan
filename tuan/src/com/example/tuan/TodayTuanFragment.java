package com.example.tuan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.example.tuan.net.NetUtils;
import com.example.tuan.parser.Display;
import com.example.tuan.parser.XMLParser;
import com.example.tuan.util.Constant;

public class TodayTuanFragment extends SherlockListFragment /*
															 * implements
															 * LoaderManager
															 * .LoaderCallbacks
															 * <Cursor>
															 */{
	// Debugging.
	static final String TAG = "TodayTuanFragment";
	/**
	 * The authority we use to get to our sample provider.
	 */
	public static final String AUTHORITY = "com.example.tuan.TodayTuan";

	/**
	 * Definition of the contract for the main table of our provider.
	 */
	public static final class MainTable implements BaseColumns {

		private MainTable() {
		}

		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "main";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/main");

		/**
		 * The content URI base for a single row of data. Callers must append a
		 * numeric row id to this Uri to retrieve a row
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/main/");

		/**
		 * The MIME type of {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.api-demos-throttle";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * row.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.api-demos-throttle";
		/**
		 * The default sort order for this table
		 */
		// public static final String DEFAULT_SORT_ORDER =
		// "data COLLATE LOCALIZED ASC";
		public static final String DEFAULT_SORT_ORDER = null;

		/**
		 * Column name for the single column holding our data.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_DATA = "data";

		public static final String COLUMN_NAME_WAP_URL = "wap_url";
		public static final String COLUMN_NAME_SMALL_IMAGE_URL = "small_image_url";
		public static final String COLUMN_NAME_REBATE = "rebate";
		public static final String COLUMN_NAME_PRICE = "price";
		public static final String COLUMN_NAME_BOUGHT = "bought";

		// public static final String COLUMN_NAME_GID = "gid";
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "todayTuan.db";
		private static final int DATABASE_VERSION = 2;

		DatabaseHelper(Context context) {

			// calls the super constructor, requesting the default cursor
			// factory.
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * 
		 * Creates the underlying database with table name and column names
		 * taken from the NotePad class.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " (" + MainTable._ID + " INTEGER PRIMARY KEY,"
					+ MainTable.COLUMN_NAME_WAP_URL + " TEXT," + MainTable.COLUMN_NAME_SMALL_IMAGE_URL + " TEXT,"
					+ MainTable.COLUMN_NAME_BOUGHT + " TEXT," + MainTable.COLUMN_NAME_PRICE + " TEXT,"
					+ MainTable.COLUMN_NAME_REBATE + " TEXT,"
					// + MainTable.COLUMN_NAME_GID + " INTEGER ,"
					+ MainTable.COLUMN_NAME_DATA + " TEXT" + ");");
		}

		/**
		 * 
		 * Demonstrates that the provider must consider what happens when the
		 * underlying datastore is changed. In this sample, the database is
		 * upgraded the database by destroying the existing data. A real
		 * application should upgrade the database in place.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// Logs that the database is being upgraded
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			// Kills the table and existing data
			db.execSQL("DROP TABLE IF EXISTS notes");

			// Recreates the database with a new version
			onCreate(db);
		}
	}

	public static class SimpleProvider extends ContentProvider {
		// A projection map used to select columns from the database
		private final HashMap<String, String> mNotesProjectionMap;
		// Uri matcher to decode incoming URIs.
		private final UriMatcher mUriMatcher;

		// The incoming URI matches the main table URI pattern
		private static final int MAIN = 1;
		// The incoming URI matches the main table row ID URI pattern
		private static final int MAIN_ID = 2;

		// Handle to a new DatabaseHelper.
		private DatabaseHelper mOpenHelper;

		/**
		 * Global provider initialization.
		 */
		public SimpleProvider() {
			// Create and initialize URI matcher.
			mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME, MAIN);
			mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", MAIN_ID);

			// Create and initialize projection map for all columns. This is
			// simply an identity mapping.
			mNotesProjectionMap = new HashMap<String, String>();
			mNotesProjectionMap.put(MainTable._ID, MainTable._ID);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_WAP_URL, MainTable.COLUMN_NAME_WAP_URL);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_SMALL_IMAGE_URL, MainTable.COLUMN_NAME_SMALL_IMAGE_URL);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_PRICE, MainTable.COLUMN_NAME_PRICE);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_REBATE, MainTable.COLUMN_NAME_REBATE);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_BOUGHT, MainTable.COLUMN_NAME_BOUGHT);
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_DATA, MainTable.COLUMN_NAME_DATA);
		}

		/**
		 * Perform provider creation.
		 */
		@Override
		public boolean onCreate() {
			mOpenHelper = new DatabaseHelper(getContext());
			// Assumes that any failures will be reported by a thrown exception.
			return true;
		}

		/**
		 * Handle incoming queries.
		 */
		@Override
		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

			// Constructs a new query builder and sets its table name
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(MainTable.TABLE_NAME);

			switch (mUriMatcher.match(uri)) {
			case MAIN:
				// If the incoming URI is for main table.
				qb.setProjectionMap(mNotesProjectionMap);
				break;

			case MAIN_ID:
				// The incoming URI is for a single row.
				qb.setProjectionMap(mNotesProjectionMap);
				qb.appendWhere(MainTable._ID + "=?");
				selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = MainTable.DEFAULT_SORT_ORDER;
			}

			SQLiteDatabase db = mOpenHelper.getReadableDatabase();

			Cursor c = qb.query(db, projection, selection, selectionArgs, null /*
																				 * no
																				 * group
																				 */, null /*
																						 * no
																						 * filter
																						 */, sortOrder);

			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		}

		/**
		 * Return the MIME type for an known URI in the provider.
		 */
		@Override
		public String getType(Uri uri) {
			switch (mUriMatcher.match(uri)) {
			case MAIN:
				return MainTable.CONTENT_TYPE;
			case MAIN_ID:
				return MainTable.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		}

		/**
		 * Handler inserting new data.
		 */
		@Override
		public Uri insert(Uri uri, ContentValues initialValues) {
			if (mUriMatcher.match(uri) != MAIN) {
				// Can only insert into to main URI.
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			ContentValues values;

			if (initialValues != null) {
				values = new ContentValues(initialValues);
			} else {
				values = new ContentValues();
			}

			if (values.containsKey(MainTable.COLUMN_NAME_DATA) == false) {
				values.put(MainTable.COLUMN_NAME_DATA, "");
			}

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			// long rowId = db.insert(MainTable.TABLE_NAME, null, values);

			long rowId = db.insertWithOnConflict(MainTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

			// If the insert succeeded, the row ID exists.
			if (rowId >= 0) {
				Uri noteUri = ContentUris.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
			Log.d(TAG, "rowId:" + rowId);
			throw new SQLException("Failed to insert row into " + uri);
		}

		/**
		 * Handle deleting data.
		 */
		@Override
		public int delete(Uri uri, String where, String[] whereArgs) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			String finalWhere;

			int count;

			switch (mUriMatcher.match(uri)) {
			case MAIN:
				// If URI is main table, delete uses incoming where clause and
				// args.
				count = db.delete(MainTable.TABLE_NAME, where, whereArgs);
				break;

			// If the incoming URI matches a single note ID, does the delete
			// based on the
			// incoming data, but modifies the where clause to restrict it to
			// the
			// particular note ID.
			case MAIN_ID:
				// If URI is for a particular row ID, delete is based on
				// incoming
				// data but modified to restrict to the given ID.
				finalWhere = DatabaseUtilsCompat.concatenateWhere(MainTable._ID + " = " + ContentUris.parseId(uri),
						where);
				count = db.delete(MainTable.TABLE_NAME, finalWhere, whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			getContext().getContentResolver().notifyChange(uri, null);

			return count;
		}

		/**
		 * Handle updating data.
		 */
		@Override
		public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			int count;
			String finalWhere;

			switch (mUriMatcher.match(uri)) {
			case MAIN:
				// If URI is main table, update uses incoming where clause and
				// args.
				count = db.update(MainTable.TABLE_NAME, values, where, whereArgs);
				break;

			case MAIN_ID:
				// If URI is for a particular row ID, update is based on
				// incoming
				// data but modified to restrict to the given ID.
				finalWhere = DatabaseUtilsCompat.concatenateWhere(MainTable._ID + " = " + ContentUris.parseId(uri),
						where);
				count = db.update(MainTable.TABLE_NAME, values, finalWhere, whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			getContext().getContentResolver().notifyChange(uri, null);

			return count;
		}
	}

	public class AppCursorAdapter extends BaseAdapter {

		Cursor cusor;

		public AppCursorAdapter() {
		}

		@Override
		public int getCount() {
			return cusor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}

	}

	public class AppListAdapter extends SimpleCursorAdapter {
		// HashMap<String, WeakReference<Bitmap>> imageCache;

		// Cursor cursor;

		// int layout;

		public AppListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// imageCache = new HashMap<String, WeakReference<Bitmap>>();
			// this.cursor = c;
			// this.layout = layout;
		}

		// class ViewHolder {
		// TextView textView;
		// TextView priceTextView;
		// TextView rebateTextView;
		// TextView boughtTextView;
		// ImageView imageView;
		// }

		// @Override
		// public View getView(int position, View convertView, ViewGroup parent)
		// {
		// Log.d(TAG, "getView "+ position);
		// if (!mCursor.moveToPosition(position)) {
		// throw new IllegalStateException("couldn't move cursor to position " +
		// position);
		// }
		//
		// View view;
		// if (convertView == null) {
		// view = super.newView(mContext, mCursor, parent);
		// } else {
		// view = convertView;
		// }
		// ViewHolder holder = (ViewHolder) view.getTag();
		// if (holder == null) {
		// holder = new ViewHolder();
		// view.setTag(holder);
		// holder.textView = (TextView) view.findViewById(R.id.text);
		// holder.imageView = (ImageView) view.findViewById(R.id.icon);
		// }
		// Cursor cursor = mCursor;
		//
		// String text =
		// cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_DATA));
		// holder.textView.setText(text);
		//
		// ImageView imageView = holder.imageView;
		// String small_image_url = cursor.getString(cursor
		// .getColumnIndexOrThrow(MainTable.COLUMN_NAME_SMALL_IMAGE_URL));
		// String fileName =
		// cursor.getString(cursor.getColumnIndexOrThrow(MainTable._ID)) +
		// ".jpg";
		// ImageDownloader downloader = new ImageDownloader(fileName);
		// downloader.download(small_image_url, imageView);

		// WeakReference<Bitmap> bitmapR = imageCache.get(small_image_url);

		// if (bitmapR != null) {
		// Bitmap bitmap = bitmapR.get();
		// if (bitmap != null) { // 有缓存
		// Log.d(TAG, "load Image cache ");
		// imageView.setImageBitmap(bitmap);
		// }
		// } else {
		// if
		// (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		// {
		// String fileName =
		// cursor.getString(cursor.getColumnIndexOrThrow(MainTable._ID)) +
		// ".jpg";
		// Bitmap bitmap = retrieveCacheImage(fileName);
		// if (bitmap != null) {
		// imageView.setImageBitmap(bitmap);
		// imageCache.put(small_image_url, new WeakReference<Bitmap>(bitmap));
		// } else {
		// imageView.setTag(small_image_url);
		// asyncwork
		// ImageLoader loader = new ImageLoader(small_image_url, null);
		// loader.execute(small_image_url);
		// }
		// } else {
		// Log.e(TAG, "Unreadable");
		// }

		// }

		// return view;
		// }

		// @Override
		// public View newView(Context context, Cursor cursor, ViewGroup parent)
		// {
		// Log.d(TAG, "newView");
		// return super.newView(context, cursor, parent);
		// }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String wap_url = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_WAP_URL));
			view.setTag(wap_url);
			View v = view.findViewById(R.id.text);
			if (v != null) {
				String text = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_DATA));
				((TextView) v).setText(text);
			}

			v = view.findViewById(R.id.price_text);
			if (v != null) {
				String text = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_PRICE));
				Float flt = Float.parseFloat(text);

				((TextView) v).setText(getString(R.string.RMB) + String.format("%.0f", flt));
			}

			v = view.findViewById(R.id.rebate_text);
			if (v != null) {
				String text = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_REBATE));
				((TextView) v).setText("/" + text + getString(R.string.rebate));
			}

			v = view.findViewById(R.id.bought_text);
			if (v != null) {
				String text = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.COLUMN_NAME_BOUGHT));
				((TextView) v).setText(text);
			}

			ImageView imageView = ((ImageView) view.findViewById(R.id.icon));
			if (imageView != null) {
				String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MainTable._ID)) + ".jpg";
				String small_image_url = cursor.getString(cursor
						.getColumnIndexOrThrow(MainTable.COLUMN_NAME_SMALL_IMAGE_URL));
				ImageDownloader downloader = new ImageDownloader(fileName);
				downloader.download(small_image_url, imageView);

			}
		}

		// class ImageLoader extends AsyncTask<String, Void, Bitmap> {
		//
		// String small_image_url;
		// String fileName;
		//
		// public ImageLoader(String small_image_url, String fileName) {
		// this.small_image_url = small_image_url;
		// this.fileName = fileName;
		// }
		//
		// @Override
		// protected Bitmap doInBackground(String... params) {
		// String url = params[0];
		// Bitmap bitmap = NetUtils.downloadBitmap(url);
		// return bitmap;
		// }
		//
		// @Override
		// protected void onPostExecute(Bitmap result) {
		// // storeCacheImage(fileName, result);
		// imageCache.put(small_image_url, new WeakReference<Bitmap>(result));
		//
		// ListView view = getListView();
		// ImageView imageView = (ImageView)
		// view.findViewWithTag(small_image_url);
		// if(imageView!=null){
		// imageView.setImageBitmap(result);
		// }
		// }
		// }

		// Bitmap retrieveCacheImage(String fileName) {
		// File externalCacheDir = Environment.getExternalStorageDirectory();
		// File dir = new File(externalCacheDir, "tuan");
		// if (!dir.exists())
		// return null;
		// File file = new File(dir.getAbsoluteFile() + File.separator +
		// fileName);
		// if (file.exists()) {
		// FileInputStream fis = null;
		// try {
		// fis = new FileInputStream(file);
		// Bitmap bitmap = BitmapFactory.decodeStream(fis);
		// return bitmap;
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } finally {
		// if (fis != null)
		// try {
		// fis.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// return null;
		// }
		//
		// boolean storeCacheImage(String fileName, Bitmap bitmap) {
		// File externalCacheDir = Environment.getExternalStorageDirectory();
		// if (externalCacheDir == null)
		// return false;
		// // Log.d(TAG, externalCacheDir.getAbsolutePath());
		// File dir = new File(externalCacheDir, "tuan");
		// if (!dir.exists()) {
		// dir.mkdirs();
		// // Log.d(TAG, dir.getAbsolutePath()+"-----"+bool);
		// }
		// File file = new File(dir + File.separator + fileName);
		// // Log.d(TAG, file.getAbsolutePath());
		// if (!file.exists())
		// try {
		// file.createNewFile();
		// } catch (IOException e) {
		// e.printStackTrace();
		// return false;
		// }
		//
		// if (bitmap == null)
		// return false;
		//
		// BufferedOutputStream bos = null;
		// try {
		//
		// bos = new BufferedOutputStream(new FileOutputStream(file));
		// boolean bool = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		// return bool;
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } finally {
		// if (bos != null) {
		// try {
		// bos.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// return false;
		// }
	}

	static final int CITY_ID = -1;
	SimpleCursorAdapter mAdapter;

	AsyncTask<Void, Void, List<Display>> mPopulatingTask;

	int city_id;
	int start = 0;
	int end = 0;
	int count = -1;
	View footer;
	TextView footerTextView;
	private ProgressBar footerBar;
	TextView hintTextView;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText("");
		setHasOptionsMenu(true);

		footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer_view, null, false);
		getListView().addFooterView(footer);
		footerTextView = (TextView) footer.findViewById(R.id.text);
		footerBar = (ProgressBar) footer.findViewById(R.id.pg);
		hintTextView = (TextView) footer.findViewById(R.id.textView1);
		footerTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				footerTextView.setVisibility(View.GONE);
				footerBar.setVisibility(View.VISIBLE);
				hintTextView.setVisibility(View.INVISIBLE);
				populate();
			}
		});

		ContentResolver cr = getActivity().getContentResolver();
		cr.delete(MainTable.CONTENT_URI, null, null);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new AppListAdapter(getActivity(), R.layout.list_item_icon_text, null,
				new String[] { MainTable.COLUMN_NAME_DATA }, new int[] { R.id.text }, 0);
		setListAdapter(mAdapter);

		Bundle bundle = getArguments();
		city_id = bundle.getInt("city_id");

		setListShown(false);

		// final ContentResolver cr = getActivity().getContentResolver();
		// cr.delete(MainTable.CONTENT_URI, null, null);

		// getLoaderManager().initLoader(0, null, this);

		// populate();

		getCount();
	}

	static final String[] PROJECTION = new String[] { MainTable._ID, MainTable.COLUMN_NAME_WAP_URL,
			MainTable.COLUMN_NAME_SMALL_IMAGE_URL, MainTable.COLUMN_NAME_DATA, MainTable.COLUMN_NAME_BOUGHT,
			MainTable.COLUMN_NAME_PRICE, MainTable.COLUMN_NAME_REBATE };

	// @Override
	// public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
	// CursorLoader cl = new CursorLoader(getActivity(), MainTable.CONTENT_URI,
	// PROJECTION, null, null, null);
	// cl.setUpdateThrottle(500); // update at most every 2 seconds.
	// return cl;
	// }

	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {
		// Insert desired behavior here.
		Log.i("LoaderCustom", "Item clicked: " + position);
		String wap_url = (String) view.getTag();
		if (wap_url != null) {
			Log.i("LoaderCustom", "Item clicked: " + wap_url);
			Intent intent = new Intent(getActivity(), DisplayActivity.class);
			intent.putExtra("wap_url", wap_url);
			startActivity(intent);
		}
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// SubMenu sub = menu.addSubMenu(0, CITY_ID, 0, getString(R.string.city));
	//
	// String[] city = getResources().getStringArray(R.array.cities_array);
	// String[] city_id =
	// getResources().getStringArray(R.array.cities_array_id);
	// Map<String, String> maps = new LinkedHashMap<String, String>();
	// for (int i = 0; i < city_id.length; i++) {
	// maps.put(city_id[i], city[i]);
	// sub.add(0, i, 0, city[i]);
	// }
	// sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
	// MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (item.getItemId() != CITY_ID) {
	// int index = item.getItemId();
	// Log.d(TAG, "itemID:" + index);
	//
	// if (mPopulatingTask != null) {
	//
	// boolean bool = mPopulatingTask.cancel(true);
	// Log.d(TAG, "cancel task "+bool);
	// }
	//
	// setListShown(false);
	// setTitle(index);
	//
	// ContentResolver cr = getActivity().getContentResolver();
	// cr.delete(MainTable.CONTENT_URI, null, null);
	// // Cursor cusor = cr.query(MainTable.CONTENT_URI, PROJECTION, null,
	// // null, null);
	// // mAdapter.swapCursor(cusor);
	// // mAdapter.notifyDataSetChanged();
	//
	// getCount();
	// // refresh();
	// }
	//
	// return true;
	// }

	// void setTitle(int index) {
	// String[] city = getResources().getStringArray(R.array.cities_array);
	// String[] city_ids =
	// getResources().getStringArray(R.array.cities_array_id);
	// Map<String, String> maps = new LinkedHashMap<String, String>();
	// for (int i = 0; i < city_ids.length; i++) {
	// maps.put(city_ids[i], city[i]);
	// }
	//
	// StringBuilder builder = new
	// StringBuilder(getString(R.string.today_tuan));
	// builder.append("-" + maps.get(city_ids[index]));
	// getActivity().setTitle(builder);
	//
	// city_id = Integer.parseInt(city_ids[index]);
	// }

	// @Override
	// public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	// mAdapter.swapCursor(data);
	// // The list should now be shown.
	// if (isResumed()) {
	// setListShown(true);
	// } else {
	// setListShownNoAnimation(true);
	// }
	// }

	// @Override
	// public void onLoaderReset(Loader<Cursor> arg0) {
	// mAdapter.swapCursor(null);
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();

		// ContentResolver cr = getActivity().getContentResolver();
		// cr.delete(MainTable.CONTENT_URI, null, null);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mPopulatingTask != null) {
			Log.d(TAG, "task status:" + mPopulatingTask.getStatus());
			boolean bool = mPopulatingTask.cancel(true);
			Log.d(TAG, "cancel task " + bool);
		}
	}

	/**
	 * 获取总共的数据
	 */
	void getCount() {

		String content = NetUtils.downloadData(city_id, 1, 1);
		XMLParser parser = new XMLParser();
		count = parser.getCount(content);

		Log.d(TAG, "count:" + count);
		if (count >= 1) {
			start = 1;
			end = (int) Math.ceil(count * 1.0 / Constant.ITEMS);
			populate();
		} else {
			setListShown(true);
			Toast.makeText(getActivity(), "没有数据可以加载!", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 获取数据
	 */
	void populate() {
		final ContentResolver cr = getActivity().getContentResolver();
		if (mPopulatingTask != null) {
			mPopulatingTask.cancel(false);
		}
		mPopulatingTask = new AsyncTask<Void, Void, List<Display>>() {
			@Override
			protected List<Display> doInBackground(Void... params) {
				Log.d(TAG, "start:" + start);
				String content = NetUtils.downloadData(city_id, start, Constant.ITEMS);

				XMLParser parser = new XMLParser();
				List<Display> lists = parser.parseXML(content);
				// Log.d(TAG, lists.size() + "");
				for (Display display : lists) {
					ContentValues values = new ContentValues();
					values.put(MainTable._ID, Integer.parseInt(display.getGid()));
					values.put(MainTable.COLUMN_NAME_DATA, display.getTitle());
					values.put(MainTable.COLUMN_NAME_SMALL_IMAGE_URL, display.getSmall_image_url());
					values.put(MainTable.COLUMN_NAME_WAP_URL, display.getWap_url());
					values.put(MainTable.COLUMN_NAME_BOUGHT, display.getBought());
					values.put(MainTable.COLUMN_NAME_PRICE, display.getPrice());
					values.put(MainTable.COLUMN_NAME_REBATE, display.getRebate());

					cr.insert(MainTable.CONTENT_URI, values);
					// try {
					// Thread.sleep(250);
					// } catch (InterruptedException e) {
					// }
				}
				start++;
				return lists;
			}

			@Override
			protected void onCancelled(List<Display> result) {
				Log.d(TAG, "cancel task");
				for (Display display : result) {

					cr.delete(MainTable.CONTENT_URI, MainTable._ID + "=?",
							new String[] { "" + Integer.parseInt(display.getGid()) });

				}
			}

			@Override
			protected void onCancelled() {
				Log.d(TAG, "cancel task no result");
			}

			@Override
			protected void onPostExecute(List<Display> result) {
				setListShown(true);
				Cursor cusor = cr.query(MainTable.CONTENT_URI, PROJECTION, null, null, null);
				mAdapter.swapCursor(cusor);
				mAdapter.notifyDataSetChanged();

				footerTextView.setVisibility(View.VISIBLE);
				footerBar.setVisibility(View.GONE);
				hintTextView.setVisibility(View.VISIBLE);
				int _count = cusor.getCount();
				hintTextView.setText("已有"+_count+"项,共"+count+"项");
				
				if (start > end) {
					//getListView().removeFooterView(footer);
					footerTextView.setVisibility(View.GONE);
					footerBar.setVisibility(View.GONE);
					hintTextView.setText("共"+count+"项");
					
					Toast.makeText(getActivity(), "没有数据可以加载!", Toast.LENGTH_LONG).show();
				}

			}
		};
		mPopulatingTask.execute((Void[]) null);
	}

	// void clear() {
	// final ContentResolver cr = getActivity().getContentResolver();
	// if (mPopulatingTask != null) {
	// mPopulatingTask.cancel(false);
	// mPopulatingTask = null;
	// }
	// AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
	// @Override
	// protected Void doInBackground(Void... params) {
	// cr.delete(MainTable.CONTENT_URI, null, null);
	//
	// // 清除文件夹
	// if
	// (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
	// {
	// File externalCacheDir = Environment.getExternalStorageDirectory();
	// File dir = new File(externalCacheDir, "tuan");
	// if (dir.exists()) {
	// for (File file : dir.listFiles()) {
	// file.delete();
	// }
	// }
	//
	// }
	//
	// return null;
	// }
	// };
	// task.execute((Void[]) null);
	// }

}
