package de.ccc.muc.zeusplus.service;

import java.net.Inet6Address;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import de.ccc.muc.zeusplus.misc.QLog;
import de.ccc.muc.zpp.JsonDiscover;

public class MoodlampService extends Service {

	private IBinder binder = new MoodlampServiceBinder();

	private WifiLock wifiLock;
	private Inet6AddressPicker inet6AddressPicker;
	private JsonDiscover discover;
	private MoodlampRegistry registry;
	private MoodlampSender moodlampSender;

	public class MoodlampServiceBinder extends Binder {
		public MoodlampService getService() {
			return MoodlampService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		QLog.i(this.getClass().getName(), "Starting...");
		wifiLock = new WifiLock(getBaseContext());
		inet6AddressPicker = new Inet6AddressPicker();
		registry = new MoodlampRegistry();
		discover = new JsonDiscover(registry);
		moodlampSender = new MoodlampSender();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdown();
	}

	synchronized public void discoverStart() {
		registry.clearLamps();
		wifiLock.acquire();
		Set<Inet6Address> addresses = inet6AddressPicker.pickAddresses();
		for (Inet6Address ia : addresses) {
			discover.add(ia);
		}
	}

	synchronized public void discoverStop() {
		discover.removeAll();
		wifiLock.release();
	}

	public void send(List<Moodlamp> mls, int color) {
		moodlampSender.enqueue(mls, color);
	}

	public MoodlampRegistry getRegistry() {
		return registry;
	}

	private void shutdown() {
		QLog.i(this.getClass().getName(), "Shutdown...");
		registry.clearLamps();
		moodlampSender.shutdown();
		discover.shutdown();
		wifiLock.release();
	}

}
