// Board.java
package tetris;

import java.util.Arrays;
import java.lang.Math;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private int maxHeight;
	boolean committed;
	private static final boolean DEBUG = true; // true to call sanityCheck() 
	
	// Arrays/helper arrays used for the board itself
	private boolean[][] grid; 
	private int[] widths; 
	private int[] heights; 

	// Copies used to implement 1-level undo functionality
	private boolean[][] gridCopy;
	private int[] widthsCopy;
	private int[] heightsCopy;
	private int widthCopy;
	private int heightCopy;
	private int maxHeightCopy;
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		// Assign internal fields
		this.width     = width;
		this.height    = height;
		this.maxHeight = 0;
		committed = true;
		
		// Instantiate the board and helper arrays to correct sizes
		grid    = new boolean[width][height];
		widths  = new int[height];
		heights = new int[width];
		
		return;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return maxHeight; 
	}
	
	
	/**
	 * Checks the board for internal consistency, used for debugging. 
	 * Explicitly checks the values within the widths and heights arrays, and
	 * the maxHeight internal variable.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			// Catch any discrepancies with width, height, and maxHeight
			if ( !Arrays.equals(widths, sanityGetWidths()) ) {
				 throw new RuntimeException("Internal 'widths' = " + 
						 					Arrays.toString(widths) + 
						 					" expected "+  
						 					Arrays.toString(sanityGetWidths()));
			} 
			if ( !Arrays.equals(heights, sanityGetHeights()) ) {
				 throw new RuntimeException("Internal 'heights' = " + 
						 					Arrays.toString(heights) + 
						 					" expected "+  
						 					Arrays.toString(sanityGetHeights()));
			}		
		}
		return;
	}
	
	/**
	 * Manually constructs a new widths array, counting the number of filled 
	 * grid cells for each row in grid
	 * @return array of manually computed widths
	 */
	private int[] sanityGetWidths() {
		// Construct a new widths array manually, counting filled cells in grid
		int[] sanityWidths = new int[height];
		for (int y = 0; y < height; y++) {
			int currWidth = 0;
			for (int x = 0; x < width; x++) {
				if (grid[x][y]) currWidth++;
			}
			sanityWidths[y] = currWidth;
		}
		return sanityWidths;
	}
	
	/**
	 * Manually constructs a new heights array, counting the number of filled 
	 * grid cells for each col in grid. Throws an error if manually computed
	 * maxHeight is different from the internal variable of the same name.
	 * @return array of manually computed heights
	 */
	private int[] sanityGetHeights() {
		// Construct a new heights array manually, counting filled cells in grid
		int[] sanityHeights = new int[width];
		int sanityMaxHeight = 0;
		
		for (int x = 0; x < width; x++) {
			int currHeight = 0;
			for (int y = 0; y < height; y++) {
				if (grid[x][y]) currHeight = y + 1;
			}
			sanityHeights[x] = currHeight;
			if (currHeight > sanityMaxHeight) sanityMaxHeight = currHeight;
		}
		// Check max height against value of internal variable
		if (sanityMaxHeight != maxHeight) {
			throw new RuntimeException("Internal 'maxHeight' = " + 
									    maxHeight + " expected "+  sanityMaxHeight);
		}
		return sanityHeights;
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest (specifically, its (0,0))
	 if it were dropped straight down at that x.
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {		
		// Catch any x's out of bounds (will not check y)
		if (!pieceInBounds(piece, x, 0)) return PLACE_OUT_BOUNDS;
		
		// Find maximum effective height
		int[] skirt = piece.getSkirt();

		int currEffectiveMax;
		int yMax = -1;
		for (int skirtIdx = 0; skirtIdx < skirt.length; skirtIdx++) {
			// Effective height is grid value - skirt offset
			currEffectiveMax  = heights[skirtIdx + x] - skirt[skirtIdx];
			if (currEffectiveMax > yMax) yMax = currEffectiveMax;
		}
		return yMax;
	}
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	
	public int getColumnHeight(int x) {
		return heights[x];
	}
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		 return widths[y];
	}
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if (x >= width || y >= height) return true; // out of bounds
		return grid[x][y]; 
	}
	
	/**
	 * Makes copies of the current grid, helper arrays, and internal vars. 
	 * Used in implementing 1-level undo functionality. 
	 * Also changes committed field to false.
	 */
	private void makeStateCopies() {
		// Initialize new arrays for the copy
		gridCopy    = new boolean[width][height];
		widthsCopy  = new int[height];
		heightsCopy = new int[width];
		
		// Copy 2D grid, each x/width value is an array of y/height values
		for (int x = 0; x < width; x++) {
			System.arraycopy(grid[x], 0, gridCopy[x], 0, height);
		}
		// copy 1D helper arrays
		System.arraycopy(widths,  0, widthsCopy,  0, height);
		System.arraycopy(heights, 0, heightsCopy, 0, width);
		
		// and width, height, maxHeight ivars
		widthCopy     = width;
		heightCopy    = height;
		maxHeightCopy = maxHeight;
		committed = false; 
		
		return;
	}

	/**
	 * Helper function to revert grid and helper array pointers back to a 
	 * saved copy of a previous state. 
	 * Also changes committed field to true.
	 */
	private void revertToCopies() {
		// arrays
		grid    = gridCopy;
		widths  = widthsCopy;
		heights = heightsCopy; 
		// ivars
		width     = widthCopy;
		height    = heightCopy;
		maxHeight = maxHeightCopy;
		
		commit(); // commit after this change
		return;
	}
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// Must be committed, flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
		
		// Copy board for undo capability, also sets committed to false
		makeStateCopies(); 
		
		// Check if piece is in bounds
		if ( !pieceInBounds(piece, x, y) ) return PLACE_OUT_BOUNDS;
		
		int result = PLACE_OK;
		boolean rowFilled = false;
		// Place each point in Piece
		for (TPoint pt : piece.getBody() ) {
			result = placePoint(pt.x + x, pt.y + y);
			if (result == PLACE_BAD) return PLACE_BAD;
			if (result == PLACE_ROW_FILLED) rowFilled = true;
		} 
		sanityCheck();
		return rowFilled ? PLACE_ROW_FILLED : PLACE_OK;
	}
	
	/**
	 * Returns whether dropping the piece with its origin at the specified 
	 * (x,y) is inbounds. 
	 * @param piece Piece to drop
	 * @param x x position to drop Piece origin
	 * @param y y position to drop Piece origin
	 * @return whether the piece would be in bounds
	 */
	private boolean pieceInBounds(Piece piece, int x, int y) {
		if ( x < 0 || x + piece.getWidth()  > width || 
		     y < 0 || y + piece.getHeight() > height ) return false;		
		return true;
	}
	
	/**
	 * Checks whether a specified row is full or not
	 * @param row row to check 'fullness' of
	 * @return
	 */
	private boolean rowIsFull(int row) {
		return widths[row] >= width;
	}
	
	/** 
	 * Helper function to fill a single point in the board. Catches
	 * indices that are already filled, otherwise sets specified cell on 
	 * board to true, updating the grid, widths, heights, and maxHeight ivars.
	 * Returns sentinel reflecting placement.
	 * @param x x value of point to fill in grid
	 * @param y y value of point to fill in grid
	 * @return PLACE_OK, PLACE_BAD, or PLACE_ROW_FILLED
	 */
	private int placePoint(int x, int y) {
		// Catch indices that are filled already
		if (grid[x][y]) return PLACE_BAD;
		
		// Fill grid, update widths, heights, and maxHeight
		grid[x][y] = true;
		widths[y] += 1; 
		if (heights[x] < y + 1) heights[x] = y + 1; // check in case pt above
		if (y + 1 > maxHeight) maxHeight = y + 1;   // check in case pt above or diff x
		
		return rowIsFull( y ) ? PLACE_ROW_FILLED : PLACE_OK;
	}
	
	/**
	 Deletes rows that are filled all the way across, moving
	 rows above cleared rows down. Returns the number of rows cleared.
	*/
	public int clearRows() {		
		// Copy the current state to come back to, if this hasn't been done
		if (committed) makeStateCopies(); 

		// Now clear rows in a single pass
		int rowsCleared = 0;
		for(int currRow = 0; currRow < height; currRow++) {
			if (currRow >= maxHeight) break; // unnecessary past this point
			
			if (rowIsFull(currRow)) { // clear row and increment counter if full
				rowsCleared++;
				clearRowAndWidths(currRow);
			} else if (rowsCleared > 0) { // else shift grid and width values
				moveRow(currRow, currRow - rowsCleared);
				moveWidth(currRow, currRow - rowsCleared);
			}
		}
		decrementHeights(); // update heights values
		sanityCheck(); // validate new state
		return rowsCleared;
	}
	
	/**
	 * Moves the contents of a one widths index to another. Sets 
	 * the startIdx to 0
	 * @param startY index to move from
	 * @param destY index to move to
	 */
	private void moveWidth(int startY, int destY) {
		widths[destY]  = widths[startY];
		widths[startY] = 0;	 
		return;
	}
	
	/**
	 * Decrements the internal heights array by the specified value, as well
	 * as the maxHeights internal variable. Useful when calling clearRows()
	 * @param decrementBy value to remove from each index of the heights array
	 */
	private void decrementHeights() {
		// For each x position, move from the old height downwards to 
		// find the new height index.
		int newMaxHeight = 0;
		for (int x = 0; x < width; x++) {
			for (int y = Math.min(heights[x], height - 1); y >= 0; y--) {
				if (grid[x][y]) {
					heights[x] = y + 1;
					if (heights[x] > newMaxHeight) newMaxHeight = heights[x];
					break;
				} else if (y == 0) { // empty column
					heights[x] = 0;
				}
			}
		}
		maxHeight = newMaxHeight;
		return;
	}
	
	/**
	 * Moves the contents of a row from one index to another, setting
	 * the starting index to false
	 * @param startY index of row to shift
	 * @param destY destination index for row to shift
	 */
	private void moveRow(int startY, int destY) {
		// Iterate over each col, shifting single cells as we go
		for (int x = 0; x < width; x++) {
			grid[x][destY]  = grid[x][startY];
			grid[x][startY] = false;
		}
		return;
	}
	
	/**
	 * Clears the row and width array at the specified y index, 
	 * by setting all grid values to false and widths to 0. 
	 * Useful for creating a fresh row during clearRows().
	 * @param y index of row to clear
	 */
	private void clearRowAndWidths(int y) {
		for (int x = 0; x < width; x++) { grid[x][y] = false; }
		widths[y] = 0;
	}
	
	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	*/
	public void undo() {
		// Do nothing if board is in committed state
		if (committed) return;
		
		// Otherwise change board pointers to copies from previous state
		revertToCopies(); 
		sanityCheck(); // check state of board after this
		return;
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true; // don't care what previous state was
	}

	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


