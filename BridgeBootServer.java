package diff_match_patch;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

class AccessPeerAddress {
	String acPeerAddress;

	public AccessPeerAddress() {
		acPeerAddress = "";
	}

	void setACPeerAddress(String acPeerAddress) {
		this.acPeerAddress = acPeerAddress;
	}

	String getACPeerAddress() {
		return acPeerAddress;
	}
}

public class BridgeBootServer {
	public static void main(String args[]) {

		AccessPeerAddress accessPeerAddress = new AccessPeerAddress();

		BootStrapServer bootStrapServer = new BootStrapServer(accessPeerAddress);
		bootStrapServer.start();

		ServerSocket listener = null;

		try {
			listener = new ServerSocket(9091);

			while (true) {

				Socket bridgeSocket = listener.accept();

				DataOutputStream dOut = new DataOutputStream(
						bridgeSocket.getOutputStream());

				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(bridgeSocket.getInputStream()));

				if (accessPeerAddress.getACPeerAddress().equals("")) {
					accessPeerAddress.setACPeerAddress(bridgeSocket
							.getInetAddress().toString());

					dOut.writeBytes("You are the new Access peer to the zone"
							+ "\n");

					System.out.println("BridgeBootServer : "
							+ accessPeerAddress.getACPeerAddress());
				} else {
					accessPeerAddress.setACPeerAddress(bufferedReader
							.readLine());
					System.out.println("New Access peer "
							+ accessPeerAddress.getACPeerAddress());
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
