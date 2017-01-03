package diff_match_patch;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import diff_match_patch.diff_match_patch.Diff;
import diff_match_patch.diff_match_patch.Patch;

public class HearChangeFromClient extends Thread
{
	
	diff_match_patch dmp_object;
	ServerSocket listener;
	UI uiObject;
	ArrayList<String> hierarchy;
	
	public HearChangeFromClient() 
	{
		
	}
	HearChangeFromClient( ArrayList<String> hierarchy,UI uiObject )
	{
		try
		{
			this.uiObject = uiObject;
			this.hierarchy=hierarchy;
			dmp_object = new diff_match_patch();
			listener = new ServerSocket(9997);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				Socket socket = listener.accept();
				new HearChangeFromClientThread(hierarchy, socket,uiObject).start();
		        
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
class HearChangeFromClientThread extends Thread
{
	Socket socket;
	diff_match_patch dmp_object;
	ArrayList<String> hierarchy;
	HearChangeFromClient hearChangeFromClient;
	static int lockValue =0;
	static final Object lock = new Object();
	UI uiObject;
	public HearChangeFromClientThread(ArrayList<String> hierarchy,Socket socket,UI uiObject) 
	{
		this.socket=socket;
		dmp_object = new diff_match_patch();
		this.hierarchy=hierarchy;
		hearChangeFromClient = new HearChangeFromClient();
		this.uiObject=uiObject;
	}
	public  void upDateTextArea( LinkedList<Diff> diffFromClient )
	{
		
		try
		{
			LinkedList<Patch> patch1 = dmp_object.patch_make(MainTextServer.MainServerShadow, diffFromClient);
			Object[] serverShadowPatchObject = dmp_object.patch_apply(patch1, MainTextServer.MainServerShadow);
			String previousShadow = MainTextServer.MainServerShadow ;
			MainTextServer.MainServerShadow = serverShadowPatchObject[0].toString();
			String textAreaString = uiObject.textArea.getText();
			
			LinkedList<Patch> patch2 = dmp_object.patch_make( textAreaString, diffFromClient );
			Object[] serverPatchObject = dmp_object.patch_apply(patch2, textAreaString );
			uiObject.textArea.setText(serverPatchObject[0].toString());
			String clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());
			ArrayList<String> sendIp = new ArrayList<String>();
			sendIp.add(clientIP);
			ObjectOutputStream oOutputStream = new ObjectOutputStream(socket.getOutputStream());
			if( previousShadow.equals(MainTextServer.MainServerShadow) )
			{
				ArrayList<String> send = new ArrayList<String>();
				send.add("no");
				oOutputStream.writeObject(send);
				oOutputStream.flush();
			}
			else
			{
				ArrayList<String> send = new ArrayList<String>();
				send.add("yes");
				oOutputStream.writeObject(send);
				oOutputStream.flush();
			}
			for( int i = 0; i < hierarchy.size(); i++ )
			{
				try
				{
					Socket sendFileSocket = new Socket( hierarchy.get(i) , 10013 );
					DataOutputStream dstream = new DataOutputStream( sendFileSocket.getOutputStream() );
					dstream.writeBytes( clientIP + "\n" );
					BufferedReader bufferedRead = new BufferedReader( new FileReader( "TextDoc.txt" ) );
					String line = "";
					while( ( line = bufferedRead.readLine() ) != null )
					{
						dstream.writeBytes( line + "\n" );
					}
					dstream.writeBytes( ")))" + "\n" );
					sendFileSocket.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				ObjectOutputStream oOutputStream = new ObjectOutputStream(socket.getOutputStream());
				ArrayList<String> send = new ArrayList<String>();
				send.add("no");
				oOutputStream.writeObject(send);
				oOutputStream.flush();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	public void run()
	{
		try
		{
			String ip = socket.getInetAddress().toString();
			ip = ip.substring(1,ip.length() );
			
			ObjectInputStream objectInputStream = new ObjectInputStream( socket.getInputStream() );
			LinkedList<Diff> diffFromClient = (LinkedList<Diff>) objectInputStream.readObject();
			upDateTextArea(diffFromClient);
		
		}
		catch(Exception e)
		{
			
		}
	}
}