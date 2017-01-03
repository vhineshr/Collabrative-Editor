package diff_match_patch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class UI extends JFrame
{	
	
	JFrame frame = new JFrame("TextEditor");
    Container content = frame.getContentPane();
    JTextArea textArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(textArea);
    final Document document = textArea.getDocument();
    File file;
    UI uiObject = this;
	
	UI(  )
	{
		try
		{
			file = new File("TextDoc.txt");
			
			content.add(scrollPane, BorderLayout.CENTER);
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
			textArea.setText(string);
			MainTextServer.MainServerShadow = string;
			
			document.addDocumentListener(new MyListener( ));
			
		}
		catch( Exception e )
		{
			e.printStackTrace();
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
			}
		    catch (IOException e) 
		    {
				e.printStackTrace();
			}
		 }
	};
	
}