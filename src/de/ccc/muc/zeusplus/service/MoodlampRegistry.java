package de.ccc.muc.zeusplus.service;

import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.ccc.muc.zeusplus.misc.C;
import de.ccc.muc.zeusplus.misc.QLog;

public class MoodlampRegistry {

	private List<MoodlampRegistryListener> listeners;
	private Map<String, Moodlamp> mls;

	public MoodlampRegistry() {
		listeners = new LinkedList<MoodlampRegistryListener>();
		mls = new HashMap<String, Moodlamp>();
	}

	synchronized public void addLamp(String id, Inet6Address address, int port) {
		Moodlamp ml = new Moodlamp(id, address, port);

		if (filter(ml)) {
			QLog.d(this.getClass().getName(), "Ignoring lamp: " + ml + " ("
					+ mls.size() + ")");
			return;
		}

		if (mls.put(id, ml) == null) {
			QLog.d(this.getClass().getName(),
					"Adding lamp: " + ml + " (" + mls.size() + ")");
			for (MoodlampRegistryListener l : listeners) {
				l.moodlampAdded(ml);
			}
		} else {
			QLog.d(this.getClass().getName(), "Updating lamp: " + ml + " ("
					+ mls.size() + ")");
		}

	}
	
	synchronized public void clearLamps() {
		QLog.d(this.getClass().getName(), "Clearing lamps");
		mls.clear();
		for (MoodlampRegistryListener l : listeners) {
			l.moodlampsRemoved();
		}
	}

	synchronized public Moodlamp getLamp(String id) {
		return mls.get(id);
	}

	synchronized public void clearListeners() {
		listeners.clear();
	}

	synchronized public List<Moodlamp> addListener(MoodlampRegistryListener l) {
		listeners.add(l);
		return new ArrayList<Moodlamp>(mls.values());
	}

	synchronized public void removeListener(MoodlampRegistryListener l) {
		listeners.remove(l);
	}

	@SuppressWarnings("unused")
	// if static constant is disabled may be unused
	private boolean filter(Moodlamp ml) {
		return C.ML_MULTICAST_FILTER && !ml.getAddress().isMulticastAddress();
	}

}
