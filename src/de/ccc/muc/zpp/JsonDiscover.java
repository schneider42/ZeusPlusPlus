package de.ccc.muc.zpp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import de.ccc.muc.zeusplus.misc.QLog;
import de.ccc.muc.zeusplus.service.MoodlampRegistry;

public class JsonDiscover extends Thread {

	private boolean running = true;
	String url = null;
	
	private MoodlampRegistry registry;
	Inet6Address iface = null;
	//private Map<String, String> events;


	public JsonDiscover(MoodlampRegistry registry) {
		this.registry = registry;
		//events = new HashMap<String, String>();
	}

	@Override
	public void run() {
		super.run();

		MulticastSocket msocket;
		
		DatagramSocket socket;
		Inet6Address dirmulticastaddress;
		try {
			dirmulticastaddress = (Inet6Address) Inet6Address.getByName("ff18:583:786d:8ec9:d3d6:fd2b:1155:e066");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}
		String discoverMsg = "{\"cmd\": \"discover-directory\"}";
		DatagramPacket discoverPacket = new DatagramPacket(discoverMsg.getBytes(), discoverMsg.length(), 
															dirmulticastaddress, 2323);
		
		//Map<String, ServiceEvent> tmp;
		
		while (running) {
			try {
				if( url == null ){
				try {
					socket = new DatagramSocket();
					try {
						msocket = new MulticastSocket(2323);
						msocket.joinGroup(dirmulticastaddress);
						msocket.setSoTimeout(100);
						socket.send(discoverPacket);
						for(int i=0; i< 5; i++){
							byte[] buffer = new byte[1024];
							DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
							try {
								msocket.receive(packet);
								String json = new String(packet.getData(), 0 , packet.getLength());
								QLog.d(this.getClass().getName(), "received: "+ json);
								JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
								String cmd = object.getString("cmd");
								if( cmd.equals("directory") ){
									url = object.getString("url");
									refreshMoodlamps();
								}
							} catch (java.net.SocketTimeoutException e) {
								QLog.d(this.getClass().getName(), "timeout");
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						msocket.close();
						sleep(5);
					} catch (IOException e) {
						QLog.d(this.getClass().getName(),
								"Failed to send packet", e);
						// we can ignore this, network may be flaky
						continue;
					}
				
				} catch (SocketException e) {
					QLog.d(this.getClass().getName(), "Failed to create socket",
							e);
					// we can ignore this, network may be flaky
				}
				}
			} catch (InterruptedException e) {
				interrupt();
			}
		}
	}
	
	public void refreshMoodlamps()
	{
		//QLog.d(this.getClass().getName(), "Getting moodlamp list from "+url);
		JSONObject json = RestJsonClient.connect(url);
		//QLog.d(this.getClass().getName(), "got "+ json.toString());
		try {
			JSONArray services = json.getJSONArray("services");
			for(int i=0; i<services.length(); i++){
				try{
					JSONObject service = services.getJSONObject(i);
					if( service.getString("service-type").equals("moodlamp") &&
						service.getBoolean("udp") ){
						String name = service.getString("name");
						String ip = service.getString("url");
						int port = service.getInt("port");
						//QLog.d(this.getClass().getName(), "got "+ ip);
						
						Inet6Address ia;
						try {
							ia = (Inet6Address) Inet6Address.getByName(ip);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
							continue;
						}
						
						//QLog.d(getClass().getName(),
						//		"Resolved moodlamp ipv6: " + name + " " + ia
						//				+ " on " + port);

						registry.addLamp(name, ia, port);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	public void shutdown() {
		QLog.d(this.getClass().getName(), "Shutdown...");
		running = false;
		interrupt();
	}

	synchronized public void add(Inet6Address address) {
		QLog.d(this.getClass().getName(), "adding address "+address);
		iface = address;
		start();
	}

	synchronized public void remove(Inet6Address address) {
	}

	synchronized public void removeAll() {
		shutdown();
	}

}
