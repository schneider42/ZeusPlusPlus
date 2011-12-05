package de.ccc.muc.zeusplus.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class DefaultMenu {
	private static final int MENU_ABOUT = 1;

	public static void addAbout(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);
	}

	public static boolean onOptionsItemSelected(MenuItem item,
			final Activity act) {
		int itemid = item.getItemId();

		if (MENU_ABOUT == itemid) {
			new AboutDialog(act).show();
			return true;
		}

		return false;
	}
}
