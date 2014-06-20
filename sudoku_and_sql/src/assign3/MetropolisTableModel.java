package assign3;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Custom TableModel class which manages the connection with the SQL 
 * database and controls the logic for updating the model in response
 * to GUI events.
 */
public class MetropolisTableModel extends AbstractTableModel {		
	
	// Database info is pulled from MyDBInfo object
	static final String account  = MyDBInfo.MYSQL_USERNAME;
	static final String password = MyDBInfo.MYSQL_PASSWORD;
	static final String server   = MyDBInfo.MYSQL_DATABASE_SERVER;
	static final String database = MyDBInfo.MYSQL_DATABASE_NAME;
	
	private MetropolisFrame view;
	private Connection connection;
	private Statement  sqlStmt;
	private ResultSet  sqlResult;
	private ArrayList<ArrayList<Object>> localData;	
	private ArrayList<String> colNames;
	
	public MetropolisTableModel (MetropolisFrame view) {
		this.view = view;
		localData = new ArrayList<ArrayList<Object>>();
		colNames  = new ArrayList<String>() {{
			add("Metropolis"); add("Continent"); add("Population");
		}};
		
		makeConnection();
	}
	
	/**
	 * Attempts to make a connection to a MySQL database based on 
	 * the database, server, user, and password fields of the MyDBInfo object
	 */
	public void makeConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection
						 ( "jdbc:mysql://" + server, account ,password);
			
			sqlStmt = connection.createStatement();
			sqlStmt.executeQuery("USE " + database);
			//System.out.println("Made connection");
		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) { e.printStackTrace(); }
	}
	/**
	 * Closes the TableModel's connection to the MySQL database. Removes
	 * the need for establishing connections during each method call and 
	 * allows connections to easily be closed when appropriate.
	 */
	public void destroyConnection() {
		try {
			connection.close();
			//System.out.println("Connection closed");
			
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	/**
	 * Attempts to insert the current contents of the MetropolisFrame text
	 * fields into the connected SQL database. If a new entry is made,
	 * the table view is updated to show only the newly entered row.
	 * If fields are empty when this method is called, nothing happens. 
	 * Intended to be the EventListener for the GUI add button.
	 * @param event which triggered a call to this method (not used)
	 */
	public void add(ActionEvent event) {
		try {
			// Construct and execute insert String 
			String insertStr = constructInsertStr();
			if (insertStr == "") return; // no update if fields are empty
			sqlStmt.executeUpdate(insertStr);
			
			// Update view to show only newly added row
			makeLocalDataMatchFields();
			fireTableDataChanged();		
		} catch (SQLException e) { e.printStackTrace(); }
	}

	/**
	 * Constructs a SQL string for inserting the contents of of the 
	 * MetropolisFrame's text fields into the metropolises database. 
	 * @return SQL string for inserting current text field text into the db
	 */
	private String constructInsertStr() {
		String metropolis = view.fieldMetropolis.getText(); 
		String continent  = view.fieldContinent.getText();
		String population = view.fieldPopulation.getText();

		// Return if there is no content
		if (metropolis.length() == 0 &&
			continent.length()  == 0 &&
			population.length() == 0) return "";
		
		// Construct query as two halves, adding field/values if populated
		String insertFields = "INSERT INTO metropolises (";
		String insertVals   = "VALUES (";
		
		if (metropolis.length() > 0) {
			insertFields += "metropolis";
			insertVals   += "'" + metropolis + "'";
		}
		if (continent.length() > 0) {
			insertFields += (metropolis.length() > 0) 
					        ? ", continent" : "continent";
			insertVals   += (metropolis.length() > 0) 
							? ", '" + continent + "'" : "'" + continent + "'";
		}
		if (population.length() > 0) {
			insertFields += (metropolis.length() > 0 || continent.length() > 0)
							? ", population" : "population";
			insertVals   += (metropolis.length() > 0 || continent.length() > 0)
							? ", " + population : population;
		}	
		String insertStr = insertFields + ") " + insertVals + ")";
		//System.out.println(insertStr);
		return insertStr;
	}
	
	/**
	 * Updates the localData ivar to contain only the current contents of 
	 * the MetropolisFrame text fields. 
	 */
	private void makeLocalDataMatchFields() {
		String metropolis = view.fieldMetropolis.getText(); 
		String continent  = view.fieldContinent.getText();
		String population = view.fieldPopulation.getText();
		
		ArrayList<Object> currFields = new ArrayList<Object>();
		currFields.add(metropolis);
		currFields.add(continent);
		currFields.add(population);
		
		localData.clear();
		localData.add(currFields);
		return;
	}
	
	/**
	 * Constructs and executes a SQL query based on the current state of 
	 * the MetropolisFrame text fields and combo boxes. Intended to be the
	 * EventListener for the GUI search button.
	 * @param event which triggered a call to this method (not used)
	 */
	public void search(ActionEvent event) {
		// Construct a query, execute on db, parse the results, update view
		try {
			String queryStr = constructQuery();
			
			sqlResult = sqlStmt.executeQuery(queryStr);
			parseResultSet();
			fireTableDataChanged();
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
	
	/**
	 * This method constructs a full SQL query string based on the 
	 * current state of the MetropolisFrame text fields and combo boxes
	 * @return SQL query
	 */
	private String constructQuery() { 
		String metropolis, continent, population, query;
		metropolis = continent = population = "";
		
		// Fetch states of combo boxes, which affect query logic
		boolean exact   = 
				view.matchCombo.getSelectedItem() == "Exact match";
		boolean greater = 
				view.popSizeCombo.getSelectedItem() == "Population >";

		// Get specific text from 
		if (view.fieldMetropolis.getText().length() > 0) {
			metropolis = view.fieldMetropolis.getText();
			if (!exact) metropolis = makeGeneral(metropolis);
		} 
		if (view.fieldContinent.getText().length() > 0) {
			continent  = view.fieldContinent.getText();
			if (!exact) continent = makeGeneral(continent);
		} 
		if (view.fieldPopulation.getText().length() > 0) {
			population =  (greater) ? "> " : "<= ";
			population += view.fieldPopulation.getText();
		}
		
		// Piece together the query
		query = "SELECT * FROM metropolises";
		if (metropolis != "") {
			query += " WHERE metropolis LIKE '" + metropolis + "'";
		}
		if (continent != "") {
			query += (metropolis != "") ? " AND " : " WHERE ";
			query += "continent LIKE '" + continent + "'";
		}
		if (population != "") {
			query += (metropolis != "" || 
					  continent  != "" ) ? " AND " : " WHERE ";
			query += "population " + population;
		}	
		//System.out.println(query);
		return query; 
	}
	
	/**
	 * Adds SQL wildcard '%' characters on both ends of the input String
	 * for partial matching in SQL queries
	 * @param partialString
	 * @return '%partialString%'
	 */
	private String makeGeneral(String partialString) {
		return "%" + partialString + "%";
	}
	
	/**
	 * Parses the current SQL result set and adds query results
	 * to the localData array
	 */
	private void parseResultSet() {
		// each row is represented as an array of objects
		localData = new ArrayList<ArrayList<Object>>(); 
		try {
			while(sqlResult.next()) {
				ArrayList<Object> currRow = new ArrayList<Object>();
				for (int i = 1; i <= colNames.size(); i++) { // always this length
					currRow.add( sqlResult.getObject(i) );
				}
				localData.add(currRow);
			}
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	/**
	 * Returns the column name for the specified column index. In this
	 * case, names are pulled from class ivar.
	 */
	@Override
	public String getColumnName(int col) {
		return colNames.get(col);
	}
	
	/**
	 * Returns the number of rows that can be shown in the JTable, given 
	 * unlimited space
	 */
	@Override
	public int getRowCount() {
		return localData.size();
	}
	/**
	 * Returns the number of columns in the column model.
	 */
	@Override
	public int getColumnCount() {
		return colNames.size();
	}
	/**
	 * Returns the cell value at row and column.
	 */
	@Override
	public Object getValueAt(int row, int col) {
		return localData.get(row).get(col);
	}
	
}
