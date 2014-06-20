package assign4;

import java.awt.*; 
import javax.swing.*; 
import java.awt.event.*;

/**
 * JCount class which forks off worker threads to count up to integer values
 * specified in its text field. These threads are controlled with start
 * and stop buttons on the panel, and display some intermediate values
 * on the panel label as they count.
 */
public class JCount extends JPanel {
	private static final int NUM_JCOUNTS = 4;
	private static final int DEFAULT_CT = 100000000;  // default to count to
	private static final int UPDATE_INTERVAL = 10000; // when to update count
	private static final int PAUSE_TIME = 100; // ms to pause when updating count
	
	// Static
	private static JFrame frame; // frame to house JCount objects
	
	// Instance
	private JTextField field;
	private JLabel     label;
	private JButton    startButton;
	private JButton    stopButton;
	private int        currVal;
	private Worker     worker;
	
	public JCount() {
		super();
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		
		currVal = 0;
		worker  = null;
		
		field = new JTextField( DEFAULT_CT + "" );
		label = new JLabel(currVal + "");
		
		startButton = new JButton("Start");
		stopButton  = new JButton("Stop");
		
		// Start interrupts existing workers, starts a new one at 0
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (worker != null) worker.interrupt();
				
				worker = new Worker( field.getText() );
				worker.start();
			}
		});
		
		// Stop interrupts the existing worker, if it exists
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.interrupt();
					worker = null;
				}
			}
		});
				
		add(field);
		add(label);
		add(startButton);
		add(stopButton);
		add(Box.createRigidArea( new Dimension(0,40) ));
	}

	/**
	 * Worker thread class responsible for counting and communicating
	 * with the swing thread to update a JCounts label.
	 */
	private class Worker extends Thread {
		int target;
		
		public Worker(String target) {
			currVal = 0; // reset to 0
			try {
				this.target = Integer.parseInt( target ); // new target
			} catch (Exception e) {
				throw new RuntimeException("Invalid integer '" + target + "'");
			}
		}
		
		/**
		 * Tells a Worker to begin counting from its JCount's currVal to 
		 * its target. Pauses and makes request to Swing thread to update
		 * the label at defined intervals. Listens for interrupt at every
		 * increment
		 */
		@Override
		public void run() {
			for (int i = currVal; i <= target; i++) {
				// Stop if we've been interrupted
				if ( isInterrupted() ) break;
				
				// Pause and send request to update label at defined interval
				if ( (currVal % UPDATE_INTERVAL) == 0 ) {
					try {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								label.setText( currVal + "");
							}
						});
						Thread.sleep(PAUSE_TIME);
					} catch (InterruptedException e) { break; }
				}
				currVal++;
			}
		}
	}
	
	/**
	 * Sets up the main GUI JFrame with NUM_JCOUNTS JCount objects. No
	 * counting begins until a start button is pressed.
	 */
	private static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}
		
		frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// Initialize all JCount objects and add each to frame
		JCount currJC;
		for (int i = 0; i < NUM_JCOUNTS; i++) {
			currJC = new JCount();
			frame.add(currJC);
		}
		
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
