package assign3;

import java.util.*;

/*
 * Encapsulates a Sudoku grid to be solved.
 * CS108 Stanford.
 */
public class Sudoku {
	// Provided grid data for main/testing
	// The instance variable strategy is up to you.
	
	// Provided easy 1 6 grid
	// (can paste this text into the GUI too)
	public static final int[][] easyGrid = Sudoku.stringsToGrid(
	"1 6 4 0 0 0 0 0 2",
	"2 0 0 4 0 3 9 1 0",
	"0 0 5 0 8 0 4 0 7",
	"0 9 0 0 0 6 5 0 0",
	"5 0 0 1 0 2 0 0 8",
	"0 0 8 9 0 0 0 3 0",
	"8 0 9 0 4 0 2 0 0",
	"0 7 3 5 0 9 0 0 1",
	"4 0 0 0 0 0 6 7 9");
	
	
	// Provided medium 5 3 grid
	public static final int[][] mediumGrid = Sudoku.stringsToGrid(
	 "530070000",
	 "600195000",
	 "098000060",
	 "800060003",
	 "400803001",
	 "700020006",
	 "060000280",
	 "000419005",
	 "000080079");
	
	// Provided hard 3 7 grid
	// 1 solution this way, 6 solutions if the 7 is changed to 0
	public static final int[][] hardGrid = Sudoku.stringsToGrid(
	"3 7 0 0 0 0 0 8 0",
	"0 0 1 0 9 3 0 0 0",
	"0 4 0 7 8 0 0 0 3",
	"0 9 3 8 0 0 0 1 2",
	"0 0 0 0 4 0 0 0 0",
	"5 2 0 0 0 6 7 9 0",
	"6 0 0 0 2 1 0 4 0",
	"0 0 0 5 3 0 9 0 0",
	"0 3 0 0 0 0 0 5 1");
	
	
	public static final int SIZE = 9;  // size of the whole 9x9 puzzle
	public static final int PART = 3;  // size of each 3x3 part
	public static final int MAX_SOLUTIONS = 100;
	
	// Provided various static utility methods to
	// convert data formats to int[][] grid.
	/**
	 * Returns a 2-d grid parsed from strings, one string per row.
	 * The "..." is a Java 5 feature that essentially
	 * makes "rows" a String[] array.
	 * (provided utility)
	 * @param rows array of row strings
	 * @return grid
	 */
	public static int[][] stringsToGrid(String... rows) {
		int[][] result = new int[rows.length][];
		for (int row = 0; row<rows.length; row++) {
			result[row] = stringToInts(rows[row]);
		}
		return result;
	}
	
	/**
	 * Given a single string containing 81 numbers, returns a 9x9 grid.
	 * Skips all the non-numbers in the text.
	 * (provided utility)
	 * @param text string of 81 numbers
	 * @return grid
	 */
	public static int[][] textToGrid(String text) {
		int[] nums = stringToInts(text);
		if (nums.length != SIZE*SIZE) {
			throw new RuntimeException("Needed 81 numbers, but got:" + nums.length);
		}
		
		int[][] result = new int[SIZE][SIZE];
		int count = 0;
		for (int row = 0; row<SIZE; row++) {
			for (int col=0; col<SIZE; col++) {
				result[row][col] = nums[count];
				count++;
			}
		}
		return result;
	}
	
	/**
	 * Given a string containing digits, like "1 23 4",
	 * returns an int[] of those digits {1 2 3 4}.
	 * (provided utility)
	 * @param string string containing ints
	 * @return array of ints
	 */
	public static int[] stringToInts(String string) {
		int[] a = new int[string.length()]; // max len, not guaranteed all ints
		int found = 0;
		for (int i=0; i<string.length(); i++) {
			if (Character.isDigit(string.charAt(i))) {
				a[found] = Integer.parseInt(string.substring(i, i+1));
				found++;
			}
		}
		int[] result = new int[found];
		System.arraycopy(a, 0, result, 0, found);
		return result;
	}

	// ------------------------------------------------------------------------
	// Start my implementation
	// ------------------------------------------------------------------------
	// Variables
	private Spot[][] grid; // internal representation of the board
	
	// if Spots are checked before assignment
	private static final boolean CHECK_ASSIGNMENTS = false; 
	
	// Vars set by the solve() method
	private List<String> solutions = new LinkedList<String>(); 
	private long elapsedTime = 0;
	// ------------------------------------------------------------------------
	
	/**
	 * Sets up board internally based on the given ints.
	 */
	public Sudoku(int[][] ints) {
		grid = new Spot[SIZE][SIZE];
		
		// Initialize board by creating a Spot object for each grid position
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				grid[row][col] = new Spot(row, col, ints[row][col]);
			}
		}
		
	}
	
	/**
	 * Alternate constructor from a string containing 81 ints
	 * @param text String containing 81 ints
	 */
	public Sudoku(String text) {
		this( textToGrid(text) );
	}
	
	/**
	 * Constructs a String representation of the Sudoku Board
	 */
	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");
		StringBuilder board = new StringBuilder();
		
		for (int row = 0; row < SIZE; row++) {
			if (row > 0) board.append( newLine );
			for (int col = 0; col < SIZE; col++) {
				board.append(" " + grid[row][col].value);
			}
		}
		return board.toString();
	}
	
	/**
	 * Generates a sorted list of Spots based on the number of possible values
	 * that can be assigned to them given the current Sudoku grid state. Spots
	 * which have non-zero values are filtered from the list.
	 * @return sorted list of Spots which currently have non-zero values
	 */
	private List<Spot> getSortedSpots() {
		List<Spot> sortedSpots = new ArrayList<Spot>(SIZE*SIZE);
		
		Spot currSpot; // Add Spots with non-zero values to the list
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				currSpot = grid[row][col];
				if (currSpot.value == 0) sortedSpots.add(currSpot);
			} 
		}
		Collections.sort( sortedSpots );
		return sortedSpots;
	}

	/**
	 * Solves the puzzle, invoking the underlying recursive search.
	 */
	public int solve() {
		// Keep track of the time to solve Sudoku
		long start = System.currentTimeMillis();
		
		// First sort Spots based on their value constraints
		List<Spot> sortedSpots = getSortedSpots();
		//System.out.println( sortedSpots.toString() );
		
		// Invoke the recursive solve method, which adds solutions as it goes
		recSolve(sortedSpots, 0);
		
		elapsedTime = System.currentTimeMillis() - start;
		return solutions.size(); 
	}

	/**
	 * Recurssive solution for solving the Sudoku board
	 * @param sortedSpots 
	 * @param currIdx current index in the sortedSpots list
	 */
	public void recSolve(List<Spot> sortedSpots, int currIdx) {
		// Base case: return if we have already found MAX_SOLUTIONS solutions
		if (solutions.size() >= MAX_SOLUTIONS) return;
		
		// Fetch possible values for the current Spot, to recurse on
		Spot currSpot = sortedSpots.get(currIdx);		
		HashSet<Integer> possibleValues = currSpot.getPossibleVals();

		for (int value : possibleValues) {
			currSpot.set(value);
			// Base case: if we are the last spot in the list, add solution!
			if (currIdx >= sortedSpots.size() - 1) {
				solutions.add( this.toString() );
			} 
			// Otherwise recurse on the subsequent Spots
			else { 
				recSolve(sortedSpots, currIdx + 1);
			}
		}
		// Return the Spot's value to zero before returning
		currSpot.value = 0;
		return;
	}
	
	/**
	 * Returns the first solution to the input Sudoku board if solve() has been
	 * called and at least one solution was found, otherwise returns the empty 
	 * string.
	 * @return string representing a Sudoku solution if one exists, 
	 * else the empty string
	 */
	public String getSolutionText() {
		// Return empty string if no solutions, else the first solution
		if (solutions.size() == 0) return ""; 
		return solutions.get(0);
	}
	
	/**
	 * If solve() has been called, returns the time in milliseconds it took to
	 * find at least one, and up to 100 solutions, else 0.
	 * @return the amount of time (ms) it took to solve the input Sudoku board
	 */
	public long getElapsed() {
		return elapsedTime; 
	}

	/**
	 * Spot class representing a single square in a Sudoku board,
	 * used to abstract methods used to solve a Sudoku problem
	 */
	public class Spot implements Comparable<Spot> {
		private int row, col, value, sqrRowMin, sqrRowMax, sqrColMin, sqrColMax;
		
		public Spot(int row, int col, int value) {
			this.row   = row;
			this.col   = col;
			this.value = value;
			
			// Pre-compute square locations as well
			sqrRowMin = getSqrMin(row);
			sqrRowMax = getSqrMax(row);
			sqrColMin = getSqrMin(col);
			sqrColMax = getSqrMax(col);
		}
		
		/**
		 * 
		 * @param idx
		 * @return
		 */
		public int getSqrMin(int idx) {
			return idx - (idx % PART);
		}
		
		/**
		 * 
		 * @param idx
		 * @return 
		 */
		public int getSqrMax(int idx) {
			return idx + ((SIZE - 1 - idx) % PART);
		}
		
		/**
		 * Set the Spot's value ivar to the specified integer. If the global 
		 * CHECK_ASSIGNMENTS is true and value is not 0, also performs a 
		 * check to see if value is already in use in the Spot's row, col, 
		 * or square
		 * @param value
		 */
		public void set(int value) {
			if (CHECK_ASSIGNMENTS   && 
				this.value == 0 &&
			    !getPossibleVals().contains(value)) {
				System.out.println("\n" + Sudoku.this.toString());
				throw new RuntimeException("(" + row + "," + col + ") '" + 
				value + "' assignment error");
			}
			this.value = value;
		}
		
		/**
		 * Returns a set of assignable values for this Spot, based on the
		 * values of the Spot's row, col, and square in the current Sudoku grid
		 * state
		 * @return set of possible values to which the Spot can be assigned
		 */
		private HashSet<Integer> getPossibleVals() {
			// If the Spot has a value, it has no more possible values
			if (this.value != 0) return new HashSet<Integer>();
			
			// Otherwise add possible values based on Spot's row, col, and square
			HashSet<Integer> possibleVals = new HashSet<Integer>(
					Arrays.asList(1,2,3,4,5,6,7,8,9) // start with all possible values
			);
			
			possibleVals.removeAll( getInUseRowVals() );
			possibleVals.removeAll( getInUseColVals() );
			possibleVals.removeAll( getInUseSqrVals() );
			return possibleVals;
		}
		
		/**
		 * Returns a set of non-zero values in use by members of the Spot's row
		 * @return
		 */
		private HashSet<Integer> getInUseRowVals() {
			HashSet<Integer> inUseVals = new HashSet<Integer>();
			
			int currVal; // Add values in the Spot's row if they aren't zero
			for (int col = 0; col < SIZE; col++) {
				currVal = grid[this.row][col].value;
				if (currVal != 0) inUseVals.add( currVal );
			}
			return inUseVals;
		}
		
		/**
		 * Returns a set of non-zero values in use by members of the Spot's col
		 * @return
		 */
		private HashSet<Integer> getInUseColVals() {
			HashSet<Integer> inUseVals = new HashSet<Integer>();
			
			int currVal; // Add values in the Spot's col if they aren't zero
			for (int row = 0; row < SIZE; row++) {
				currVal = grid[row][this.col].value;
				if (currVal != 0) inUseVals.add( currVal );
			}
			return inUseVals;
		}
		
		/**
		 * Returns a set of non-zero values in use by members of the Spot's square
		 * @return
		 */
		private HashSet<Integer> getInUseSqrVals() {
			HashSet<Integer> inUseVals = new HashSet<Integer>();
			
			int currVal; // Add values in the Spot's square if they aren't zero
			for (int row = sqrRowMin; row <= sqrRowMax; row++) {
				for (int col = sqrColMin; col <= sqrColMax; col++) {
					currVal = grid[row][col].value;
					if (currVal != 0) inUseVals.add( currVal );
				}
			}
			return inUseVals;
		}
		
		/**
		 * Counts the number of assignable numbers for the Spot
		 * @return Number of possible values this Spot can currently 
		 * be assigned to
		 */
		private int countAssignableVals() { 
			return getPossibleVals().size(); 
		}
		
		/**
		 * Sorts a Spot based on the number of possible values that it could
		 * possibly be assigned to, based on the values in its row, col, and 
		 * square. Spots with more possible assignable values are larger.
		 * @param other Spot whose size to compare this Spot's size to
		 */
		@Override
		public int compareTo(Spot other) {
			Integer mySize = this.countAssignableVals();
			return mySize.compareTo( other.countAssignableVals() );
		}
		
		@Override
		public String toString() {
			return "(" + row + "," + col + "): " + value;
		}
	}
	

	// Provided -- the deliverable main().
	// You can edit to do easier cases, but turn in
	// solving hardGrid.
	public static void main(String[] args) {
		Sudoku sudoku;
		sudoku = new Sudoku(hardGrid);
		System.out.println(sudoku); // print the raw problem
		
		int count = sudoku.solve();
		System.out.println("solutions:" + count);
		System.out.println("elapsed:" + sudoku.getElapsed() + "ms");
		System.out.println(sudoku.getSolutionText());
	}
}
