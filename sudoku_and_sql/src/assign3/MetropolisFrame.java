package assign3;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.junit.internal.ExactComparisonCriteria;

import java.awt.*;
import java.awt.event.*;

import java.sql.*;
import java.util.ArrayList;

public class MetropolisFrame extends JFrame{
		
	// Components of GUI / model-view
	// Panels and table
	JPanel content;
	JPanel topPanel;
	JPanel textPanel;
	JPanel buttonPanel;
	JPanel searchPanel; 
	
	JTable table; // the model
	JScrollPane scrollPane;
	MetropolisTableModel model;
	
	// Fields
	JTextField fieldMetropolis;
	JTextField fieldContinent;
	JTextField fieldPopulation;
	
	// Labels
	JLabel labelMetropolis;
	JLabel labelContinent;
	JLabel labelPopulation;
	
	// Buttons
	JButton addButton;
	JButton searchButton;
	
	// Drop-downs
	JComboBox popSizeCombo;
	JComboBox matchCombo;
	
	public MetropolisFrame() {
		super("Metropolis Viewer");
		setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Main content panel layout 
		content = new JPanel();
		content.setLayout(new BorderLayout(4, 4));
		
		// Top panel, text fields and search options
		topPanel  = new JPanel();     
		
		// Text panel
		textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		
		labelMetropolis = new JLabel("Metropolis:");
		labelContinent  = new JLabel("Continent:");
		labelPopulation = new JLabel("Population:");
		
		fieldMetropolis = new JTextField(10);
		fieldContinent  = new JTextField(10);
		fieldPopulation = new JTextField(10);
			
		textPanel.add(labelMetropolis);
		textPanel.add(fieldMetropolis);
		textPanel.add(labelContinent);
		textPanel.add(fieldContinent);
		textPanel.add(labelPopulation);
		textPanel.add(fieldPopulation);
		
		topPanel.add(textPanel);
		
		// Search panel
		searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBorder(new TitledBorder("Search Options"));
		
		// nb: update model.constructQuery() if these change!
		popSizeCombo = new JComboBox
				(new String[]{"Population >", "Population <="});
		matchCombo   = new JComboBox 
				(new String[]{"Exact match", "Contains"});
		  
		searchPanel.add(popSizeCombo);
		searchPanel.add(matchCombo);
		
		topPanel.add(searchPanel);
		content.add(topPanel, BorderLayout.NORTH);

		// Buttons
		buttonPanel  = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		addButton    = new JButton("Add");
		searchButton = new JButton("Search"); 
		
		// Call the appropriate methods in the MetropolisTableModel
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.add(e);
			}
		});
		
		searchButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.search(e);
			}
		});

		buttonPanel.add(addButton);
		buttonPanel.add(searchButton);
		content.add(buttonPanel, BorderLayout.EAST);
		
		// Table model and view
		model = new MetropolisTableModel(this);
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scrollPane = new JScrollPane(table); 
		
		content.add(scrollPane, BorderLayout.CENTER);
		
		// Add everything to the frame, pack, make visible
		add(content);
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		final MetropolisFrame frame = new MetropolisFrame();
		
		// Added to kill connections upon window close 
		// (source: stackoverflow --> oracle shutdown hook doc)
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            frame.model.destroyConnection();
	        }
	    }, "shutdown-thread"));
	}
}
