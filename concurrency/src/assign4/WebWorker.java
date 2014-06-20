package assign4;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class WebWorker extends Thread {
	private static final int PAUSE     = 100; // ms for Worker pauses
	static final int SUCCESS   = 0;
	static final int FAIL      = 1;
	static final int INTERRUPT = 2;
	
	StringBuilder contents;
	WebFrame webframe;
	String urlString;
	int rowToModify;
	long elapsed;
	long start;
	
	public WebWorker(String urlString, int rowToModify, WebFrame webframe) {
		this.urlString   = urlString;
		this.rowToModify = rowToModify;
		this.webframe    = webframe;
		contents = null;
	}
	
	@Override
	public void run() {
		try {
			webframe.semaphore.acquire();
		} catch (InterruptedException e) { 
			exit(INTERRUPT, 0);
			return; 
		}

		webframe.updateRunning(+1);
		start = System.currentTimeMillis();
		download();
	}
	
	/**
	 * Handles three types of exits for the WebWorker: success, failure,
	 * or interruption. Handles logic for all cases, and signals back to
	 * the WebFrame appropriate changes to JLabels/corresponding ivars
	 * @param status constant indicating type of exit
	 * @param deltaRunning value to add to the current number of Worker threads
	 * upon exit 
	 */
	void exit(int status, int deltaRunning) {
		if (status == SUCCESS) {
			setSuccessStatus();
		} else if (status == FAIL) {
			webframe.model.setValueAt("err", rowToModify, 1);
		} else { // interrupt
			webframe.model.setValueAt("interrupted", rowToModify, 1);
		}
		
		// Update webframe ivars appropriately, and release semaphore
		webframe.updateRunning(deltaRunning);
		webframe.updateCompleted(+1);
		webframe.incrementProgress();
		webframe.semaphore.release();
	}
	
	
	private void setSuccessStatus() {
		
		StringBuilder status = new StringBuilder();
		
		// Add date
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String formattedDate = dateFormat.format( new Date() );
		
		status.append( formattedDate );
		
		// Add elapsed time
		status.append("   " + elapsed + "ms");
		
		// Add file size
		int numChars = contents.length();
		int numBytes = numChars * 1;
		status.append("   " + numBytes + " bytes");
		
		webframe.model.setValueAt(status.toString(), rowToModify, 1);
	}
	
	
	
	/**
	 * Attempts do download url contents from the web server specified
	 * by the Worker's urlstring field. Contents are added to the contents
	 * instance variable. nb: listens for interrupt messages throughout read.
	 * Different exit statuses are handled by the exit method.
	 */
	private void download() {
 		InputStream input = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				contents.append(array, 0, len);
				Thread.sleep(PAUSE);
			}
			// Successful download if we're here
			elapsed = System.currentTimeMillis() - start;
			exit(SUCCESS, -1);
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException exception) { exit(FAIL, -1); } 
		catch(InterruptedException exception)  { exit(INTERRUPT, -1); }
		catch(IOException exception)           { exit(FAIL, -1); }
		
		// "finally" clause, to close the input stream in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {}
		}
	}	
}
