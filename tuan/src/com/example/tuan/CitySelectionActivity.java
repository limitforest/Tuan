package com.example.tuan;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.ContentProvider;
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
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.tuan.TodayTuanFragment.MainTable;
import com.example.tuan.view.SideBar;

public class CitySelectionActivity extends Activity {
	static final String TAG = "CitySelectionActivity";
	public static final String AUTHORITY = "com.example.tuan.TodayTuan";
	DatabaseHelper mOpenHelper;

	public static final class CITYTable implements BaseColumns {

		private CITYTable() {
		}

		public static final String TABLE_NAME = "cities";

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cities");

		public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/cities/");

		public static final String DEFAULT_SORT_ORDER = "city_pinyin COLLATE LOCALIZED ASC";

		/**
		 * The MIME type of {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.api-demos-throttle";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * row.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.api-demos-throttle";

		public static final String COLUMN_NAME_ID = "_id";

		public static final String COLUMN_NAME_CITY = "city";
		public static final String COLUMN_NAME_CITY_ID = "city_id";
		public static final String COLUMN_NAME_CITY_PINYIN = "city_pinyin";

	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "todayTuan.db";
		private static final int DATABASE_VERSION = 2;

		DatabaseHelper(Context context) {

			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "onCreate");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion > newVersion)
				return;

			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			// db.execSQL("DROP TABLE  IF EXISTS "+ CITYTable.TABLE_NAME);
			// onCreate(db);
			db.execSQL("CREATE TABLE  IF NOT EXISTS " + CITYTable.TABLE_NAME + " (" + CITYTable._ID
					+ " INTEGER PRIMARY KEY," + CITYTable.COLUMN_NAME_CITY + " TEXT ," + CITYTable.COLUMN_NAME_CITY_ID
					+ " TEXT  ," + CITYTable.COLUMN_NAME_CITY_PINYIN + " TEXT" + ");");

			Log.d(TAG, "onUpgrade");

		}

	}

	public static class CityProvider extends ContentProvider {
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
		public CityProvider() {
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
			mNotesProjectionMap.put(MainTable.COLUMN_NAME_DISPLAY_ID, MainTable.COLUMN_NAME_DISPLAY_ID);
		}

		@Override
		public boolean onCreate() {
			mOpenHelper = new DatabaseHelper(getContext());
			return true;
		}

		@Override
		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

			// Constructs a new query builder and sets its table name
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(CITYTable.TABLE_NAME);

			// switch (mUriMatcher.match(uri)) {
			// case MAIN:
			// // If the incoming URI is for main table.
			// qb.setProjectionMap(mNotesProjectionMap);
			// break;
			//
			// case MAIN_ID:
			// // The incoming URI is for a single row.
			// qb.setProjectionMap(mNotesProjectionMap);
			// qb.appendWhere(MainTable._ID + "=?");
			// selectionArgs =
			// DatabaseUtilsCompat.appendSelectionArgs(selectionArgs,
			// new String[] { uri.getLastPathSegment() });
			// break;
			//
			// default:
			// throw new IllegalArgumentException("Unknown URI " + uri);
			// }

			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = CITYTable.DEFAULT_SORT_ORDER;
			}

			SQLiteDatabase db = mOpenHelper.getReadableDatabase();

			Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

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
				return CITYTable.CONTENT_TYPE;
			case MAIN_ID:
				return CITYTable.CONTENT_ITEM_TYPE;
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

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	class City {
		String id;
		String name;
		String pinyin;
	}

	class InertTable implements Runnable {

		@Override
		public void run() {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			final List<City> cities = new ArrayList<CitySelectionActivity.City>();
			final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			try {
				SAXParser parser = factory.newSAXParser();

				DefaultHandler handler = new DefaultHandler() {
					StringBuilder sb;
					City city;

					@Override
					public void characters(char[] ch, int start, int length) throws SAXException {
						// super.characters(ch, start, length);
						sb.append(new String(ch, start, length));
					}

					@Override
					public void startElement(String uri, String localName, String qName, Attributes attributes)
							throws SAXException {
						// super.startElement(uri, localName, qName,
						// attributes);
						sb = new StringBuilder();
						if (qName.equals("city")) {
							city = new City();
						} else if (qName.equals("id")) {
						} else if (qName.equals("name")) {
						}
					}

					@Override
					public void endElement(String uri, String localName, String qName) throws SAXException {
						// super.endElement(uri, localName, qName);
						if (qName.equals("city")) {

							StringBuilder sbb = new StringBuilder();
							for (int i = 0; i < sb.toString().length(); i++) {
								char ch = sb.toString().charAt(i);
								try {
									String[] temp = PinyinHelper.toHanyuPinyinStringArray(ch, format);
									if (temp != null) {
										sbb.append(temp[0]);
									}
								} catch (BadHanyuPinyinOutputFormatCombination e) {
									e.printStackTrace();
								}
							}

							city.pinyin = sbb.toString();

							cities.add(city);
						} else if (qName.equals("id")) {
							city.id = sb.toString();

						} else if (qName.equals("name")) {
							city.name = sb.toString();
						}
					}

				};
				InputStream is = getResources().openRawResource(R.raw.cities);
				parser.parse(is, handler);
				is.close();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DatabaseHelper mOpenHelper = new DatabaseHelper(CitySelectionActivity.this);
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			db.execSQL("CREATE TABLE  IF NOT EXISTS " + CITYTable.TABLE_NAME + " (" + CITYTable._ID
					+ " INTEGER PRIMARY KEY," + CITYTable.COLUMN_NAME_CITY + " TEXT ," + CITYTable.COLUMN_NAME_CITY_ID
					+ " TEXT  ," + CITYTable.COLUMN_NAME_CITY_PINYIN + " TEXT" + ");");

			// ContentResolver cr = getContentResolver();
			for (City city : cities) {
				db.execSQL("insert into " + CITYTable.TABLE_NAME + "(city,city_id,city_pinyin) values(?,?,?)",
						new String[] { city.name, city.id, city.pinyin });
				// ContentValues values = new ContentValues();
				// values.put(CITYTable.COLUMN_NAME_CITY, city.name);
				// values.put(CITYTable.COLUMN_NAME_CITY_ID, city.id);
				// values.put(CITYTable.COLUMN_NAME_CITY_PINYIN, city.pinyin);
				// cr.insert(CITYTable.CONTENT_URI, values);
			}

			System.out.println("insert successfully");
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_selection);

	//	new Thread(new InertTable()).start();

		ListView view = (ListView) findViewById(R.id.listView2);
		// SimpleAdapter adpater = new SimpleAdapter(this, data,
		// android.R.layout.simple_list_item_1, new S, to)

		mOpenHelper = new DatabaseHelper(this);
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor cursor = db.query(CITYTable.TABLE_NAME, new String[] { CITYTable.COLUMN_NAME_ID,
				CITYTable.COLUMN_NAME_CITY_PINYIN, CITYTable.COLUMN_NAME_CITY, CITYTable.COLUMN_NAME_CITY_ID }, null,
				null, null, null, CITYTable.DEFAULT_SORT_ORDER);

		// Cursor cursor = cr.query(CITYTable.CONTENT_URI, new String[] {
		// CITYTable.COLUMN_NAME_CITY_PINYIN,
		// CITYTable.COLUMN_NAME_CITY }, null, null, null);
		// List<String> lists = new ArrayList<String>();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
				new String[] { CITYTable.COLUMN_NAME_CITY }, new int[] { android.R.id.text1 }) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				super.bindView(view, context, cursor);

				String text = cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY_ID));
				String text2 = cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY));
				view.setTag(text + "#" + text2);
			}
		};
		// ArrayAdapter<String> adapter = new
		// ArrayAdapter<String>(this,android.R.id.text1 , lists);
		view.setAdapter(adapter);

		view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = (String) view.getTag();
				if (text instanceof String) {
					String[] ss = text.split("#");
					if (ss != null && ss.length == 2) {
						Intent data = new Intent();
						data.putExtra("city_id", ss[0]);
						data.putExtra("city", ss[1]);
						setResult(0x01, data);
						finish();
					}
				}
			}
		});
		int[] az = new int[27];
		while (cursor.moveToNext()) {
			String text = cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY_PINYIN));
			char c = text.toLowerCase().charAt(0);
			int index = c - 'a' + 1;
			az[index]++;
		}
		for (int i = 1; i < 27; i++) {
			az[i] += az[i - 1];
		}

		SideBar bar = (SideBar) findViewById(R.id.sidebar1);
		bar.setListView(view, az);

		cursor = db.query(CITYTable.TABLE_NAME, new String[] { CITYTable.COLUMN_NAME_ID,
				CITYTable.COLUMN_NAME_CITY_PINYIN, CITYTable.COLUMN_NAME_CITY, CITYTable.COLUMN_NAME_CITY_ID }, null,
				null, null, null, CITYTable.DEFAULT_SORT_ORDER);
		AutoCompleteTextView auto = (AutoCompleteTextView) findViewById(R.id.auto1);
		SimpleCursorAdapter ad = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, cursor,
				new String[] { CITYTable.COLUMN_NAME_CITY }, new int[] { android.R.id.text1 }) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				super.bindView(view, context, cursor);
				String text = cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY_ID));
				String text2 = cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY));
				view.setTag(text + "#" + text2);

			}

			@Override
			public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
				String selection = CITYTable.COLUMN_NAME_CITY + " LIKE ?";
				Cursor cursor = db.query(CITYTable.TABLE_NAME, new String[] { CITYTable.COLUMN_NAME_ID,
						CITYTable.COLUMN_NAME_CITY_PINYIN, CITYTable.COLUMN_NAME_CITY, CITYTable.COLUMN_NAME_CITY_ID },
						selection, new String[] { constraint + "%" }, null, null, CITYTable.DEFAULT_SORT_ORDER);
				return cursor;
			}

			@Override
			public CharSequence convertToString(Cursor cursor) {

				return cursor.getString(cursor.getColumnIndexOrThrow(CITYTable.COLUMN_NAME_CITY));
			}
		};

		auto.setAdapter(ad);
		auto.setThreshold(1);
		auto.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = (String) view.getTag();
				if (text instanceof String) {
					String[] ss = text.split("#");
					if (ss != null && ss.length == 2) {
						Intent data = new Intent();
						data.putExtra("city_id", ss[0]);
						data.putExtra("city", ss[1]);
						setResult(0x01, data);
						finish();
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenHelper != null) {
			mOpenHelper.close();
		}
	}

}
