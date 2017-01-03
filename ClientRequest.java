package diff_match_patch;

import java.io.DataOutputStream;
import java.net.Socket;


public class ClientRequest 
{
	
	static String clientShadow = "";
	static String cacheIP = "129.21.85.189";
	
	public static void main(String args[])
	{
		
		ClientUI ui = null;
		ClientDifferentialSyncronisation clientDifferentialSyncronisation = null;
		
		new ClientServer( ui , clientDifferentialSyncronisation ).start();
		String fileName = "TextDoc.txt";
		
		try
		{ 
			Socket socket = new Socket( cacheIP  , 10000);
			DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
			dataOutputStream.writeBytes( fileName + "\n" ); 
			Thread.sleep(30000);
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
