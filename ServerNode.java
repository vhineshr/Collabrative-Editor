package diff_match_patch;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class ServerClass {
	static boolean endAll;

	int pointX, pointY;
	static boolean isAccessPeer = false;

	ArrayList<Zone> belongedZones;
	ArrayList<NeighbourPeer> neighbourPeers;

	PeerClient peerClient;
	NeighbourServer neighbourServer;
	NeighbourPeerCheck neighbourPeerCheck;
	HearForFile hearForFile;
	DirectoryListServer directoryListServer;
    HearPatchesFromMainText hearPatchesFromMainText;
	ServerSocket listener;
	Socket peerServerSocket;

	public ServerClass() {
		belongedZones = new ArrayList<Zone>();
		neighbourPeers = new ArrayList<NeighbourPeer>();
		
		peerClient = new PeerClient(belongedZones, neighbourPeers );
		neighbourServer = new NeighbourServer(neighbourPeers, belongedZones);
		neighbourPeerCheck = new NeighbourPeerCheck(neighbourPeers,
				belongedZones);
		hearForFile = new HearForFile( neighbourPeers , belongedZones );
		System.out.println(belongedZones.size());
		directoryListServer = new DirectoryListServer(neighbourPeers , belongedZones);
		hearPatchesFromMainText = new HearPatchesFromMainText();

	}

	static boolean getIsAccessPeer() {
		return isAccessPeer;
	}

	static void setIsAccessPeer(boolean isAccess) {
		isAccessPeer = isAccess;
	}

	void run() {
		peerClient.start();
		neighbourServer.start();
		neighbourPeerCheck.start();
		hearForFile.start();
		directoryListServer.start();
		hearPatchesFromMainText.start();
		try {
			listener = new ServerSocket(9092);
			peerClient.setPeerServer(listener);
			while (true) {
				peerServerSocket = listener.accept();
				if (endAll == false) {
					new ResponseClass(belongedZones, neighbourPeers,
							peerServerSocket, peerClient ).start();
				} 
				else 
				{
					break;
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}

public class ServerNode {
	public static void main(String args[]) {
		ServerClass.endAll = false;
		ServerClass serverClass = new ServerClass();
		serverClass.run();
	}
}
