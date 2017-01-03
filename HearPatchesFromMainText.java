package diff_match_patch;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import diff_match_patch.diff_match_patch.Diff;
import diff_match_patch.diff_match_patch.Patch;


class HearPatchesFromMainTextThread extends Thread
{
	
	Socket socket;
	diff_match_patch dmp_object;
	
	public HearPatchesFromMainTextThread( Socket socket ) 
	{
		this.socket = socket;
		dmp_object = new diff_match_patch();
	}
	
	public void run()
	{
		try
		{
			
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			String sentFomClient = bufferedReader.readLine();
			String line = "";	
			String text = "";
			while( !( line =  bufferedReader.readLine() ).equals(")))")  )
			{
				text = text + line + "\n";
			}
			FileWriter fileWriter = new FileWriter("TextDoc.txt");
			fileWriter.write(text);
			fileWriter.flush();
			fileWriter.close();
			for( int i = 0; i < HearForFile.clientIp.size(); i++ )
			{
				if( !sentFomClient.equals(HearForFile.clientIp.get(i)) )
				{
					Socket sendUpdatedFile = new Socket( HearForFile.clientIp.get(i) , 10001 );
					BufferedReader bufferedReade = new BufferedReader( new FileReader( "TextDoc.txt" ) );
					DataOutputStream dpsrtm = new DataOutputStream( sendUpdatedFile.getOutputStream() );
					String lin = "";
					while( ( lin = bufferedReade.readLine() ) != null )
					{
						dpsrtm.writeBytes( lin + "\n" );
					}
					dpsrtm.writeBytes( ")))" + "\n" );
					sendUpdatedFile.close();
				}
			}
			Socket socketUpdate = new Socket( HearForFile.DirectoryListIP , 10002 );
			DataOutputStream doutS = new DataOutputStream( socketUpdate.getOutputStream() );
			doutS.writeBytes( "1-"+"TextDoc.txt"+"-"+sentFomClient+"- " + "\n" );
			socketUpdate.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}

public class HearPatchesFromMainText extends Thread
{
	ServerSocket listener;
	
	public HearPatchesFromMainText() 
	{
		try
		{
			listener = new ServerSocket(10013);
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
				new HearPatchesFromMainTextThread( socket ).start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
