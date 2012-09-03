package com.example.tuan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DisplayActivity extends Activity {
	WebView webView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		Intent intent = getIntent();
		if (intent != null) {
			String url = intent.getExtras().getString("wap_url");
			if (url != null) {
				webView = (WebView) findViewById(R.id.webView1);
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						view.loadUrl(url);
						return true;
					}
				});

				webView.loadUrl(url);
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (webView != null && webView.canGoBack()
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			webView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
