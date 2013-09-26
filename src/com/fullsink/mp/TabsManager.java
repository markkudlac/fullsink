package com.fullsink.mp;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class TabsManager {
	private MainActivity mnact;
	int ACTIVE_TAB = 0;

	public TabsManager(MainActivity mnact){
		this.mnact = mnact;
	}
	
	void setActiveMenu(int select) {
		Button tbtn;
		clearActiveMenu();
		tbtn = (Button) mnact.findViewById(select);
		tbtn.setTypeface(Typeface.DEFAULT_BOLD);
		tbtn.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		tbtn.setClickable(false);
		ACTIVE_TAB = select;
	}
	
	void clearActiveMenu() {
		Button tbtn;

		ViewGroup menu = (ViewGroup) mnact.findViewById(R.id.topMenu);
		for (int i = 2; i < menu.getChildCount() - 1; i++) {
			tbtn = (Button) menu.getChildAt(i);
			tbtn.setPaintFlags(0);
			tbtn.setTypeface(Typeface.DEFAULT);
			tbtn.setClickable(true);
			tbtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mnact.click(view);
				}
			});
		}

		((Button) mnact.findViewById(R.id.btnReceiver)).setClickable(true);
		((ImageView) mnact.findViewById(R.id.imgReceiver))
				.setImageResource(R.drawable.fs_receive_white);
	}



	public int getActiveTab() {
		return ACTIVE_TAB;
	}
}
