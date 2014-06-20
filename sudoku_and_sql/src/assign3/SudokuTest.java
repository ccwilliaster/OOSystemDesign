package assign3;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

public class SudokuTest {

	@Test
	public void testSquareIdxs() {
		Sudoku sudoku = new Sudoku(Sudoku.easyGrid);
		Sudoku.Spot spot = sudoku.new Spot(0,0,0);	
		
		// Test limits of all index positions
		// Top or left
		assertTrue(spot.getSqrMin(0) == 0);
		assertTrue(spot.getSqrMin(1) == 0);
		assertTrue(spot.getSqrMin(2) == 0);
		assertTrue(spot.getSqrMax(0) == 2);
		assertTrue(spot.getSqrMax(1) == 2);
		assertTrue(spot.getSqrMax(2) == 2);
		
		// Middle
		assertTrue(spot.getSqrMin(3) == 3);
		assertTrue(spot.getSqrMin(4) == 3);
		assertTrue(spot.getSqrMin(5) == 3);
		assertTrue(spot.getSqrMax(3) == 5);
		assertTrue(spot.getSqrMax(4) == 5);
		assertTrue(spot.getSqrMax(5) == 5);
		
		// Bottom or right
		assertTrue(spot.getSqrMin(6) == 6);
		assertTrue(spot.getSqrMin(7) == 6);
		assertTrue(spot.getSqrMin(8) == 6);
		assertTrue(spot.getSqrMax(6) == 8);
		assertTrue(spot.getSqrMax(7) == 8);
		assertTrue(spot.getSqrMax(8) == 8);
	}
	

}
