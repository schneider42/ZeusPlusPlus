package de.ccc.muc.zeusplus.service;

import java.net.Inet6Address;
import java.util.Comparator;

public class Inet6AddressComparator implements Comparator<Inet6Address> {

	private int s1;
	private int s2;

	@Override
	public int compare(Inet6Address ia1, Inet6Address ia2) {
		s1 = score(ia1);
		s2 = score(ia2);

		if (s1 > s2) {
			return -1;
		}
		if (s1 < s2) {
			return 1;
		}

		return ia1.getHostAddress().compareTo(ia2.getHostAddress());
	}

	private int score(Inet6Address ia) {
		if (ia == null) {
			return 0;
		}
		if (ia.isLoopbackAddress()) {
			return 1;
		}
		if (ia.isLinkLocalAddress()) {
			return 2;
		}
		if (ia.isSiteLocalAddress()) {
			return 3;
		}
		return 4;
	}

}
