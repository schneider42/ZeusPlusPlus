package de.ccc.muc.zeusplus.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.ccc.muc.zeusplus.misc.QLog;
import de.ccc.muc.zpp.R;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		setTitle("About");
		setVersion();
		setButton(this);
	}

	private void setVersion() {
		String versionCurrent = getVersionName(getContext(), "?.?.?");
		((TextView) findViewById(R.id.aboutAppVersion)).setText("v"
				+ versionCurrent);
	}

	private void setButton(final Dialog d) {
		Button btn;

		btn = (Button) findViewById(R.id.aboutBtnOk);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				d.dismiss();
			}
		});

		btn = (Button) findViewById(R.id.aboutBtnDetails);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = Uri.parse("http://wiki.muc.ccc.de/moodlamp");
				d.getContext().startActivity(
						new Intent(Intent.ACTION_VIEW, uri));
			}
		});

	}

	public static String getVersionName(Context ctx, String def) {
		PackageManager pm = ctx.getPackageManager();
		try {
			return pm.getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			QLog.e(AboutDialog.class.getName(),
					"Could not find current version name: " + e.getMessage());
			return def;
		}
	}
}
