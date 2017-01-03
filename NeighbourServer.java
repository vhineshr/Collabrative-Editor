package diff_match_patch;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class checkNeighbour extends Thread {
	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;
	Socket socket;

	public checkNeighbour(ArrayList<NeighbourPeer> neighbourPeers,
			ArrayList<Zone> belongedZones, Socket socket) {
		this.neighbourPeers = neighbourPeers;
		this.belongedZones = belongedZones;
		this.socket = socket;
	}

	boolean isHisZone(String message, Zone z) {
		String zone[] = message.split(" ");
		int bx1 = z.getTop().getx();
		int by1 = z.getTop().gety();
		int bx2 = z.getBottom().getx();
		int by2 = z.getBottom().gety();

		if (Integer.parseInt(zone[0]) == bx1
				&& Integer.parseInt(zone[1]) == by1
				&& Integer.parseInt(zone[2]) == bx2
				&& Integer.parseInt(zone[3]) == by2) {
			return true;
		} else {
			return false;
		}
	}

	boolean checkNeighbourFunction(Zone myZone, Zone neighbourZone) {
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

	public synchronized void run() {
		try {
			DataOutputStream dOut = new DataOutputStream(
					socket.getOutputStream());
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			String clientMessage = bufferedReader.readLine();
			String message[] = clientMessage.split("-");
			String ip = socket.getInetAddress().toString();
			ip = ip.substring(1, ip.length());

			if (message.length == 1) {
				boolean flag = false;
				for (int i = 0; i < neighbourPeers.size(); i++) {
					if (neighbourPeers.get(i).getIPAddress().equals(ip)) {
						flag = true;
						break;
					}
				}
				if (flag == true) {
					dOut.writeBytes("1" + "\n");
				} else {
					dOut.writeBytes("0" + "\n");
				}
			}

			else if (message.length == 2) {
				String zone[] = message[0].split(" ");
				Zone neighbourTo = null;
				Zone z = new Zone(Integer.parseInt(zone[0]),
						Integer.parseInt(zone[1]), Integer.parseInt(zone[2]),
						Integer.parseInt(zone[3]));
				for (int i = 0; i < belongedZones.size(); i++) {
					if (checkNeighbourFunction(belongedZones.get(i), z)) {
						neighbourTo = belongedZones.get(i);
					}
				}
				NeighbourPeer nPeer = new NeighbourPeer(z, neighbourTo, ip);

				boolean flag = false;
				for (int i = 0; i < neighbourPeers.size(); i++) {
					if (neighbourPeers.get(i).getIPAddress().equals(ip)
							&& message[1].equals("0")) {
						flag = true;
					}
					if (isHisZone(message[0], neighbourPeers.get(i).getZone())) {
						flag = true;
					}
				}
				if (flag == false) {
					if (checkNeighbourFunction(nPeer.getNeighbourTo(),
							nPeer.getZone())) {
						try {
							neighbourPeers.add(nPeer);
						} catch (NullPointerException e) {

						}
					}
				}
			}

			else if (message.length == 3) {
				String zone[] = message[0].split(" ");
				String ipClient = socket.getInetAddress().toString();
				ipClient = ipClient.substring(1, ipClient.length());
				for (int i = 0; i < neighbourPeers.size(); i++) {
					if (ipClient.equals(neighbourPeers.get(i).getIPAddress())) {

						int x1 = Integer.parseInt(zone[0]);
						int y1 = Integer.parseInt(zone[1]);
						int x2 = Integer.parseInt(zone[2]);
						int y2 = Integer.parseInt(zone[3]);
						Zone z = new Zone(x1, y1, x2, y2);
						if (isHisZone(message[2], neighbourPeers.get(i)
								.getZone())) {
							neighbourPeers.get(i).setZone(z);
						}
					}
				}
			}

			else if (message.length == 4) {
				String ipClient = socket.getInetAddress().toString();
				ipClient = ipClient.substring(1, ipClient.length());
				for (int i = neighbourPeers.size() - 1; i >= 0; i--) {
					if (ipClient.equals(neighbourPeers.get(i).getIPAddress())) {
						String belonledTo = neighbourPeers.get(i).getZone()
								.getTop().getx()
								+ " "
								+ neighbourPeers.get(i).getZone().getTop()
										.gety()
								+ " "
								+ neighbourPeers.get(i).getZone().getBottom()
										.getx()
								+ " "
								+ neighbourPeers.get(i).getZone().getBottom()
										.gety();
						if (belonledTo.equals(message[0])) {
							neighbourPeers.remove(i);
						}
					}
				}
			}

			socket.close();

		} catch (Exception e) {

		}

	}

}

public class NeighbourServer extends Thread {

	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;

	public NeighbourServer(ArrayList<NeighbourPeer> neighbourPeers,
			ArrayList<Zone> belongedZones) {
		this.neighbourPeers = neighbourPeers;
		this.belongedZones = belongedZones;
	}

	public void run() {
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(9093);
			while (true) {
				Socket socket = listener.accept();
				if (ServerClass.endAll == false) {
					new checkNeighbour(neighbourPeers, belongedZones, socket)
							.start();
				} else {
					break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
