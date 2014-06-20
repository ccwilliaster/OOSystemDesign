package assign3;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.*;
import java.awt.event.*;


 public class SudokuFrame extends JFrame {
	
	 protected JPanel panel;
	 protected JTextArea sourceArea;
	 protected Document sourceDoc;
	 
	 protected JTextArea solutionArea;
	 
	 protected JPanel controlPanel;
	 protected JButton checkButton;
	 protected JCheckBox autoCheckBox;
	 
	 protected Sudoku sudoku;
	 
	public SudokuFrame() {
		super("Sudoku Solver");
		setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Construct a BoarderLayout panel for Frame components 
		panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 4));
		
		// Create text area for Puzzle input
		sourceArea = new JTextArea(15, 20);
		sourceArea.setBorder(new TitledBorder("Puzzle"));
		sourceDoc = sourceArea.getDocument(); // for updates below
		
		// Add event listener for source changes
		sourceDoc.addDocumentListener( new SudokuDocListener() );
		panel.add(sourceArea, BorderLayout.CENTER);
		
		// Create another area to display any solutions found
		solutionArea = new JTextArea(15, 20);
		solutionArea.setBorder(new TitledBorder("Solution"));
		solutionArea.setEditable(false); // no editing!
		
		panel.add(solutionArea, BorderLayout.EAST);
		
		// Add a control button at the bottom
		controlPanel = new JPanel();
		checkButton  = new JButton("Check");
		checkButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e){
				runSudoku();
			}
		});
		controlPanel.add(checkButton);
		
		// And finally a control check box
		autoCheckBox = new JCheckBox("Auto Check");
		autoCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if ( autoCheckBox.isSelected() ) runSudoku();
			}
		});
		autoCheckBox.setSelected(true);
		controlPanel.add(autoCheckBox);
		
		panel.add(controlPanel, BorderLayout.SOUTH);
		
		add(panel);
		pack();
		setVisible(true);
	}
	
	/**
	 * Attempts to run Sudoku using the input text currently in the source 
	 * input area. If successful, displays the solution in the
	 * solution area, along with the number of solutions found and time to 
	 * solve. "Parsing problem" is displayed if any exceptions occur or no
	 * solutions are found.
	 */
	private void runSudoku() {
		//System.out.println("Running Sudoku"); // check controls work correctly
		
		try { // Try running Sudoku with the current source text
			sudoku = new Sudoku(sourceDoc.getText(0, sourceDoc.getLength()));
			int result = sudoku.solve();
			
			// If successful, print results to screen
			if (result > 0) {
				solutionArea.setText(sudoku.getSolutionText() +
									 "\nsolutions: " + result + "\n" +
									 "elapsed: " + sudoku.getElapsed() + "ms");
			} else {
				solutionArea.setText("Parsing problem");
			}
		} catch (RuntimeException e) {
			solutionArea.setText("Parsing problem");
		} catch (BadLocationException ignored) { System.out.println("Bad doc"); }
		
	}
	
	/**
	 * Document Listener for SudokuFrame. If the autoCheckBox is selected and
	 * changes are made to the source document, an attempt to run Sudoku is 
	 * made.
	 */
	private class SudokuDocListener implements DocumentListener {
		@Override
	    public void insertUpdate(DocumentEvent e) {
	        if ( autoCheckBox.isSelected() ) runSudoku();
	    }
	    @Override
	    public void removeUpdate(DocumentEvent e) {
	    	if ( autoCheckBox.isSelected() ) runSudoku();;
	    }
	    @Override
	    public void changedUpdate(DocumentEvent e) {
	    	if ( autoCheckBox.isSelected() ) runSudoku();
	    }
	}
	
	public static void main(String[] args) {
		// GUI Look And Feel
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		SudokuFrame frame = new SudokuFrame();
	}

}
