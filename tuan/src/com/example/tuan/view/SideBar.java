package com.example.tuan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SideBar extends View {
	private char[] l;
	// private SectionIndexer sectionIndexter = null;
	private ListView list;
	// private final int m_nItemHeight = 22;
	int[] az;
	PopupWindow mPopupWindow;
	float height;

	public SideBar(Context context) {
		super(context);
		init();
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		l = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
				'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
		setBackgroundColor(0x44FFFFFF);
	}

	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setListView(ListView _list, int[] az) {
		list = _list;
		// sectionIndexter = (SectionIndexer) _list.getAdapter();
		this.az = az.clone();
	}

	public boolean onTouchEvent(MotionEvent event) {
		
		int i = (int) event.getY();
		int idx = Math.round(i / height);
		if (idx >= l.length) {
			idx = l.length - 1;
		} else if (idx < 0) {
			idx = 0;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			if (mPopupWindow == null) {
				LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View view = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				mPopupWindow = new PopupWindow(view, 100, 100);
			}

			if (!mPopupWindow.isShowing()) {
				mPopupWindow.showAtLocation(list, Gravity.CENTER, 0, 0);
			}
			TextView tv = (TextView) mPopupWindow.getContentView().findViewById(android.R.id.text1);
			tv.setText(l[idx] + "");

			Log.d("onTouchEvent", l[idx] + "");
			if (idx >= 0 && idx <= 25)
				list.setSelection(az[idx]);
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mPopupWindow != null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
				mPopupWindow = null;
			}
			return true;
		}
		return super.onTouchEvent(event);
		
	}

	public int getFontHeight(Paint paint, float fontSize) {
		paint.setTextSize(fontSize);
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.top) + 2;
	}

	boolean first = true;
	int _size = 0;

	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setTextAlign(Paint.Align.CENTER);
		// paint.setColor(0xffA6A9AA);

		// height = (getMeasuredHeight() - 26 * height) / 26;
		// ÅÐ¶Ï×îºÏÊÊµÄsize
		int height1;
		if (first) {
			int size = 50;
			while (true) {
				int total = 26 * getFontHeight(paint, size);
				if (total > getMeasuredHeight()) {
					size--;
					// paint.setTextSize(size);
				} else {
					// paint.setTextSize(size);
					break;
				}
			}
			first = false;
			_size = size;
		} else {
			paint.setTextSize(_size);
		}
		height1 = getFontHeight(paint, _size);
		height = height1;
		Log.d("ondraw", height1 + "");
		float widthCenter = getMeasuredWidth() / 2;
		for (int i = 0; i < l.length; i++) {
			canvas.drawText(String.valueOf(l[i]), widthCenter, (i + 1) * height1, paint);
		}
		super.onDraw(canvas);
	}
}
