package de.ccc.muc.zeusplus.service;

import java.io.IOException;
import java.net.Inet6Address;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.ccc.muc.zeusplus.misc.C;
import de.ccc.muc.zeusplus.misc.QLog;

public class MdnsDiscover extends Thread implements ServiceListener {

	private boolean running = true;

	private MoodlampRegistry registry;
	private Map<String, ServiceEvent> events;
	private Map<Inet6Address, JmDNS> mdnss;

	public MdnsDiscover(MoodlampRegistry registry) {
		this.registry = registry;
		events = new HashMap<String, ServiceEvent>();
		mdnss = new HashMap<Inet6Address, JmDNS>();

		start();
	}

	@Override
	public void run() {
		super.run();

		Map<String, ServiceEvent> tmp;
		
		while (running) {
			try {

				synchronized (events) {
					while (events.isEmpty()) {
						events.wait();
					}
					tmp = new HashMap<String, ServiceEvent>(events);
					events.clear();
				}

				QLog.d(this.getClass().getName(), "Found " + tmp.size()
						+ " entries for resolving");

				sleep(C.MDNS_RESOLVE_WAIT);

				for (ServiceEvent event : tmp.values()) {
					sleep(C.MDNS_RESOLVE_WAIT);
					event.getDNS().requestServiceInfo(event.getType(),
							event.getName(), true);
				}

			} catch (InterruptedException e) {
				interrupt();
			}
		}
	}

	public void shutdown() {
		QLog.d(this.getClass().getName(), "Shutdown...");
		running = false;
		interrupt();
	}

	synchronized public void add(Inet6Address address) {
		if (mdnss.containsKey(address)) {
			return;
		}

		JmDNS mdns;
		try {
			mdns = JmDNS.create(address);
		} catch (IOException e) {
			QLog.e(this.getClass().getName(), "Could not add mdns for: "
					+ address, e);
			return;
		}

		mdns.addServiceListener(C.MDNS_SERVICE_NAME, this);
		mdnss.put(address, mdns);
	}

	synchronized public void remove(Inet6Address address) {
		JmDNS mdns = mdnss.remove(address);
		if (mdns == null) {
			return;
		}

		try {
			mdns.close();
		} catch (IOException e) {
			// Shutting down discovery, can be ignored
		}
	}

	synchronized public void removeAll() {
		for (Inet6Address address : new HashSet<Inet6Address>(mdnss.keySet())) {
			remove(address);
		}
	}

	@Override
	public void serviceAdded(ServiceEvent event) {
		QLog.d(getClass().getName(), "Found moodlamp, queuing for resolving: "
				+ event.getName());
		synchronized (events) {
			events.put(event.getName(), event);
			events.notify();
		}
	}

	@Override
	public void serviceRemoved(ServiceEvent event) {
		// the mdns library does not throw removed events
	}

	@Override
	public void serviceResolved(ServiceEvent event) {
		Inet6Address[] addresses = event.getInfo().getInet6Addresses();

		if (addresses.length == 0) {
			QLog.w(getClass().getName(),
					"Moodlamp has no ipv6: " + event.getName());
			return;
		}

		Inet6Address ia = addresses[0]; // Moodlamps advertise only one address
		int port = event.getInfo().getPort();
		QLog.d(getClass().getName(),
				"Resolved moodlamp ipv6: " + event.getName() + " " + ia
						+ " on " + port);

		registry.addLamp(event.getName(), ia, port);
	}

}
