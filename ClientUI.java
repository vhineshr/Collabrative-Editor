package diff_match_patch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;


public class ClientUI extends JFrame
{	
	
	JFrame frame = new JFrame("ClientTextEditor");
    Container content = frame.getContentPane();
    JTextArea textArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(textArea);
    final Document document = textArea.getDocument();
    File file;
    ClientDifferentialSyncronisation clientDifferentialSyncronisation;
    int count = 0;
    ClientUI THIS = this;
    TextField textField = new TextField();
	
	ClientUI( ClientDifferentialSyncronisation clientDifferentialSyncronisation )
	{
		try
		{
			file = new File("TextDoc.txt");
			
			JMenuBar menubar = new JMenuBar();
			this.setJMenuBar(menubar);
		    JMenu fil = new JMenu("File");
		    menubar.add(fil);
		    fil.add(new ExitAction());
			
		    content.add(menubar , BorderLayout.NORTH);
			content.add(scrollPane, BorderLayout.CENTER);
			content.add( textField , BorderLayout.SOUTH );
			
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500, 500);
			frame.setVisible(true);
			BufferedReader bufferedReader = new BufferedReader( new FileReader("TextDoc.txt") );
			StringBuilder readingString = new StringBuilder();
			String line = bufferedReader.readLine();
			while( line != null )
			{
				readingString.append(line + "\n");
				line = bufferedReader.readLine();
			}
			String string = readingString.toString();
			ClientRequest.clientShadow = string;
			textArea.setText(string);
			
			document.addDocumentListener(new MyListener());
			
			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public class ExitAction extends AbstractAction 
	{
	    public ExitAction() 
	    {
	    	super("Exit"); 
	    }
	    public void actionPerformed(ActionEvent ev) 
	    {
	    	try
	    	{
	    		Socket socket = new Socket( ClientRequest.cacheIP , 10002 );
	    		DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
	    		dataOutputStream.writeBytes("delete"+"\n");
	    		socket.close();
	    		System.exit(0);
	    	}
	    	catch( Exception e )
	    	{
	    		
	    	}
	    }
	}
	
	class MyListener implements DocumentListener 
	{
		
		public void changedUpdate(DocumentEvent documentEvent) 
		{
			printInfo(documentEvent);
		}

		public void insertUpdate(DocumentEvent documentEvent) 
		{
		    printInfo(documentEvent);
		}

		public void removeUpdate(DocumentEvent documentEvent) 
		{
			printInfo(documentEvent);
		}

		public void printInfo(DocumentEvent documentEvent) 
		{
			DocumentEvent.EventType type = documentEvent.getType();
		    Document documentSource = documentEvent.getDocument();
		    Element rootElement = documentSource.getDefaultRootElement();
		    DocumentEvent.ElementChange change = documentEvent
		        .getChange(rootElement);
		    try 
		    {
				FileWriter write = new FileWriter(file);
				textArea.write(write);
				if( count == 0 )
				{
					clientDifferentialSyncronisation = new ClientDifferentialSyncronisation(THIS);
					clientDifferentialSyncronisation.start();
					count++;
				}
			}
		    catch (IOException e) 
		    {
				e.printStackTrace();
			}
		 }
	};
	
}
