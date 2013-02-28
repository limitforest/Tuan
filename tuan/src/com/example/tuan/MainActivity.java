package com.example.tuan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.tuan.TodayTuanFragment.MainTable;
import com.example.tuan.net.NetUtils;
import com.example.tuan.parser.Display;
import com.example.tuan.parser.XMLParser;
import com.example.tuan.util.Constant;

public class MainActivity extends SherlockListActivity {
	class PopulateDataAsyncTask extends AsyncTask<Void, Void, List<Display>> {

		boolean first = true;

		@Override
		protected List<Display> doInBackground(Void... params) {
			Log.d(TAG, "start:" + start);
			String content = NetUtils.downloadData(city_id, start, Constant.ITEMS);

			XMLParser parser = new XMLParser();
			List<Display> lists = parser.parseXML(content);
			// Log.d(TAG, lists.size() + "");
			ContentResolver cr = getContentResolver();
			for (Display display : lists) {

				ContentValues values = new ContentValues();
				values.put(MainTable._ID, Integer.parseInt(display.getGid()));
				values.put(MainTable.COLUMN_NAME_DATA, display.getTitle());
				values.put(MainTable.COLUMN_NAME_SMALL_IMAGE_URL, display.getSmall_image_url());
				values.put(MainTable.COLUMN_NAME_WAP_URL, display.getWap_url());
				values.put(MainTable.COLUMN_NAME_BOUGHT, display.getBought());
				values.put(MainTable.COLUMN_NAME_PRICE, display.getPrice());
				values.put(MainTable.COLUMN_NAME_REBATE, display.getRebate());
				values.put(MainTable.COLUMN_NAME_DISPLAY_ID, ++cursorCount);

				cr.insert(MainTable.CONTENT_URI, values);
			}

			// if (isCancelled()) {
			// for (Display display : lists) {
			// int row = cr.delete(MainTable.CONTENT_URI, MainTable._ID + "=?",
			// new String[] { "" + Integer.parseInt(display.getGid()) });
			// Log.d(TAG, "delte display in row " + row);
			// }
			//
			// }
			if (lists.size() > 0)
				start++;
			return lists;
		}

		@Override
		protected void onCancelled(List<Display> result) {
			Log.d(TAG, "cancel task");
			ContentResolver cr = getContentResolver();
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
			if (first) {
				setListShown(true);
				first = false;
			}
			ContentResolver cr = getContentResolver();
			Cursor cusor = cr.query(MainTable.CONTENT_URI, PROJECTION, null, null, null);
			mAdapter.swapCursor(cusor);
			mAdapter.notifyDataSetChanged();

			// footerTextView.setVisibility(View.VISIBLE);
			footerBar.setVisibility(View.GONE);
			hintTextView.setVisibility(View.VISIBLE);
			int _count = cusor.getCount();
			hintTextView.setText("已有" + _count + "项,共" + count + "项");

			if (start > end) {
				// getListView().removeFooterView(footer);
				footerTextView.setVisibility(View.GONE);
				footerBar.setVisibility(View.GONE);
				hintTextView.setText("共" + count + "项");

				Toast.makeText(MainActivity.this, "没有数据可以加载!", Toast.LENGTH_LONG).show();
			}

		}

	}

	public class DisplayCursorAdapter extends SimpleCursorAdapter {

		ImageDownloader downloader = new ImageDownloader();

		public DisplayCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

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

				downloader.download(small_image_url, imageView, fileName);

			}
		}

	}

	class GetCount implements Runnable {

		@Override
		public void run() {
			String content = NetUtils.downloadData(city_id, 1, 1);
			XMLParser parser = new XMLParser();
			int _count = parser.getCount(content);
			Message message = new Message();
			message.arg1 = _count;
			handler.sendMessage(message);
		}
	}

	class GetCountHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			count = msg.arg1;
			Log.d(TAG, "count:" + count);

			if (count >= 1) {
				start = 1;
				end = (int) Math.ceil(count * 1.0 / Constant.ITEMS);
				populate();
			} else {
				getListView().removeFooterView(footer);

				setListShown(true);
				Toast.makeText(MainActivity.this, "没有数据可以加载!", Toast.LENGTH_LONG).show();
			}

		}

	}

	static final String TAG = "MainActivity";

	static final int CITY_ID = -1;
	SimpleCursorAdapter mAdapter;

	int city_id;
	String city_name;
	volatile int start = 0;
	int end = 0;
	int count = -1;
	int cursorCount = 0;
	View footer;
	TextView footerTextView;
	private ProgressBar footerBar;
	TextView hintTextView;
	Handler handler = new GetCountHandler();

	static final String[] PROJECTION = new String[] { MainTable._ID, MainTable.COLUMN_NAME_WAP_URL,
			MainTable.COLUMN_NAME_SMALL_IMAGE_URL, MainTable.COLUMN_NAME_DATA, MainTable.COLUMN_NAME_BOUGHT,
			MainTable.COLUMN_NAME_PRICE, MainTable.COLUMN_NAME_REBATE, MainTable.COLUMN_NAME_DISPLAY_ID };
	AsyncTask<Void, Void, List<Display>> mPopulatingTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		footer = getLayoutInflater().inflate(R.layout.footer_view, null, false);
		getListView().addFooterView(footer);
		footerTextView = (TextView) footer.findViewById(R.id.text);
		footerBar = (ProgressBar) footer.findViewById(R.id.pg);
		hintTextView = (TextView) footer.findViewById(R.id.textView1);
		footerTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (start <= end) {
					// footerTextView.setVisibility(View.VISIBLE);
					footerBar.setVisibility(View.VISIBLE);
					hintTextView.setVisibility(View.GONE);
					populate();
				}
			}
		});
		mAdapter = new DisplayCursorAdapter(this, R.layout.list_item_icon_text, null,
				new String[] { MainTable.COLUMN_NAME_DATA }, new int[] { R.id.text }, 0);
		setListAdapter(mAdapter);

		// DatabaseHelper mOpenHelper = new DatabaseHelper(this);
		// mOpenHelper.getWritableDatabase();

		setListShown(false);
//		moveDB();
		// Bundle bundle = getArguments();
		if (savedInstanceState != null) {
			city_id = savedInstanceState.getInt("city_id");
			city_name = savedInstanceState.getString("city_name");
		} else {
			city_id = 2419;// 无锡
			city_name = "北京";
		}
		setTitle(getString(R.string.title_activity_main) + "--" + city_name);

		ContentResolver cr = getContentResolver();
		cr.delete(MainTable.CONTENT_URI, null, null);

		new Thread(new GetCount()).start();
	}

	void setListShown(boolean b) {
		if (b) {
			View view = findViewById(R.id.progressBar1);
			view.setVisibility(View.GONE);
			view = findViewById(R.id.textView1);
			view.setVisibility(View.GONE);
			view = findViewById(android.R.id.list);
			view.setVisibility(View.VISIBLE);

		} else {
			View view = findViewById(R.id.progressBar1);
			view.setVisibility(View.VISIBLE);
			view = findViewById(R.id.textView1);
			view.setVisibility(View.VISIBLE);
			view = findViewById(android.R.id.list);
			view.setVisibility(View.GONE);
		}
	}

	/**
	 * 获取数据
	 */
	void populate() {
		if (mPopulatingTask != null) {
			mPopulatingTask.cancel(true);
		}
		mPopulatingTask = new PopulateDataAsyncTask();
		mPopulatingTask.execute((Void[]) null);

	}

	void moveDB() {
		try {
			String dbDirPath = "/data/data/com.test/databases";
			File dbDir = new File(dbDirPath);
			if (!dbDir.exists()) // 如果不存在该目录则创建
				dbDir.mkdir();
			File file = new File(dbDirPath + "/todayTuan.db");
			if (file.exists())
				return;

			InputStream is = getAssets().open("todayTuan.db");

			file.createNewFile();
			
			FileOutputStream os = new FileOutputStream(file);
			
			byte[] buffer = new byte[2048];
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
				os.write(buffer, 0, count);
			}
			is.close();
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (city_id != 0) {
			outState.putInt("city_id", city_id);
			outState.putString("city_name", city_name);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem sub = menu.add(0, CITY_ID, 0, getString(R.string.city));

		sub.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == CITY_ID) {
			if (mPopulatingTask != null) {
				mPopulatingTask.cancel(true);
			}
			// try {
			// mPopulatingTask.get(1,TimeUnit.SECONDS);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// } catch (ExecutionException e) {
			// e.printStackTrace();
			// } catch (TimeoutException e) {
			// e.printStackTrace();
			// }

			Intent intent = new Intent(this, CitySelectionActivity.class);
			startActivityForResult(intent, 0x01);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult");
		if (resultCode == 0x01) {
			String string = data.getStringExtra("city_id");
			try {
				city_id = Integer.parseInt(string);
			} catch (Exception e) {
			}
			String title = data.getStringExtra("city");
			city_name = title;
			setTitle(getString(R.string.title_activity_main) + "--" + title);
			setListShown(false);

			if (mPopulatingTask != null) {
				boolean bool = mPopulatingTask.cancel(true);
				if (bool) {
					try {
						List<Display> result = mPopulatingTask.get();
						if (result != null) {
							ContentResolver cr = getContentResolver();
							for (Display display : result) {

								cr.delete(MainTable.CONTENT_URI, MainTable._ID + "=?",
										new String[] { "" + Integer.parseInt(display.getGid()) });

							}
							Log.d(TAG, "delete display");
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}

			}

			ContentResolver cr = getContentResolver();
			cr.delete(MainTable.CONTENT_URI, null, null);
			mAdapter.swapCursor(null);
			mAdapter.notifyDataSetChanged();
			if (getListView().getFooterViewsCount() < 1)
				getListView().addFooterView(footer);
			new Thread(new GetCount()).start();

		}

	}

	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {
		// Insert desired behavior here.
		Log.i("LoaderCustom", "Item clicked: " + position);
		String wap_url = (String) view.getTag();
		if (wap_url != null) {
			Log.i("LoaderCustom", "Item clicked: " + wap_url);
			Intent intent = new Intent(this, DisplayActivity.class);
			intent.putExtra("wap_url", wap_url);
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ContentResolver cr = getContentResolver();
		// cr.delete(MainTable.CONTENT_URI, null, null);
		// Create an empty adapter we will use to display the loaded data.
		Log.d(TAG, "onResume");

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
