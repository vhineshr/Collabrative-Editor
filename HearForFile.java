package diff_match_patch;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;


class HearFromFileResponse extends Thread
{
	
	Socket socket; 
	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;
	Zone requestedPointInZone;
	int belongedToIndex;
	
	public HearFromFileResponse( Socket socket, ArrayList<NeighbourPeer> neighbourPeers, ArrayList<Zone> belongedZones)
	{
		this.neighbourPeers = neighbourPeers;
		this.belongedZones = belongedZones;
		this.socket = socket;
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
	
	public void run()
	{
		try
		{
			String clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());
			BufferedReader bufferedInputStream = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			String fileNeeded = bufferedInputStream.readLine();
			HearForFile.clientIp.add(clientIP);
			if( HearForFile.fileName.equals("TextDoc.txt") )
			{
				Socket sendFile = new Socket( clientIP , 10001);
				BufferedReader bufferedReader = new BufferedReader( new FileReader( "TextDoc.txt" ) );
				DataOutputStream dataOutputStream = new DataOutputStream( sendFile.getOutputStream() );
				String line = "";
				while( ( line = bufferedReader.readLine() ) != null )
				{
					dataOutputStream.writeBytes( line + "\n" );
				}
				dataOutputStream.writeBytes( ")))" + "\n" );
				
				sendFile.close();
				
			}
			else
			{
				String hashPoints =  getHashFunction( fileNeeded ) ;
				String points[] = hashPoints.split(" ");
				if( isMyZone(hashPoints) )
				{
					Socket socketPeer = new Socket( getIP() , 10002 );
					DataOutputStream dataOutputStream = new DataOutputStream( socketPeer.getOutputStream() );
					dataOutputStream.writeBytes(  getIP() + "-"+ hashPoints +"-" + clientIP + "\n" );
					socketPeer.close();
				}
				else
				{
					NeighbourPeer nPeer = checkTraverse( Integer.parseInt(points[0] ), Integer.parseInt(points[1]) );
					Socket socketPeer = new Socket( nPeer.getIPAddress() , 10002 );
					DataOutputStream dataOutputStream = new DataOutputStream( socketPeer.getOutputStream() );
					dataOutputStream.writeBytes(  getIP() + "-"+ hashPoints +"-" + clientIP + "\n" );
					socketPeer.close();
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

public class HearForFile extends Thread
{
	
	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;
	static ArrayList<String> clientIp = new ArrayList<String>();
	String filename = "";
	ServerSocket listener;
	static String fileName = "";
	static String DirectoryListIP = "";
	static boolean isMainTextCache = false;
	
	public HearForFile( ArrayList<NeighbourPeer> neighbourPeers, ArrayList<Zone> belongedZones )
	{
		try
		{
			this.neighbourPeers = neighbourPeers;
			this.belongedZones = belongedZones;
			listener = new ServerSocket(10000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while( true )
		{
			try
			{
				Socket socket = listener.accept();
				new HearFromFileResponse( socket , neighbourPeers , belongedZones).start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
