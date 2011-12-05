package de.ccc.muc.zeusplus.misc;

public class Helper {

	public static String tohex(int i) {
		if (i < 16) {
			return "0" + Integer.toHexString(i).toUpperCase();
		}
		return Integer.toHexString(i).toUpperCase();
	}

}
