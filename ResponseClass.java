package diff_match_patch;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class ResponseClass extends Thread {

	Zone requestedPointInZone;
	int belongedToIndex = 0;

	Socket serverSocket;
	ArrayList<Zone> belongedZones;
	ArrayList<NeighbourPeer> neighbourPeers;
	PeerClient peerClient;

	public ResponseClass(ArrayList<Zone> belongedZones,
			ArrayList<NeighbourPeer> neighbourPeers, Socket serverSocket,
			PeerClient peerClient ) {
		this.belongedZones = belongedZones;
		this.neighbourPeers = neighbourPeers;
		this.serverSocket = serverSocket;
		this.peerClient = peerClient;
	}

	String getHashFunction(String fileName) {
		String pointString = "";

		char stringArray[] = fileName.toCharArray();

		int x = 0, y = 0;
		for (int i = 0; i < stringArray.length; i = i + 2) {
			x = x + (int) stringArray[i];
		}
		for (int i = 1; i < stringArray.length; i = i + 2) {
			y = y + (int) stringArray[i];
		}

		x = x % 800;
		y = y % 800;

		pointString = x + " " + y;

		return pointString;
	}

	boolean getIP(String g) {
		String ip = "";
		boolean b = false;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (!addr.getHostAddress().contains(":")) {
						ip = addr.getHostAddress();
					}
				}
			}
			if (ip.equals(g)) {
				return true;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return b;

	}

	String getIPAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (!addr.getHostAddress().contains(":")) {
						ip = addr.getHostAddress();
					}
				}
			}
			return ip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	boolean isMyZone(String zone) {
		String zoneVal[] = zone.split(" ");
		int x = Integer.parseInt(zoneVal[0]);
		int y = Integer.parseInt(zoneVal[1]);
		boolean boolVal = false;
		for (int i = 0; i < belongedZones.size(); i++) {
			if ((belongedZones.get(i).getTop().getx() <= x)
					&& (belongedZones.get(i).getTop().gety() <= y)
					&& (belongedZones.get(i).getBottom().getx() >= x)
					&& (belongedZones.get(i).getBottom().gety() >= y)) {
				boolVal = true;
				requestedPointInZone = belongedZones.get(i);
				belongedToIndex = i;
			}
		}
		return boolVal;
	}

	boolean isNeighbourZone(String zone, Zone zoneObj) {
		String zoneVal[] = zone.split(" ");
		int x = Integer.parseInt(zoneVal[0]);
		int y = Integer.parseInt(zoneVal[1]);
		boolean boolVal = false;
		if ((zoneObj.getTop().getx() <= x) && (zoneObj.getTop().gety() <= y)
				&& (zoneObj.getBottom().getx() >= x)
				&& (zoneObj.getBottom().gety() >= y)) {
			boolVal = true;
		}

		return boolVal;
	}

	boolean checkNeighbour(Zone myZone, Zone neighbourZone) {
		int x1 = neighbourZone.getTop().getx();
		int y1 = neighbourZone.getTop().gety();
		int x2 = neighbourZone.getBottom().getx();
		int y2 = neighbourZone.getBottom().gety();
		int bx1 = myZone.getTop().getx();
		int by1 = myZone.getTop().gety();
		int bx2 = myZone.getBottom().getx();
		int by2 = myZone.getBottom().gety();
		if ((((x1 + 1) < bx2 && (x1 + 1) > bx1) && ((y1 == by1) || (y1 == by2)
				|| (y2 == by1) || (y2 == by2)))
				|| (((x2 - 1) < bx2 && (x2 - 1) > bx1) && ((y1 == by1)
						|| (y1 == by2) || (y2 == by1) || (y2 == by2)))
				|| (((y1 + 1) < by2 && (y1 + 1) > by1) && ((x1 == bx1)
						|| (x1 == bx2) || (x2 == bx1) || (x2 == bx2)))
				|| (((y2 - 1) < by2 && (y2 - 1) > by1) && ((x1 == bx1)
						|| (x1 == bx2) || (x2 == bx1) || (x2 == bx2)))) {
			return true;
		} else if ((((bx1 + 1) < x2 && (bx1 + 1) > x1) && ((by1 == y1)
				|| (by1 == y2) || (by2 == y1) || (by2 == y2)))
				|| (((bx2 - 1) < x2 && (bx2 - 1) > x1) && ((by1 == y1)
						|| (by1 == y2) || (by2 == y1) || (by2 == y2)))
				|| (((by1 + 1) < y2 && (by1 + 1) > y1) && ((bx1 == x1)
						|| (bx1 == x2) || (bx2 == x1) || (bx2 == x2)))
				|| (((by2 - 1) < y2 && (by2 - 1) > y1) && ((bx1 == x1)
						|| (bx1 == x2) || (bx2 == x1) || (bx2 == x2)))) {
			return true;
		} else {
			return false;
		}
	}

	NeighbourPeer checkTraverse(int belongedZoneNumber, int x, int y,
			String clientIP) {
		NeighbourPeer nPeer = null;
		int minDistance = 0, count = 0;
		boolean isInMyNeighbour = false;

		for (int j = 0; j < neighbourPeers.size(); j++) {
			isInMyNeighbour = isNeighbourZone(x + " " + y, neighbourPeers
					.get(j).getZone());
			if (isInMyNeighbour == true) {
				nPeer = neighbourPeers.get(j);
				break;
			}
		}
		for (int i = 0; i < neighbourPeers.size(); i++) {

			if (!clientIP.equals(neighbourPeers.get(i).getIPAddress())
					&& !getIP(clientIP)) {

				if (isInMyNeighbour == false) {
					int x1 = neighbourPeers.get(i).getNeighbourTo().getTop()
							.getx();
					int y1 = neighbourPeers.get(i).getNeighbourTo().getTop()
							.gety();
					int x2 = neighbourPeers.get(i).getNeighbourTo().getBottom()
							.getx();
					int y2 = neighbourPeers.get(i).getNeighbourTo().getBottom()
							.gety();

					int midX = (x1 + x2) / 2;
					int midY = (y1 + y2) / 2;

					int distance = ((midX - x) * (midX - x))
							+ ((midY - y) * (midY - y));

					if (minDistance == 0) {
						minDistance = distance;
						count = i;
						nPeer = neighbourPeers.get(i);
					} else {
						if (distance < minDistance) {
							minDistance = distance;
							count = i;
							nPeer = neighbourPeers.get(i);
						}
					}

				}
			}

		}
		NeighbourPeer temp = neighbourPeers.get(count);
		neighbourPeers
				.set(count, neighbourPeers.get(neighbourPeers.size() - 1));
		neighbourPeers.set(neighbourPeers.size() - 1, temp);
		return nPeer;
	}

	public synchronized void run() {
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(serverSocket.getInputStream()));

			String clientIP = serverSocket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());
			String clientMessage = bufferedReader.readLine();
			String message[] = clientMessage.split("-");

			if (!getIP(message[0])) {

				if (message.length == 2) {

					if (isMyZone(message[1])) {

						String zoneBeforeUpDate = requestedPointInZone.getTop()
								.getx()
								+ " "
								+ requestedPointInZone.getTop().gety()
								+ " "
								+ requestedPointInZone.getBottom().getx()
								+ " "
								+ requestedPointInZone.getBottom().gety();
						String zone;
						Socket socket = new Socket(message[0], 9092);
						String zoneVal[] = message[1].split(" ");
						int x = Integer.parseInt(zoneVal[0]);
						int y = Integer.parseInt(zoneVal[1]);

						DataOutputStream d = new DataOutputStream(
								socket.getOutputStream());

						if (belongedToIndex == 0) {
							if (requestedPointInZone.getBottom().getx()
									- requestedPointInZone.getTop().getx() >= requestedPointInZone
									.getBottom().gety()
									- requestedPointInZone.getTop().gety()) // Square
							{
								int temp = (requestedPointInZone.getBottom()
										.getx() - requestedPointInZone.getTop()
										.getx()) / 2;
								if (temp > x) // less then half
								{
									zone = requestedPointInZone.getTop().getx()
											+ " "
											+ requestedPointInZone.getTop()
													.gety()
											+ " "
											+ (requestedPointInZone.getTop()
													.getx() + temp)
											+ " "
											+ requestedPointInZone.getBottom()
													.gety();
									requestedPointInZone.setZone(
											requestedPointInZone.getTop()
													.getx() + temp,
											requestedPointInZone.getTop()
													.gety(),
											requestedPointInZone.getBottom()
													.getx(),
											requestedPointInZone.getBottom()
													.gety());
								} else {
									zone = (requestedPointInZone.getTop()
											.getx() + temp)
											+ " "
											+ requestedPointInZone.getTop()
													.gety()
											+ " "
											+ requestedPointInZone.getBottom()
													.getx()
											+ " "
											+ requestedPointInZone.getBottom()
													.gety();
									requestedPointInZone.setZone(
											requestedPointInZone.getTop()
													.getx(),
											requestedPointInZone.getTop()
													.gety(),
											(requestedPointInZone.getTop()
													.getx() + temp),
											requestedPointInZone.getBottom()
													.gety());
								}
							} else {
								int temp = (requestedPointInZone.getBottom()
										.gety() - requestedPointInZone.getTop()
										.gety()) / 2;
								if (temp > y) {
									zone = requestedPointInZone.getTop().getx()
											+ " "
											+ requestedPointInZone.getTop()
													.gety()
											+ " "
											+ requestedPointInZone.getBottom()
													.getx()
											+ " "
											+ (requestedPointInZone.getTop()
													.gety() + temp);
									requestedPointInZone.setZone(
											requestedPointInZone.getTop()
													.getx(),
											(requestedPointInZone.getTop()
													.gety() + temp),
											requestedPointInZone.getBottom()
													.getx(),
											requestedPointInZone.getBottom()
													.gety());
								} else {
									zone = requestedPointInZone.getTop().getx()
											+ " "
											+ (requestedPointInZone.getTop()
													.gety() + temp)
											+ " "
											+ requestedPointInZone.getBottom()
													.getx()
											+ " "
											+ requestedPointInZone.getBottom()
													.gety();
									requestedPointInZone.setZone(
											requestedPointInZone.getTop()
													.getx(),
											requestedPointInZone.getTop()
													.gety(),
											requestedPointInZone.getBottom()
													.getx(),
											requestedPointInZone.getTop()
													.gety() + temp);
								}
							}
							String temp[] = zone.split(" ");

							Zone nZone = new Zone(Integer.parseInt(temp[0]),
									Integer.parseInt(temp[1]),
									Integer.parseInt(temp[2]),
									Integer.parseInt(temp[3]));
							NeighbourPeer neighbourPeer = new NeighbourPeer(
									nZone, requestedPointInZone, message[0]);

							d.writeBytes(zone + "\n");

							// send neighbour
							for (int i = 0; i < neighbourPeers.size(); i++) {
								if (neighbourPeers.get(i).getNeighbourTo() == requestedPointInZone) {
									d.writeBytes("801" + "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getTop().getx())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getTop().gety())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getBottom()
													.getx())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getBottom()
													.gety())
											+ "\n");
									d.writeBytes(neighbourPeers.get(i)
											.getIPAddress() + "\n");
								}
							}
							d.writeBytes("802" + "\n");

							// add into neighbour look up;

							d.writeBytes(Integer.toString(requestedPointInZone
									.getTop().getx()) + "\n"); // send my zone;
							d.writeBytes(Integer.toString(requestedPointInZone
									.getTop().gety()) + "\n");
							d.writeBytes(Integer.toString(requestedPointInZone
									.getBottom().getx()) + "\n");
							d.writeBytes(Integer.toString(requestedPointInZone
									.getBottom().gety()) + "\n");

							neighbourPeers.add(neighbourPeer);
							// update neighbour list to remove few
							int size = neighbourPeers.size();
							for (int i = size - 1; i >= 0; i--) {
								if (checkNeighbour(requestedPointInZone,
										neighbourPeers.get(i).getZone())) {

								} else {
									neighbourPeers.remove(i);
								}
							}

							// send zone update
							for (int i = 0; i < neighbourPeers.size(); i++) {
								Socket sendZoneUpdate = new Socket(
										neighbourPeers.get(i).getIPAddress(),
										9093);
								DataOutputStream dout = new DataOutputStream(
										sendZoneUpdate.getOutputStream());
								dout.writeBytes(requestedPointInZone.getTop()
										.getx()
										+ " "
										+ requestedPointInZone.getTop().gety()
										+ " "
										+ requestedPointInZone.getBottom()
												.getx()
										+ " "
										+ requestedPointInZone.getBottom()
												.gety()
										+ "-"
										+ " "
										+ "-"
										+ zoneBeforeUpDate + "\n");
								sendZoneUpdate.close();
							}
						} else {

							zone = requestedPointInZone.getTop().getx() + " "
									+ requestedPointInZone.getTop().gety()
									+ " "
									+ requestedPointInZone.getBottom().getx()
									+ " "
									+ requestedPointInZone.getBottom().gety();

							d.writeBytes(zone + "\n");

							for (int i = 0; i < neighbourPeers.size(); i++) {

								if (neighbourPeers.get(i).getNeighbourTo() == belongedZones
										.get(belongedToIndex)) {
									d.writeBytes("801" + "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getTop().getx())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getTop().gety())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getBottom()
													.getx())
											+ "\n");
									d.writeBytes(Integer
											.toString(neighbourPeers.get(i)
													.getZone().getBottom()
													.gety())
											+ "\n");
									d.writeBytes(neighbourPeers.get(i)
											.getIPAddress() + "\n");
								}
							}

							for (int i = 0; i < belongedZones.size(); i++) {
								if (i != belongedToIndex) {
									d.writeBytes("801" + "\n");
									d.writeBytes(Integer.toString(belongedZones
											.get(i).getTop().getx())
											+ "\n");
									d.writeBytes(Integer.toString(belongedZones
											.get(i).getTop().gety())
											+ "\n");
									d.writeBytes(Integer.toString(belongedZones
											.get(i).getBottom().getx())
											+ "\n");
									d.writeBytes(Integer.toString(belongedZones
											.get(i).getBottom().gety())
											+ "\n");
									d.writeBytes(getIPAddress() + "\n");
								}
							}

							d.writeBytes("802" + "\n");

							NeighbourPeer nPeer = new NeighbourPeer(
									belongedZones.get(belongedToIndex),
									belongedZones.get(belongedToIndex),
									message[0]);

							neighbourPeers.add(nPeer);

							d.writeBytes(802 + "\n"); // send my zone;
							d.writeBytes(802 + "\n");
							d.writeBytes(802 + "\n");
							d.writeBytes(802 + "\n");

							// update neighbour list to remove few
							int size = neighbourPeers.size();
							for (int i = size - 1; i >= 0; i--) {
								if (neighbourPeers.get(i).getNeighbourTo() == belongedZones
										.get(belongedToIndex)) {
									Socket removeMyTempZone = new Socket(
											neighbourPeers.get(i)
													.getIPAddress(), 9093);
									DataOutputStream dout = new DataOutputStream(
											removeMyTempZone.getOutputStream());
									String belongTo = neighbourPeers.get(i)
											.getNeighbourTo().getTop().getx()
											+ " "
											+ neighbourPeers.get(i)
													.getNeighbourTo().getTop()
													.gety()
											+ " "
											+ neighbourPeers.get(i)
													.getNeighbourTo()
													.getBottom().getx()
											+ " "
											+ neighbourPeers.get(i)
													.getNeighbourTo()
													.getBottom().gety();
									dout.writeBytes(belongTo + "- - - " + "\n");
									removeMyTempZone.close();
								}
							}

							for (int i = size - 1; i >= 0; i--) {
								if (neighbourPeers.get(i).getNeighbourTo() == belongedZones
										.get(belongedToIndex)) {
									neighbourPeers.remove(i);
								}
							}

							belongedZones.remove(belongedToIndex);
						}

						socket.close();

						
					} else {
						String zoneVal[] = message[1].split(" ");
						int x = Integer.parseInt(zoneVal[0]);
						int y = Integer.parseInt(zoneVal[1]);

						int bx1 = 0, bx2 = 0, by1 = 0, by2 = 0;
						int belongedZoneNumber = 0;

						for (int i = 0; i < neighbourPeers.size(); i++) {
							if (clientIP.equals(neighbourPeers.get(i)
									.getIPAddress())) {
								bx1 = neighbourPeers.get(i).getNeighbourTo()
										.getTop().getx();
								bx2 = neighbourPeers.get(i).getNeighbourTo()
										.getBottom().getx();
								by1 = neighbourPeers.get(i).getNeighbourTo()
										.getTop().gety();
								by2 = neighbourPeers.get(i).getNeighbourTo()
										.getBottom().gety();
							}
						}

						for (int i = 0; i < belongedZones.size(); i++) {
							if (belongedZones.get(i).getTop().getx() == bx1
									&& belongedZones.get(i).getTop().gety() == by1
									&& belongedZones.get(i).getBottom().getx() == bx2
									&& belongedZones.get(i).getBottom().gety() == by2) {
								belongedZoneNumber = i;
							}
						}

						NeighbourPeer nPeer = checkTraverse(belongedZoneNumber,
								x, y, clientIP);
						Socket socket = new Socket(nPeer.getIPAddress(), 9092);
						DataOutputStream d = new DataOutputStream(
								socket.getOutputStream());
						d.writeBytes(clientMessage + "\n");

						socket.close();

					}

				}
				if (message.length == 1) {

					String temp[] = message[0].split(" ");
					Zone zone = new Zone(Integer.parseInt(temp[0]),
							Integer.parseInt(temp[1]),
							Integer.parseInt(temp[2]),
							Integer.parseInt(temp[3]));
					belongedZones.add(zone);

					String v = bufferedReader.readLine();

					while (!v.equals("802")) {

						int x1 = Integer.parseInt(bufferedReader.readLine());// bufferedReader.readLine()
																				// );
						int y1 = Integer.parseInt(bufferedReader.readLine());
						int x2 = Integer.parseInt(bufferedReader.readLine());
						int y2 = Integer.parseInt(bufferedReader.readLine());
						String ip = bufferedReader.readLine();

						Zone z = new Zone(x1, y1, x2, y2);

						for (int i = 0; i < belongedZones.size(); i++) {

							if (checkNeighbour(belongedZones.get(i), z)) {
								NeighbourPeer nPeer = new NeighbourPeer(z,
										belongedZones.get(i), ip);

								boolean flag = false;
								for (int j = 0; j < neighbourPeers.size(); j++) {
									if (nPeer.getIPAddress().equals(
											neighbourPeers.get(j)
													.getIPAddress())) {
										flag = true;
									}
								}
								if (flag == false) {

									if (neighbourPeers.size() == 0
											&& !getIP(nPeer.getIPAddress())) {
										neighbourPeers.add(nPeer);
									} else if (!getIP(nPeer.getIPAddress())) {
										neighbourPeers.add(nPeer);
									}

								}

							}

						}

						v = bufferedReader.readLine();

					}

					Point czTopPoint = new Point(
							Integer.parseInt(bufferedReader.readLine()),
							Integer.parseInt(bufferedReader.readLine()));
					Point czBottomPoint = new Point(
							Integer.parseInt(bufferedReader.readLine()),
							Integer.parseInt(bufferedReader.readLine()));
					Zone clientZone = new Zone(czTopPoint.getx(),
							czTopPoint.gety(), czBottomPoint.getx(),
							czBottomPoint.gety());
					NeighbourPeer nPeer = new NeighbourPeer(clientZone, zone,
							clientIP);
					if (nPeer.getZone().getTop().getx() != 802) {
						neighbourPeers.add(nPeer);
					}

					peerClient.setIsZoneHolderAddressing(true);

					serverSocket.close();
				}
				
				if ( message.length == 3 )
				{
					if (message[1].equals("reply")) 
					{

						if( message[2].equals("0") )
						{
							System.out.println("I do not have");
						}
						else
						{
							System.out.println("I have");
						}
						
					}
					
				}

				if (message.length == 4) {
					if (message[2].equals("1")) {
						ServerClass.setIsAccessPeer(true);
					}
					int index = 0;
					for (int i = 0; i < belongedZones.size(); i++) {
						if ((belongedZones.get(i).getTop().getx() + " "
								+ belongedZones.get(i).getTop().gety() + " "
								+ belongedZones.get(i).getBottom().getx() + " " + belongedZones
								.get(i).getBottom().gety()).equals(message[3])) {
							index = i;
						}
					}

					String stringZoneUpdate = "";
					String isTempZone = "";

					if (message[1].equals("1")) {
						String clientZone[] = message[0].split(" ");
						Zone zone = new Zone(Integer.parseInt(clientZone[0]),
								Integer.parseInt(clientZone[1]),
								Integer.parseInt(clientZone[2]),
								Integer.parseInt(clientZone[3]));
						belongedZones.add(zone);
						index = belongedZones.size() - 1;
						isTempZone = "0";
					} else {

						String clientZone[] = message[0].split(" ");
						ArrayList<Integer> xValue = new ArrayList<Integer>();
						xValue.add(Integer.parseInt(clientZone[0]));
						xValue.add(Integer.parseInt(clientZone[2]));
						xValue.add(belongedZones.get(index).getTop().getx());
						xValue.add(belongedZones.get(index).getBottom().getx());
						Collections.sort(xValue);
						ArrayList<Integer> yValue = new ArrayList<Integer>();
						yValue.add(Integer.parseInt(clientZone[1]));
						yValue.add(Integer.parseInt(clientZone[3]));
						yValue.add(belongedZones.get(index).getTop().gety());
						yValue.add(belongedZones.get(index).getBottom().gety());
						Collections.sort(yValue);

						stringZoneUpdate = belongedZones.get(index).getTop()
								.getx()
								+ " "
								+ belongedZones.get(index).getTop().gety()
								+ " "
								+ belongedZones.get(index).getBottom().getx()
								+ " "
								+ belongedZones.get(index).getBottom().gety();
						belongedZones.get(index).setZone(xValue.get(0),
								yValue.get(0), xValue.get(xValue.size() - 1),
								yValue.get(yValue.size() - 1));
						int size = neighbourPeers.size();
						for (int y = 0; y < size; y++) {
							if (clientIP.equals(neighbourPeers.get(y)
									.getIPAddress())
									&& belongedZones.get(index) == neighbourPeers
											.get(y).getNeighbourTo()) {
								neighbourPeers.remove(y);
								break;
							}
						}

						isTempZone = "1";

						for (int i = 0; i < neighbourPeers.size(); i++) {
							Socket sendZoneUpdate = new Socket(neighbourPeers
									.get(i).getIPAddress(), 9093);
							DataOutputStream dout = new DataOutputStream(
									sendZoneUpdate.getOutputStream());
							dout.writeBytes(belongedZones.get(index).getTop()
									.getx()
									+ " "
									+ belongedZones.get(index).getTop().gety()
									+ " "
									+ belongedZones.get(index).getBottom()
											.getx()
									+ " "
									+ belongedZones.get(index).getBottom()
											.gety()
									+ "-"
									+ " "
									+ "-"
									+ stringZoneUpdate + "\n");
							sendZoneUpdate.close();
						}
					}

					String v = bufferedReader.readLine();

					while (!v.equals("802")) {

						int x1 = Integer.parseInt(bufferedReader.readLine());// bufferedReader.readLine()
																				// );
						int y1 = Integer.parseInt(bufferedReader.readLine());
						int x2 = Integer.parseInt(bufferedReader.readLine());
						int y2 = Integer.parseInt(bufferedReader.readLine());
						String ip = bufferedReader.readLine();

						Zone z = new Zone(x1, y1, x2, y2);

						NeighbourPeer nPeer = new NeighbourPeer(z,
								belongedZones.get(index), ip);

						boolean flag = false;
						for (int j = 0; j < neighbourPeers.size(); j++) {
							if (nPeer.getIPAddress().equals(
									neighbourPeers.get(j).getIPAddress())) {
								flag = true;
							}
							if (belongedZones.get(index) != neighbourPeers.get(
									j).getNeighbourTo()) {
								flag = false;
							}
						}
						if (flag == false) {
							if (!getIP(nPeer.getIPAddress())) {
								neighbourPeers.add(nPeer);

								Socket sendZoneUpdate = new Socket(
										nPeer.getIPAddress(), 9093);
								DataOutputStream dout = new DataOutputStream(
										sendZoneUpdate.getOutputStream());
								dout.writeBytes(belongedZones.get(index)
										.getTop().getx()
										+ " "
										+ belongedZones.get(index).getTop()
												.gety()
										+ " "
										+ belongedZones.get(index).getBottom()
												.getx()
										+ " "
										+ belongedZones.get(index).getBottom()
												.gety()
										+ "-"
										+ isTempZone
										+ "\n");
								sendZoneUpdate.close();

							}
						}

						v = bufferedReader.readLine();
					}

				}

				if (message.length == 5) {
					if (message[0].equals("1")) {
						DataOutputStream d = new DataOutputStream(
								serverSocket.getOutputStream());
						for (int i = 0; i < neighbourPeers.size(); i++) {
							d.writeBytes("801" + "\n");
							d.writeBytes(neighbourPeers.get(i).getIPAddress()
									+ "\n");
						}
						d.writeBytes("802" + "\n");
					}
					if (message[1].equals("1")) {
						DataOutputStream d = new DataOutputStream(
								serverSocket.getOutputStream());
						for (int i = 0; i < belongedZones.size(); i++) {
							d.writeBytes("801" + "\n");
							d.writeBytes(belongedZones.get(i).getTop().getx()
									+ " "
									+ belongedZones.get(i).getTop().gety()
									+ " "
									+ belongedZones.get(i).getBottom().getx()
									+ " "
									+ belongedZones.get(i).getBottom().gety()
									+ "\n");
						}
						d.writeBytes("802" + "\n");
						for (int i = 0; i < neighbourPeers.size(); i++) {
							d.writeBytes("801" + "\n");
							d.writeBytes(neighbourPeers.get(i).getIPAddress()
									+ "\n");
						}
						d.writeBytes("802" + "\n");
						
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}