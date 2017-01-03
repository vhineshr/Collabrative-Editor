package diff_match_patch;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;

public class PeerClient extends Thread {

	int pointX, pointY;

	ArrayList<Zone> belongedZones;
	ArrayList<NeighbourPeer> neighbourPeers;
	Random randomGenerator;

	Socket requestAccess;
	Socket bridgeServer;
	ServerSocket peerServer;

	String zoneHolderAddressString;
	boolean isZoneHolderAddressString = false;

	void setPeerServer(ServerSocket peerServer) {
		this.peerServer = peerServer;
	}

	void setIsZoneHolderAddressing(boolean isZoneHolderAddressString) {
		this.isZoneHolderAddressString = isZoneHolderAddressString;
	}

	void setPeerInetAddress(String zoneHolderAddressString) {
		this.zoneHolderAddressString = zoneHolderAddressString;
		isZoneHolderAddressString = true;
	}

	public PeerClient(ArrayList<Zone> belongedZones,
			ArrayList<NeighbourPeer> neighbourPeers ) {
		this.belongedZones = belongedZones;
		this.neighbourPeers = neighbourPeers;
		randomGenerator = new Random();
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

	String getIP() {
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

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return ip;
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

	NeighbourPeer checkTraverse(int x, int y) {
		NeighbourPeer nPeer = null;
		int minDistance = 0;
		boolean isInMyNeighbour = false;

		for (int j = 0; j < neighbourPeers.size(); j++) {
			isInMyNeighbour = isNeighbourZone(x + " " + y, neighbourPeers
					.get(j).getZone());
			if (isInMyNeighbour == true) {
				nPeer = neighbourPeers.get(j);
				break;
			}
		}
		if (isInMyNeighbour == false) {
			for (int i = 0; i < neighbourPeers.size(); i++) {
				int x1 = neighbourPeers.get(i).getNeighbourTo().getTop().getx();
				int y1 = neighbourPeers.get(i).getNeighbourTo().getTop().gety();
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
					nPeer = neighbourPeers.get(i);
				} else {
					if (distance < minDistance) {
						minDistance = distance;
						nPeer = neighbourPeers.get(i);
					}
				}

			}
		}
		return nPeer;
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

	public void run() {
		try {
			requestAccess = new Socket("129.21.158.162", 9090);
			DataOutputStream dOut = new DataOutputStream(
					requestAccess.getOutputStream());
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(requestAccess.getInputStream()));

			pointX = randomGenerator.nextInt(800);
			pointY = randomGenerator.nextInt(800);

			String randomPointString = pointX + " " + pointY;
			dOut.writeBytes(randomPointString + "\n");

			String accessPeerAddress = bufferedReader.readLine();

			if (accessPeerAddress.equalsIgnoreCase("noAccessPeer")) {

				bridgeServer = new Socket("129.21.158.162", 9091); // 129.21.30.38
				BufferedReader getReplyBridgeServer = new BufferedReader(
						new InputStreamReader(bridgeServer.getInputStream()));

				getReplyBridgeServer.readLine();

				dOut.writeBytes("accessPeerAck" + "\n");

				ServerClass.setIsAccessPeer(true);

				Zone zone = new Zone(0, 0, 800, 800);
				belongedZones.add(zone);
			} else {
				String iAddress = accessPeerAddress.substring(1,
						accessPeerAddress.length());
				try {
					Socket accessSocket = new Socket(iAddress, 9092);
					DataOutputStream dataOutputStreamOut = new DataOutputStream(
							accessSocket.getOutputStream());
					dataOutputStreamOut.writeBytes(getIP() + "-"
							+ randomPointString + "-" + "\n");
					while (isZoneHolderAddressString == false) {
						System.out.print("");
					}

					// update neighbours about me
					for (int i = 0; i < neighbourPeers.size(); i++) {
						Socket socket = new Socket(neighbourPeers.get(i)
								.getIPAddress(), 9093);
						DataOutputStream dOutStream = new DataOutputStream(
								socket.getOutputStream());
						String zone = belongedZones.get(0).getTop().getx()
								+ " " + belongedZones.get(0).getTop().gety()
								+ " " + belongedZones.get(0).getBottom().getx()
								+ " " + belongedZones.get(0).getBottom().gety();
						dOutStream.writeBytes(zone + "-" + "0" + "\n");
						socket.close();
					}

					accessSocket.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			while (true) {
				int switchInt;
				int flag = 0;

				System.out.println("1.View");
				System.out.println("2.Leave");
				try {
					String value = scanner.next();
					switchInt = Integer.parseInt(value);
				} catch (Exception e) {
					switchInt = 6;
				}
				switch (switchInt) {
				case 1: {
					@SuppressWarnings("resource")
					Scanner scan = new Scanner(System.in);
					int whichDetail;
					System.out
							.println("1. Press 1 to get details of whole zone");
					System.out
							.println("2. press 2 to get details of particular ip");
					ArrayList<String> zoneList = new ArrayList<String>();
					zoneList.add(getIP());
					for (int i = 0; i < neighbourPeers.size(); i++) {
						zoneList.add(neighbourPeers.get(i).getIPAddress());
					}
					int ptr = 0;
					while (ptr < zoneList.size()) {
						if (!getIP(zoneList.get(ptr))) {
							Socket socket = new Socket(zoneList.get(ptr), 9092);
							DataOutputStream dataOut = new DataOutputStream(
									socket.getOutputStream());
							BufferedReader bReader = new BufferedReader(
									new InputStreamReader(
											socket.getInputStream()));
							dataOut.writeBytes("1- - - -" + getIP() + "\n");
							String v = bReader.readLine();
							while (!v.equals("802")) {
								String ip = bReader.readLine();
								if (!zoneList.contains(ip)) {
									zoneList.add(ip);
								}
								v = bReader.readLine();
							}
							socket.close();
						}

						ptr++;
					}

					whichDetail = scan.nextInt();
					switch (whichDetail) {
					case 1: {
						System.out.println("My IP address:" + getIP());
						System.out.println("My zone:");
						for (int i = 0; i < belongedZones.size(); i++) {
							System.out.print("	");
							System.out.println(belongedZones.get(i).getTop()
									.getx()
									+ " "
									+ belongedZones.get(i).getTop().gety()
									+ " "
									+ belongedZones.get(i).getBottom().getx()
									+ " "
									+ belongedZones.get(i).getBottom().gety());
						}
						System.out.println("My Neighbours:");
						for (int i = 0; i < neighbourPeers.size(); i++) {
							System.out.println("	"
									+ neighbourPeers.get(i).getIPAddress());
						}
						
						System.out.println("Neighbour details:");
						for (int i = 1; i < zoneList.size(); i++) {
							System.out.println("	IP Address" + zoneList.get(i));
							Socket socket = new Socket(zoneList.get(i), 9092);
							DataOutputStream dataOut = new DataOutputStream(
									socket.getOutputStream());
							BufferedReader bReader = new BufferedReader(
									new InputStreamReader(
											socket.getInputStream()));
							dataOut.writeBytes(" -1- - -" + getIP() + "\n");
							String v = bReader.readLine();
							System.out.println("		zone details");
							while (!v.equals("802")) {
								System.out.println("			" + bReader.readLine());
								v = bReader.readLine();
							}
							v = bReader.readLine();
							System.out.println("		neighbour list");
							while (!v.equals("802")) {
								System.out.println("			" + bReader.readLine());
								v = bReader.readLine();
							}
							socket.close();
						}
						break;
					}
					case 2: {
						@SuppressWarnings("resource")
						Scanner getIP = new Scanner(System.in);
						String ip = getIP.nextLine();
						if (zoneList.contains(ip)) {
							Socket socket = new Socket(ip, 9092);
							DataOutputStream dataOut = new DataOutputStream(
									socket.getOutputStream());
							BufferedReader bReader = new BufferedReader(
									new InputStreamReader(
											socket.getInputStream()));
							dataOut.writeBytes(" -1- - -" + getIP() + "\n");
							String v = bReader.readLine();
							System.out.println("zone details");
							while (!v.equals("802")) {
								System.out.println("	" + bReader.readLine());
								v = bReader.readLine();
							}
							v = bReader.readLine();
							System.out.println("neighbour list");
							while (!v.equals("802")) {
								System.out.println("	" + bReader.readLine());
								v = bReader.readLine();
							}
							socket.close();
						} else {
							System.out.println("IP is not in the zone");
						}
						break;
					}
					default: {
						System.out.println("wrong entry");
					}
					}
					break;
				}
				case 2: {
					String ip = "";
					int tellNeighbourZone = 0;
					for (int i = 0; i < belongedZones.size(); i++) {
						int bx1 = belongedZones.get(i).getTop().getx();
						int by1 = belongedZones.get(i).getTop().gety();
						int bx2 = belongedZones.get(i).getBottom().getx();
						int by2 = belongedZones.get(i).getBottom().gety();
						int x1 = 0, x2 = 0, y1 = 0, y2 = 0;

						for (int j = 0; j < neighbourPeers.size(); j++) {
							if (belongedZones.get(i) == neighbourPeers.get(j)
									.getNeighbourTo()) {
								x1 = neighbourPeers.get(j).getZone().getTop()
										.getx();
								y1 = neighbourPeers.get(j).getZone().getTop()
										.gety();
								x2 = neighbourPeers.get(j).getZone()
										.getBottom().getx();
								y2 = neighbourPeers.get(j).getZone()
										.getBottom().gety();
								if ((x1 == bx1 && x2 == bx2)
										|| (y1 == by1 && y2 == by2)) {
									ip = neighbourPeers.get(j).getIPAddress();
									tellNeighbourZone = 0;
									break;
								}
							}
							if (j == neighbourPeers.size() - 1 && ip.equals("")) {
								int min = 0;
								for (int k = 0; k < neighbourPeers.size(); k++) {
									if (belongedZones.get(i) == neighbourPeers
											.get(k).getNeighbourTo()) {
										x1 = neighbourPeers.get(k).getZone()
												.getTop().getx();
										y1 = neighbourPeers.get(k).getZone()
												.getTop().gety();
										x2 = neighbourPeers.get(k).getZone()
												.getBottom().getx();
										y2 = neighbourPeers.get(k).getZone()
												.getBottom().gety();
										int dist = ((x1 - x2) * (x1 - x2) + ((y1 - y2) * (y1 - y2)));
										if (min == 0) {
											min = dist;
											ip = neighbourPeers.get(k)
													.getIPAddress();
											tellNeighbourZone = 1;
										} else {
											if (dist < min) {
												min = dist;
												ip = neighbourPeers.get(k)
														.getIPAddress();
												tellNeighbourZone = 1;
											}
										}
									}
								}
							}
							System.out.println(ip);
						}
						Socket socket = new Socket(ip, 9092);
						DataOutputStream d = new DataOutputStream(
								socket.getOutputStream());
						int isAccessP = 0;
						if (ServerClass.isAccessPeer == true) {
							isAccessP = 1;
							Socket bSocket = new Socket("129.21.158.162", 9091);
							DataOutputStream dbSocket = new DataOutputStream(
									bSocket.getOutputStream());
							dbSocket.writeBytes("/" + ip + "\n");
							bSocket.close();
						}

						d.writeBytes(bx1 + " " + by1 + " " + bx2 + " " + by2
								+ "-" + tellNeighbourZone + "-" + isAccessP
								+ "-" + x1 + " " + y1 + " " + x2 + " " + y2
								+ "\n");

						// remove me from neighbour peers
						for (int x = 0; x < neighbourPeers.size(); x++) {
							Socket NSSocket = new Socket(neighbourPeers.get(x)
									.getIPAddress(), 9093);
							DataOutputStream nssdOut = new DataOutputStream(
									NSSocket.getOutputStream());
							String belongTo = neighbourPeers.get(x)
									.getNeighbourTo().getTop().getx()
									+ " "
									+ neighbourPeers.get(x).getNeighbourTo()
											.getTop().gety()
									+ " "
									+ neighbourPeers.get(x).getNeighbourTo()
											.getBottom().getx()
									+ " "
									+ neighbourPeers.get(x).getNeighbourTo()
											.getBottom().gety();
							nssdOut.writeBytes(belongTo + "- - - " + "\n");
							NSSocket.close();
						}

						for (int k = 0; k < neighbourPeers.size(); k++) {
							if (neighbourPeers.get(k).getNeighbourTo() == belongedZones
									.get(i)) {
								d.writeBytes("801" + "\n");
								d.writeBytes(Integer.toString(neighbourPeers
										.get(k).getZone().getTop().getx())
										+ "\n");
								d.writeBytes(Integer.toString(neighbourPeers
										.get(k).getZone().getTop().gety())
										+ "\n");
								d.writeBytes(Integer.toString(neighbourPeers
										.get(k).getZone().getBottom().getx())
										+ "\n");
								d.writeBytes(Integer.toString(neighbourPeers
										.get(k).getZone().getBottom().gety())
										+ "\n");
								d.writeBytes(neighbourPeers.get(k)
										.getIPAddress() + "\n");
							}
						}
						d.writeBytes("802" + "\n");
						belongedZones.get(i).setZone(802, 802, 802, 802);
						socket.close();

					}
					flag = 1;
					break;
				}
				case 3:
				{
					System.out.println(" Enter the file name with .txt extension ");
					@SuppressWarnings("resource")
					Scanner getFile = new Scanner(System.in);
					String fileName = getFile.nextLine();
					String hashPoints = getHashFunction(fileName);
					String points[] = hashPoints.split(" ");
					NeighbourPeer nPeer = checkTraverse(
					Integer.parseInt(points[0]),
					Integer.parseInt(points[1]));
					if (isMyZone(hashPoints)) {
						System.out.println("Directory List is in your zone...");
					} else {
						Socket socket = new Socket(nPeer.getIPAddress(), 9092);
						DataOutputStream d = new DataOutputStream(
						socket.getOutputStream());
						System.out.println(hashPoints + "-" + fileName + "-"
						+ getIP());
						d.writeBytes(getHashFunction(fileName) + "-" + fileName
						+ "-" + getIP() + "\n");
						socket.close();
					}
					break;
				}
				default: {
					System.out.println("Wrong entry...");
				}
				}

				if (flag == 1) {
					break;
				}

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		try {
			ServerClass.endAll = true;
			Socket serverClose = new Socket(getIP(), 9092);
			serverClose.close();
			Socket serverCloseNS = new Socket(getIP(), 9093);
			serverCloseNS.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
