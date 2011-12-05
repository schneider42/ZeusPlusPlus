package de.ccc.muc.zeusplus.service;

import java.net.Inet6Address;

public class Moodlamp implements Comparable<Moodlamp> {

	private String id;
	private Inet6Address address;
	private int port;

	public Moodlamp(String id, Inet6Address address, int port) {
		this.id = id;
		this.address = address;
		this.port = port;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Inet6Address getAddress() {
		return address;
	}

	public void setAddress(Inet6Address address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Moodlamp)) {
			return false;
		}
		return id.equals(((Moodlamp) o).getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "ML " + id + " on " + address;
	}

	@Override
	public int compareTo(Moodlamp other) {
		return compareScore(this).compareTo(compareScore(other));
	}

	private String compareScore(Moodlamp ml) {
		return (ml.getAddress().isMulticastAddress() ? "0" : "1") + ml.getId();
	}
}
