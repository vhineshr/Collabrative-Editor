package diff_match_patch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import diff_match_patch.diff_match_patch.Diff;


public class ClientServer extends Thread
{
	ServerSocket listener; 
	int count=1;
	ClientUI ui;
	ClientDifferentialSyncronisation clientDifferentialSyncronisation;
	public ClientServer( ClientUI ui , ClientDifferentialSyncronisation clientDifferentialSyncronisation) 
	{
		try
		{
			listener = new ServerSocket(10001);
			this.ui = ui;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				Socket socket = listener.accept();
				BufferedReader bReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
				BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter("TextDoc.txt") );
			
				String line = "";
				String text = "";
				while( !( line = bReader.readLine() ).equals(")))")  )
				{
					text = text + line + "\n";
				}
				System.out.println("Text + "+  text);
				bufferedWriter.write( text );
				bufferedWriter.flush();
				bufferedWriter.close();
				
				if(count == 1)
				{
					ui = new ClientUI( clientDifferentialSyncronisation );
	            	count++;
				}
				else
				{
					ClientRequest.clientShadow = text;
					ui.textArea.setText(text);
				}
			
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

class ClientDifferentialSyncronisation extends Thread
{

	ClientUI clintUi;
	diff_match_patch dmp_obj = new diff_match_patch();
	
	public ClientDifferentialSyncronisation( ClientUI clintUi ) 
	{
		this.clintUi = clintUi;
	}
	
	public void run()
	{
		while( true )
		{
			try
			{
				Thread.sleep(1000);
				String textAreaString = clintUi.textArea.getText();
				if( !textAreaString.equals( ClientRequest.clientShadow ) )
				{
					LinkedList<Diff> listDiff = dmp_obj.diff_main(  ClientRequest.clientShadow,textAreaString );
					Socket socket = new Socket("129.21.158.162", 9997);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream( socket.getOutputStream() );
					objectOutputStream.writeObject(listDiff);
					ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
					@SuppressWarnings("unchecked")
					ArrayList<String> reply = ( ArrayList<String> ) objectInputStream.readObject();
					if( reply.get(0).equals("yes") )
					{
						ClientRequest.clientShadow = textAreaString;
					}
					else
					{
						clintUi.textArea.setText( ClientRequest.clientShadow );
					}
					socket.close();
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
