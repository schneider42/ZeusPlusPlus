package de.ccc.muc.zeusplus.misc;

import android.util.Log;

public class QLog {

	private static int LEVEL = C.LOGLEVEL;

	public static void v(String tag, String msg) {
		v(tag, msg, null);
	}

	public static void v(String tag, String msg, Throwable t) {
		if (Log.VERBOSE >= LEVEL) {
			Log.v(tag, msg, t);
		}
	}

	public static void d(String tag, String msg) {
		d(tag, msg, null);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (Log.DEBUG >= LEVEL) {
			Log.d(tag, msg, t);
		}
	}

	public static void i(String tag, String msg) {
		i(tag, msg, null);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (Log.INFO >= LEVEL) {
			Log.i(tag, msg, t);
		}
	}

	public static void w(String tag, String msg) {
		w(tag, msg, null);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (Log.WARN >= LEVEL) {
			Log.w(tag, msg, t);
		}
	}

	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (Log.ERROR >= LEVEL) {
			Log.e(tag, msg, t);
		}
	}

	public static void a(String tag, String msg) {
		a(tag, msg, null);
	}

	public static void a(String tag, String msg, Throwable t) {
		if (Log.ASSERT >= LEVEL) {
			Log.wtf(tag, msg, t);
		}
	}

}
