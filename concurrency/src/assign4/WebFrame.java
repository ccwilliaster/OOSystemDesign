package assign4;

import java.awt.*; 
import javax.swing.*; 
import javax.swing.table.DefaultTableModel;
import javax.swing.text.TableView;

import com.sun.java.swing.plaf.nimbus.ProgressBarPainter;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebFrame class that creates a GUI for downloading the contents of URLs 
 * specified in an input file. Shows the progress of downloads and a summary 
 * after the download attempt. Controls are provided for specifying the number 
 * of threads forked off to download URL content.
 */
public class WebFrame extends JFrame {

	private static final String urlFile = "links.txt";
	
	private static final int READY   = 0; // internal state
	private static final int RUNNING = 1;
	
	// GUI components
	JPanel panel;
	JTable table;
	DefaultTableModel model;
	
	JButton buttonOneFetch;
	JButton buttonMultiFetch;
	JButton buttonStop;
	
	JLabel labelRunning;
	JLabel labelCompleted;
	JLabel labelElapsed;
	
	JTextField fieldThreadNum;
	JProgressBar progress;
	
	// Internal variables
	Integer numProcesses;
	Integer numCompleted;
	Integer numThreads;
	Semaphore semaphore;
	Launcher launcher;
	long startTime;
	int state;
	
	public WebFrame() {
		super("WebLoader");
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// internal variables
		numThreads   = 0;
		numProcesses = 0;
		numCompleted = 0;
		launcher  = null;
		semaphore = null;
		state     = READY;
		
		// GUI
		// set up the table - view
		panel = new JPanel();
		model = new DefaultTableModel(new String[] { "url", "status"}, 0) {
			@Override // no editing URLS
			public boolean isCellEditable(int row, int col) { return false; }
		}; 
		table = new JTable(model); 
		table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		
		JScrollPane scrollpane = new JScrollPane(table); 
		scrollpane.setPreferredSize(new Dimension(600,300)); 
		panel.add(scrollpane);
		
		readUrlFile(); // populates model
		
		// Buttons, labels, progress bar
		buttonOneFetch   = new JButton("Single Thread Fetch");
		buttonMultiFetch = new JButton("Concurrent Fetch");
		buttonStop       = new JButton("Stop");
		
		fieldThreadNum = new JTextField();
		fieldThreadNum.setMaximumSize(new Dimension(50,0));
		
		labelRunning   = new JLabel("Running:");
		labelCompleted = new JLabel("Completed:");
		labelElapsed   = new JLabel("Elapsed:");
		
		progress = new JProgressBar();
		
		// Add button listeners 
		buttonOneFetch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				numThreads = 1;
				setRunState();
			}
		});
		
		buttonMultiFetch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						numThreads = Integer.parseInt( fieldThreadNum.getText() );
						if (numThreads < 1) {
							throw new RuntimeException("Invalid number of threads");
						}
						setRunState();
					}
				});
			}
		});
		
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (launcher != null) {
					launcher.interrupt();
					launcher.interruptWorkers();
				}
			}
		});
		
		add(panel);
		add(buttonOneFetch);
		add(buttonMultiFetch);
		add(fieldThreadNum);
		add(labelRunning);
		add(labelCompleted);
		add(labelElapsed);
		add(progress);
		add(buttonStop);
		
		toggleButtonState();
		pack();
		setVisible(true);
	}

	/**
	 * Reads the urlFile urls into the table model and sets all 
	 * url statuses to the empty string.
	 */
	public void readUrlFile() { 
		String line = null;

		try { // Create one row per row file
			BufferedReader reader = 
					new BufferedReader( new FileReader(urlFile) );
					
			String[] row;
			while ((line = reader.readLine()) != null) { 
				row = new String[2];
				row[0] = line;
				row[1] = ""; // no status in 2nd col to start
				model.addRow(row);
			}
			reader.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Resets the progress bar to 0, and updates its maximum value to
	 * the current number of rows in the table model.
	 */
	private void resetProgressBar() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progress.setMaximum( model.getRowCount() );
				progress.setValue(0);
			}
		});
	}
	
	/**
	 * This function controls the logic for setting up the 'running'
	 * state. It explicitly sets the frame state to RUNNING, toggles
	 * buttons appropriately, resets the status labels and progress bar,
	 * initializes a new Semaphore based on the number of threads requested
	 * and calls the Launcher initializer
	 */
	private void setRunState() { 
		// Refresh / reset ivars for new run
		state = RUNNING;
		numCompleted = 0;
		toggleButtonState();
		resetProgressBar();
		resetStatusLabels();
		
		// Fork off launcher thread
		semaphore = new Semaphore(numThreads);
		if (launcher != null) launcher = null;
 		launcher = new Launcher();
 		launcher.start();
 		
 		// Time it
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Updates ivars and the GUI for transition into the READY state. THis
	 * entails re-setting the progress bar, updating the elapsed time label,
	 * and toggling button enabled-ness.
	 */
	private void setReadyState() {
		state = READY;
		toggleButtonState();
		resetProgressBar();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				long elapsed = (System.currentTimeMillis() - startTime) / 1000;
				WebFrame.this.labelElapsed.setText("Elapsed: " + elapsed + "s");
			}
		});
	}
	
	/**
	 * Resets JTable status labels, as well as the WebFrame status labels
	 */
	private void resetStatusLabels() {
		// Clear table model statuses
		for (int row = 0; row < model.getRowCount(); row++) {
			model.setValueAt("", row, 1);
		}
		
		// Request to reset GUI status labels
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				labelRunning.setText("Running:");
				labelCompleted.setText("Completed:");
				labelElapsed.setText("Elapsed:");
			}
		});
	}
	
	/**
	 * Toggles which buttons in the WebFrame are enabled, based on the current
	 * value of the state internal variable.
	 */
	private void toggleButtonState() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (state == RUNNING) {
					buttonOneFetch.setEnabled(false);
					buttonMultiFetch.setEnabled(false);
					buttonStop.setEnabled(true);
				} else {
					buttonOneFetch.setEnabled(true);
					buttonMultiFetch.setEnabled(true);
					buttonStop.setEnabled(false);
				}
		}});	
	}
	
	/**
	 * Updates the numProcesses ivar in a thread safe manner and makes a
	 * call to the Swing thread to update the running label. If numProcesses
	 * is updated to a new value of zero, the setReadyState() method is called.
	 * @param delta value to add to the current value
	 */
	synchronized protected void updateRunning(int delta) {
		synchronized(numProcesses) {
			numProcesses += delta;
			if (numProcesses == 0) setReadyState();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				labelRunning.setText("Running: " + numProcesses);
			}
		});
	}
	
	/**
	 * Updates the numCompleted ivar in a thread safe manner and makes a
	 * call to the Swing thread to update the completed label
	 * @param delta value to add to the current value
	 */
	protected void updateCompleted(int delta) {
		synchronized(numCompleted) {
			numCompleted += delta;
			
			if (numCompleted == model.getRowCount() &&
					launcher != null) {
				launcher.interrupt(); // signal to the launcher we're done
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				labelCompleted.setText("Completed: " + numCompleted);
			}
		});
	}
	
	/**
	 * If the program is in the READY state, increments the progress bar 
	 * by 1, in a thread-safe manner.
	 */
	protected void incrementProgress() {
		if (state != READY) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progress.setValue( progress.getValue() + 1);
				}
			});
		}
	}
	
	/**
	 * The launcher thread is responsible for launching WebWorkers to fetch
	 * the url contents of urls in the model view. Manages logic for
	 * initializing a specified number of Worker threads at a given time.
	 */
	private class Launcher extends Thread {
		ArrayList<WebWorker> workers;

		@Override
		public void run() {
			updateRunning(+1);
			launchWorkers();
		}
		
		/**
		 * Causes the launcher to exit, calling to decrement numProccesses.
		 * Allows proper exit from proper finish or any interruption.
		 */
		private void exit() {
			updateRunning(-1);
		}
		
		/**
		 * Initializes a WebWorker for each url in the table model and stores
		 * references in the workers array. The number of workers initialized 
		 * at any given time is limited by whatever the value of the WebFrame 
		 * semaphore is when this method is invoked. Listens for interrupts 
		 * throughout call.
		 * Synchronized for compatibility with interruptWorkers()
		 */
		synchronized private void launchWorkers() {
			workers = new ArrayList<WebWorker>( model.getRowCount() );
			WebWorker currWorker;
			String urlString;
			
			for (int url = 0; url < model.getRowCount(); url++) {
				
				if (isInterrupted()) { 
					exit(); 
					return; 
				}

				urlString  = (String) model.getValueAt(url, 0);
				currWorker = new WebWorker(urlString, url, WebFrame.this);
				workers.add(currWorker);
				currWorker.start();
				
				try { // Pause here if there are no more semaphores
					semaphore.acquire(); 
				} catch (InterruptedException e) { 
					exit();
					return; 
				}
				semaphore.release(); // release for next Worker
			}
			exit();
		}
		
		/**
		 * Interrupts all workers in the workers array.
		 */
		synchronized private void interruptWorkers() {
			for (WebWorker worker : workers) {
				if (worker != null) worker.interrupt();
			}
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ignored) {}
				
				WebFrame frame = new WebFrame();
			}	
		});
			
	}

}
