package diff_match_patch;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class MainTextServerResponse extends Thread
{
	Socket socket;
	ArrayList<String> hierarchy;
	File file;
	
	public MainTextServerResponse( ArrayList<String> hierarchy , Socket socket)
	{
		this.socket = socket;
		this.hierarchy = hierarchy;
		file = new File("TextDoc.txt");
	}
	
	public void run()
	{
		try
		{
			String clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring( 1, clientIP.length() );
			hierarchy.add( clientIP );
			for( int i = 0 ; i < hierarchy.size(); i++ )
			{
				System.out.println(hierarchy.get(i));
			}
			BufferedReader bufferedReader = new BufferedReader( new FileReader( "TextDoc.txt" ) );
			DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
			String line = "";
			while( ( line = bufferedReader.readLine() ) != null )
			{
				dataOutputStream.writeBytes( line + "\n" );
			}
			dataOutputStream.writeBytes( ")))" + "\n" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
public class MainTextServerThread extends Thread
{
	ServerSocket listener ;
	ArrayList<String> hierarchy;
	
	public MainTextServerThread( ArrayList<String> hierarchy ) 
	{
		try
		{
			this.hierarchy = hierarchy;
			listener = new ServerSocket(10010);
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
				new MainTextServerResponse( hierarchy , socket ).start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
