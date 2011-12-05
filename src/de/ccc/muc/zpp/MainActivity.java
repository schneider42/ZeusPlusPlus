package de.ccc.muc.zpp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.ccc.muc.zeusplus.service.Moodlamp;
import de.ccc.muc.zeusplus.service.MoodlampRegistryListener;
import de.ccc.muc.zeusplus.service.MoodlampService;
import de.ccc.muc.zeusplus.service.MoodlampService.MoodlampServiceBinder;
import de.ccc.muc.zeusplus.ui.ColorBlockView;
import de.ccc.muc.zeusplus.ui.DefaultMenu;
import de.ccc.muc.zeusplus.ui.LampsView;
import de.ccc.muc.zeusplus.ui.MultiColorBlockListener;
import de.ccc.muc.zeusplus.ui.MultiColorBlockView;
import de.ccc.muc.zpp.R;

public class MainActivity extends Activity implements MultiColorBlockListener,
		MoodlampRegistryListener {
	private static final String PREF_NAME = "ZeusPlusState";
	private static final String PREF_COLORS = "colors";

	private final Handler mHandlerAction = new Handler();

	private ColorBlockView mColorBlockView = null;
	private SeekBar mSeekBarR = null;
	private SeekBar mSeekBarG = null;
	private SeekBar mSeekBarB = null;
	private MultiColorBlockView mMultiColorBlockView;
	private LampsView mLamps = null;

	private MoodlampService moodlampService;
	private boolean moodlampServiceBound = false;

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MoodlampService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (moodlampServiceBound) {
			unbindService(mConnection);
			moodlampServiceBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			MoodlampServiceBinder binder = (MoodlampServiceBinder) service;
			moodlampService = binder.getService();
			moodlampServiceBound = true;
			// moodlampService.getRegistry().addListener(MainActivity.this);
			moodlampService.getRegistry().addListener(MainActivity.this);
			moodlampService.discoverStart();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			moodlampService.getRegistry().removeListener(MainActivity.this);
			moodlampServiceBound = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.main);

		mColorBlockView = (ColorBlockView) findViewById(R.id.colorBlock);
		mColorBlockView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireColor(true);
			}
		});

		mSeekBarR = (SeekBar) findViewById(R.id.seekR);
		mSeekBarR.setOnSeekBarChangeListener(new RGBSeekBarChangeListener());

		mSeekBarG = (SeekBar) findViewById(R.id.seekG);
		mSeekBarG.setOnSeekBarChangeListener(new RGBSeekBarChangeListener());

		mSeekBarB = (SeekBar) findViewById(R.id.seekB);
		mSeekBarB.setOnSeekBarChangeListener(new RGBSeekBarChangeListener());

		mMultiColorBlockView = (MultiColorBlockView) findViewById(R.id.predefinedColors1);
		mMultiColorBlockView.setListener(this);
		mMultiColorBlockView.setColors(getColors());

		mLamps = ((LampsView) findViewById(R.id.lamps));
	}

	@Override
	protected void onResume() {
		super.onResume();
		restore();
		// List<String> groups =
		// MoodlampRegistry.getInstance().addListener(this);
		// for (String group : groups) {
		// // lampAdded(group);
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();
		store();
		// MoodlampRegistry.getInstance().removeListener(this);
		// groupFlipper.removeAllViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DefaultMenu.addAbout(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return DefaultMenu.onOptionsItemSelected(item, this);
	}

	private synchronized void store() {
		Editor edit = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
		edit.putInt("seekr", mSeekBarR.getProgress());
		edit.putInt("seekg", mSeekBarG.getProgress());
		edit.putInt("seekb", mSeekBarB.getProgress());
		edit.commit();
	}

	private synchronized void restore() {
		SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		mSeekBarR.setProgress(sp.getInt("seekr", 79));
		mSeekBarG.setProgress(sp.getInt("seekg", 19));
		mSeekBarB.setProgress(sp.getInt("seekb", 237));
		fireColor(false);
	}

	private void fireColor(final boolean fromUser) {
		mHandlerAction.post(new Runnable() {
			@Override
			public void run() {
				int r = mSeekBarR.getProgress();
				int g = mSeekBarG.getProgress();
				int b = mSeekBarB.getProgress();

				String addr = "";

				if (mColorBlockView != null) {
					mColorBlockView.setColor(r, g, b);
				}
				if (fromUser && addr != null) {
					mColorBlockView.blinkFrame();
					if (moodlampServiceBound) {
						moodlampService.send(mLamps.getSelected(),
								Color.rgb(r, g, b));
					}
				}
			}
		});
	}

	private class RGBSeekBarChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			fireColor(fromUser);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	@Override
	public void changeColor(int color) {
		mSeekBarR.setProgress(Color.red(color));
		mSeekBarG.setProgress(Color.green(color));
		mSeekBarB.setProgress(Color.blue(color));
		fireColor(true);
	}

	@Override
	public void changePreset(int idx) {
		int[] colors = getColors();

		int r = mSeekBarR.getProgress();
		int g = mSeekBarG.getProgress();
		int b = mSeekBarB.getProgress();

		colors[idx] = Color.rgb(r, g, b);
		mMultiColorBlockView.setColors(colors);
		setColors(colors);
	}

	synchronized private int[] getColors() {
		SharedPreferences sp = getSharedPreferences(PREF_COLORS,
				Context.MODE_PRIVATE);

		String[] strings = sp.getString(PREF_COLORS,
				"-16777216,-16711681,-256,-1").split(",");
		int[] ints = new int[strings.length];
		int i = 0;
		for (String s : strings) {
			ints[i] = Integer.valueOf(s).intValue();
			i++;
		}
		return ints;
	}

	synchronized public void setColors(int[] colors) {
		String s = "";
		for (int i = 0; i < colors.length; i++) {
			s = s + colors[i];
			if (i != colors.length - 1) {
				s = s + ",";
			}
		}

		Editor edit = getSharedPreferences(PREF_COLORS, Context.MODE_PRIVATE)
				.edit();
		edit.putString(PREF_COLORS, s);
		edit.commit();
	}

	@Override
	public void moodlampAdded(final Moodlamp ml) {
		mHandlerAction.post(new Runnable() {
			@Override
			public void run() {
				mLamps.moodlampAdded(ml);
			}
		});

	}

	@Override
	public void moodlampsRemoved() {
		mLamps.moodlampsRemoved();
	}

}