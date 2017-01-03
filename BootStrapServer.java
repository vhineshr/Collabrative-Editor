package diff_match_patch;




import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BootStrapServer extends Thread {

	AccessPeerAddress accessPeerAddress;
	boolean hasAccessPeer;

	public BootStrapServer(AccessPeerAddress accessPeerAddress) {
		this.accessPeerAddress = accessPeerAddress;
		hasAccessPeer = false;
	}

	public void run() {
		System.out.println("BootStrapServer started");
		ServerSocket listener = null;

		try {
			listener = new ServerSocket(9090);
			BufferedReader bufferedReader;
			DataOutputStream dOut;

			while (true) {
				Socket bootSocket = listener.accept();
				bufferedReader = new BufferedReader(new InputStreamReader(
						bootSocket.getInputStream()));
				dOut = new DataOutputStream(bootSocket.getOutputStream());

				InetAddress inetAddress;

				String randomPointString = bufferedReader.readLine();

				System.out.println("BootStrapServer: " + randomPointString);

				inetAddress = bootSocket.getInetAddress();
				System.out.println("BootStrapServer: " + inetAddress);

				if (hasAccessPeer == false) {
					dOut.writeBytes("noAccessPeer" + "\n");
					String accessPeerAck = bufferedReader.readLine();
					if (accessPeerAck.equalsIgnoreCase("accessPeerAck")) {
						hasAccessPeer = true;
						System.out.println("BootStrapServer: " + inetAddress
								+ " is the AccessPeer to the zone");

					}
				} else {
					dOut.writeBytes(accessPeerAddress.getACPeerAddress() + "\n");
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
