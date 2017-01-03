package diff_match_patch;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class NeighbourPeerCheck extends Thread {

	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;

	public NeighbourPeerCheck(ArrayList<NeighbourPeer> neighbourPeers,
			ArrayList<Zone> belongedZones) {
		this.neighbourPeers = neighbourPeers;
		this.belongedZones = belongedZones;
	}

	public void run() {
		while (true) {
			if (ServerClass.endAll == false) {
				try {
					Thread.sleep(20000);
					int size = neighbourPeers.size();
					for (int i = size - 1; i >= 0; i--) {
						Socket socket = new Socket(neighbourPeers.get(i)
								.getIPAddress(), 9093);
						DataOutputStream dOut = new DataOutputStream(
								socket.getOutputStream());
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));

						dOut.writeBytes(" " + "\n");
						String read = bufferedReader.readLine();
						if (read.equals("0")) {
							neighbourPeers.remove(i);
						}
						socket.close();
					}

				} catch (ArrayIndexOutOfBoundsException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
	}

}
