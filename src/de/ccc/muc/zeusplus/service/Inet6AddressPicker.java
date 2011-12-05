package de.ccc.muc.zeusplus.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ccc.muc.zeusplus.misc.QLog;

public class Inet6AddressPicker {

	// example: "00000000000000000000000000000001 01 80 10 80       lo"
	private static final String PATTERN_IFINET6 = "([0-9a-f]{32})\\ ([0-9a-f]{2})\\ ([0-9a-f]{2})\\ ([0-9a-f]{2})\\ ([0-9a-f]{2})\\ +(.*)";
	private static final File FILE_IFINET6 = new File("/proc/net/if_inet6");

	private Inet6AddressComparator comparator;
	private Pattern patternIfInet6;
	private Matcher matcher;

	public Inet6AddressPicker() {
		comparator = new Inet6AddressComparator();
		patternIfInet6 = Pattern.compile(PATTERN_IFINET6);
	}

	public Set<Inet6Address> pickAddresses() {
		Map<String, List<Inet6Address>> mapping = getDevicesWithInet6Addresses();

		Set<Inet6Address> addresses = new HashSet<Inet6Address>();
		for (String dev : mapping.keySet()) {
			List<Inet6Address> possible = mapping.get(dev);
			if (possible == null || possible.size() == 0) {
				continue;
			}

			Collections.sort(possible, comparator);
			for (Inet6Address ia : possible) {
				QLog.d(this.getClass().getName(), "Found " + dev + ": " + ia);
			}

			addresses.add(possible.get(0));
		}

		for (Inet6Address ia : addresses) {
			QLog.d(this.getClass().getName(), "Used: " + ia);
		}

		return addresses;
	}

	private Map<String, List<Inet6Address>> getDevicesWithInet6Addresses() {
		Map<String, List<Inet6Address>> mapping = new HashMap<String, List<Inet6Address>>();

		FileReader reader;
		try {
			reader = new FileReader(FILE_IFINET6);
		} catch (FileNotFoundException e) {
			QLog.e(this.getClass().getName(), "Could not read " + FILE_IFINET6,
					e);
			return mapping;
		}

		BufferedReader br = new BufferedReader(reader, 1024);

		String line;
		Inet6Address address;
		List<Inet6Address> addresses;

		String addressRaw;
		String device;

		try {
			while ((line = br.readLine()) != null) {

				matcher = patternIfInet6.matcher(line);
				if (!matcher.matches()) {
					QLog.e(this.getClass().getName(),
							"Invalid if_inet6 description: " + line);
					continue;
				}

				addressRaw = matcher.group(1);
				device = matcher.group(6);

				QLog.d(this.getClass().getName(), "Read " + device + ": "
						+ addressRaw);

				address = parse(addressRaw);
				if (address == null) {
					continue;
				}
				if (address.isLoopbackAddress()) {
					continue;
				}

				addresses = mapping.get(device);
				if (addresses == null) {
					addresses = new ArrayList<Inet6Address>();
					mapping.put(device, addresses);
				}

				addresses.add(address);
			}
		} catch (IOException e) {
			QLog.e(this.getClass().getName(),
					"Failed to getInterfacesWithInet6Addresses!", e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// we can ignore this
			}
		}

		return mapping;
	}

	private Inet6Address parse(String addr) {
		// From: 00000000000000000000000000000001
		// To: 0000:0000:0000:0000:0000:0000:0000:0001
		addr = addr.replaceAll(".{4}", ":$0").replaceFirst(":", "");

		InetAddress ia;
		try {
			ia = Inet6Address.getByName(addr);
		} catch (UnknownHostException e) {
			QLog.e(this.getClass().getName(), "Lookup failed: " + addr);
			return null;
		}
		if (!(ia instanceof Inet6Address)) {
			QLog.e(this.getClass().getName(), "Address is not ipv6: " + addr);
			return null;
		}

		return (Inet6Address) ia;
	}
}
