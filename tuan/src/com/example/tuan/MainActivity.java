package com.example.tuan;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.example.tuan.TodayTuanFragment.MainTable;

public class MainActivity extends SherlockFragmentActivity {
	static final String TAG = "MainActivity";

	static final int CITY_ID = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		int city_id = setCityId(0);

		Fragment newFragment = new TodayTuanFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("city_id", city_id);
		newFragment.setArguments(bundle);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.simple_fragment, newFragment).commit();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu(0, CITY_ID, 0, getString(R.string.city));

		String[] city = getResources().getStringArray(R.array.cities_array);
		String[] city_id = getResources().getStringArray(R.array.cities_array_id);
		Map<String, String> maps = new LinkedHashMap<String, String>();
		for (int i = 0; i < city_id.length; i++) {
			maps.put(city_id[i], city[i]);
			sub.add(0, i, 0, city[i]);
		}
		sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() != CITY_ID) {
			int index = item.getItemId();
			Log.d(TAG, "itemID:" + index);

			int city_id = setCityId(index);

			Fragment newFragment = new TodayTuanFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("city_id", city_id);
			newFragment.setArguments(bundle);
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.simple_fragment, newFragment);
			ft.commit();

		}

		return true;
	}

	int setCityId(int index) {
		String[] city = getResources().getStringArray(R.array.cities_array);
		String[] city_ids = getResources().getStringArray(R.array.cities_array_id);
		Map<String, String> maps = new LinkedHashMap<String, String>();
		for (int i = 0; i < city_ids.length; i++) {
			maps.put(city_ids[i], city[i]);
		}

		StringBuilder builder = new StringBuilder(getString(R.string.today_tuan));
		builder.append("-" + maps.get(city_ids[index]));
		setTitle(builder);
		return Integer.parseInt(city_ids[index]);

	}
}
