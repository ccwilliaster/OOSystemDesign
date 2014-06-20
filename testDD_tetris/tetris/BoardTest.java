package tetris;

import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated, sqr, stk, l;

	// Setup useful objects before each test call
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		l   = new Piece(Piece.L1_STR);
		sqr = new Piece(Piece.SQUARE_STR);
		stk = new Piece(Piece.STICK_STR);
	}
	
	@Test
	public void testSimplePlacement() {
		// Place a single Piece
		int result = b.place(pyr1, 0, 0);
		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
		
		// Place another in what should be a valid location
		b.commit();
		result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	@Test
	public void testClearRows() {
		// Simple, 1-row clear
		b.place(pyr1, 0, 0);
		b.commit();
		b.place(sRotated, 1, 1);
		b.clearRows(); // should clear bottom row
		
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(2, b.getColumnHeight(2));
		assertEquals(3, b.getMaxHeight());
		
		assertEquals(2, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(1, b.getRowWidth(2));
		assertEquals(0, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		assertEquals(0, b.getRowWidth(5));
		
		// More complex, 2-row clear
		b.commit();
		int result = b.place(stk, 0, 0);
		assertEquals(Board.PLACE_ROW_FILLED, result);
		b.clearRows(); // should clear bottom 2 rows
		assertEquals(2, b.getColumnHeight(0));
		assertEquals(1, b.getColumnHeight(1));
		assertEquals(0, b.getColumnHeight(2));
		assertEquals(2, b.getMaxHeight());
		
		assertEquals(2, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
		assertEquals(0, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		assertEquals(0, b.getRowWidth(5));
		
		// check that we can clear at the top		
		b.commit();
		b.place(pyr1, 0, 2); // row 2 full
		b.commit();
		b.place(pyr3, 0, 4); // row 5 full

		assertEquals(3, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		assertEquals(1, b.getRowWidth(4));
		assertEquals(3, b.getRowWidth(5));
		assertEquals(6, b.getColumnHeight(0));
		assertEquals(6, b.getColumnHeight(1));
		assertEquals(6, b.getColumnHeight(2));
		
		b.clearRows();
		assertEquals(2, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(1, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		assertEquals(0, b.getRowWidth(5));
		assertEquals(2, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(0, b.getColumnHeight(2));
		
		return;
	}
	
	@Test
	public void testUndo() {
		// Simple place() --> undo() sequence
		b.place(pyr1, 0, 0);
		assertEquals(2, b.getMaxHeight());
		b.undo();
		assertEquals(0, b.getMaxHeight());
		
		// More complex sequence: place() --> clearRows() --> undo()
		b.place(pyr1, 0, 0);
		b.commit();		
		b.place(sRotated, 1, 1);
		assertEquals(4, b.getMaxHeight());
		
		b.clearRows();
		assertEquals(3, b.getMaxHeight());
		assertEquals(2, b.getRowWidth(0));

		b.undo(); // should return to pre-sRotated placement
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(2, b.getMaxHeight());
		
		// Same sequence but commit() before clearRows()
		b.place(sRotated, 1, 1);
		b.commit();
		b.clearRows();
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(2, b.getColumnHeight(2));
		
		b.undo(); // sRotated should still be there
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		
		// Try adding one more piece (3 total now)
		int result = b.place(stk, 0, 1);
		assertEquals(result, Board.PLACE_ROW_FILLED);
		assertEquals(5, b.getColumnHeight(0));
		b.clearRows();
		assertEquals(2, b.getColumnHeight(0));
		assertEquals(0, b.getColumnHeight(2));
		
		b.undo(); // then undo
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(2, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		
		b.undo(); // a second undo should do nothing
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(2, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		assertEquals(0, b.getRowWidth(4));
		
		return;
	}
	
	@Test
	public void testBadPlacements() {
		// Check filled and bad placement
		int result_fill = b.place(pyr1, 0, 0); // First should be okay
		b.commit();
		int result_bad  = b.place(pyr1, 0, 1); // on top of other
		b.undo();
		assertEquals(Board.PLACE_ROW_FILLED, result_fill);
		assertEquals(Board.PLACE_BAD, result_bad);
		
		// Check out of bounds, all directions
		int result_bounds_left  = b.place(pyr1, -1, 2);
		b.undo();
		int result_bounds_right = b.place(pyr1, 2, 2);
		b.undo();
		int result_bounds_down  = b.place(pyr1, 0, -12);
		b.undo();
		int result_bounds_up    = b.place(pyr1, 0, 5);
		b.undo();
		
		assertEquals(Board.PLACE_OUT_BOUNDS, result_bounds_left);
		assertEquals(Board.PLACE_OUT_BOUNDS, result_bounds_right);
		assertEquals(Board.PLACE_OUT_BOUNDS, result_bounds_down);
		assertEquals(Board.PLACE_OUT_BOUNDS, result_bounds_up);
		
		// Check ok placement
		int result_ok = b.place(sqr, 0, 2);
		assertEquals(Board.PLACE_OK, result_ok);
		
		return;
	}
	
	@Test
	public void testGetDropHeight() {
		// Simple case
		assertEquals(0, b.dropHeight(pyr1, 0));
		
		// More complex with placed piece
		b.place(sqr, 0, 0);
		b.commit();
		assertEquals(2, b.dropHeight(pyr1, 0)); 
		assertEquals(0, b.dropHeight(stk,  2)); 
		assertEquals(2, b.dropHeight(sRotated, 0));
		assertEquals(1, b.dropHeight(sRotated, 1)); //correct piece boundary
		
		// Test with more pieces + clearRows()
		b.place(stk, 2, 0);
		b.commit();
		assertEquals(2, b.dropHeight(sqr, 0));
		assertEquals(4, b.dropHeight(sqr, 1));
		
		b.clearRows();
		assertEquals(0, b.dropHeight(sqr, 0));
		assertEquals(2, b.dropHeight(sqr, 1));
		
		// Test after undo()
		b.undo();
		assertEquals(2, b.dropHeight(sqr, 0));
		assertEquals(4, b.dropHeight(sqr, 1));
		
		b.place(sqr, 0, 2);
		b.clearRows(); // should be completely empty, back to starting point
		assertEquals(0, b.dropHeight(pyr1, 0));

		return;
	}
	
}
