
package diff_match_patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFrame;

import diff_match_patch.diff_match_patch.Diff;

class DifferentialSyncronisation extends Thread
{
	
	ArrayList< String > arrayList = new ArrayList<String>();
	
	File file = new File("TextDoc.txt");
	
	diff_match_patch dmp_object = new diff_match_patch();
	UI uiObject;
	
	public DifferentialSyncronisation( ArrayList<String>  arrayList , UI uiObject)
	{
		this.arrayList = arrayList;
		this.uiObject = uiObject;
		
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new FileReader("TextDoc.txt") );
			StringBuilder readingString = new StringBuilder();
			String line = bufferedReader.readLine();
			while( line != null )
			{
				readingString.append(line + "\n");
				line = bufferedReader.readLine();
			}
			
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
				Thread.sleep(1000);
			
				String textAreaString = uiObject.textArea.getText();
				for(int i=0;i<arrayList.size();i++)
				{
					
					LinkedList<Diff> listDiff = dmp_object.diff_main( MainTextServer.MainServerShadow, textAreaString );
					Socket socket = new Socket(arrayList.get(i), 10013);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream( socket.getOutputStream() );
					objectOutputStream.writeObject(listDiff);
					MainTextServer.MainServerShadow = textAreaString;
					
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}

public class MainTextServer 
{
	static int oneWay=0;
	static String MainServerShadow="";
	public static void main(String args[])
	{
		
		ArrayList<String> hierarchy = new ArrayList<String>();
		
		UI ui = new UI();
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		MainTextServerThread mainTextServerThread = new MainTextServerThread( hierarchy );
		mainTextServerThread.start();
			
		HearChangeFromClient hearChangeFromClient = new HearChangeFromClient(hierarchy,ui);
		hearChangeFromClient.start();
		
	}
}
