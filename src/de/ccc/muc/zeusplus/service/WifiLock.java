package de.ccc.muc.zeusplus.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import de.ccc.muc.zeusplus.misc.QLog;

public class WifiLock {

	private Context ctx;
	private MulticastLock wifiMulticastLock;

	public WifiLock(Context ctx) {
		this.ctx = ctx;
	}

	public void acquire() {
		release();

		if (wifiMulticastLock == null) {
			wifiMulticastLock = ((WifiManager) ctx
					.getSystemService(Context.WIFI_SERVICE))
					.createMulticastLock("MCL");
		}

		if (wifiMulticastLock == null) {
			QLog.e(getClass().getName(), "Could not obtain MulticastLock!");
			return;
		}

		wifiMulticastLock.acquire();
		if (isHeld()) {
			QLog.i(getClass().getName(), "Acquired MulticastLock!");
		} else {
			QLog.w(getClass().getName(), "Failed to acquire MulticastLock!");
		}
	}

	public void release() {
		if (isHeld()) {
			QLog.i(getClass().getName(), "Releasing MulticastLock!");
			wifiMulticastLock.release();
		}
	}

	private boolean isHeld() {
		return wifiMulticastLock != null && wifiMulticastLock.isHeld();
	}
}
