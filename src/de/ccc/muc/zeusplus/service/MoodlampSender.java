package de.ccc.muc.zeusplus.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;
import de.ccc.muc.zeusplus.misc.C;
import de.ccc.muc.zeusplus.misc.QLog;

public class MoodlampSender extends Thread {

	private boolean running = true;

	private Map<Moodlamp, Integer> actions;

	public MoodlampSender() {
		actions = new HashMap<Moodlamp, Integer>();

		start();
	}

	public void enqueue(List<Moodlamp> mls, int color) {
		synchronized (actions) {
			for (Moodlamp ml : mls) {
				actions.put(ml, color);
			}

			actions.notify();
		}
	}

	@Override
	public void run() {
		super.run();

		Map<Moodlamp, Integer> tmp;

		DatagramSocket socket;
		DatagramPacket packet;
		byte[] buffer;
		Inet6Address ia;
		int port;

		while (running) {
			try {

				synchronized (actions) {
					while (actions.isEmpty()) {
						actions.wait();
					}
					tmp = new HashMap<Moodlamp, Integer>(actions);
					actions.clear();
				}

				QLog.d(this.getClass().getName(), "Found " + tmp.size()
						+ " entries for sending");

				try {
					socket = new DatagramSocket();

					for (Moodlamp ml : tmp.keySet()) {
						int color = tmp.get(ml);

						ia = ml.getAddress();
						if (ia == null) {
							continue;
						}

						port = ml.getPort();

						if (C.ML_FADE) {
							buffer = toCommandFade(color);
						} else {
							buffer = toCommandColor(color);
						}

						packet = new DatagramPacket(buffer, buffer.length, ia,
								port);

						try {
							socket.send(packet);
							sleep(C.SEND_WAIT);
						} catch (IOException e) {
							QLog.d(this.getClass().getName(),
									"Failed to send packet", e);
							// we can ignore this, network may be flaky
							continue;
						}
					}
				} catch (SocketException e) {
					QLog.d(this.getClass().getName(), "Failed to send packet",
							e);
					// we can ignore this, network may be flaky
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

	private static byte[] toCommandColor(int color) {
		byte[] cmd = new byte[4];
		cmd[0] = 'C';
		cmd[1] = (byte) Color.red(color);
		cmd[2] = (byte) Color.green(color);
		cmd[3] = (byte) Color.blue(color);
		return cmd;
	}

	private static byte[] toCommandFade(int color) {
		byte[] cmd = new byte[6];
		cmd[0] = 'T';
		cmd[1] = (byte) Color.red(color);
		cmd[2] = (byte) Color.green(color);
		cmd[3] = (byte) Color.blue(color);
		cmd[4] = 0;
		cmd[5] = (byte) 200;

		return cmd;
	}

}
