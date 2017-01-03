package diff_match_patch;

import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;


public class DeleteOldFile extends Thread 
{
	
	String fileName = "";
	
	public DeleteOldFile( String fileName ) 
	{
		this.fileName = fileName;
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
	
	public void run()
	{
		try
		{
			sleep(10000);
			if( HearForFile.clientIp.isEmpty() && HearForFile.isMainTextCache != true )
			{
				File file = new File( fileName );
				file.delete();
				HearForFile.fileName = "";
				Socket socket = new Socket( HearForFile.DirectoryListIP , 10002 );
				DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
				dataOutputStream.writeBytes( getIP() + "-" + fileName +"\n" );
				socket.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
}
