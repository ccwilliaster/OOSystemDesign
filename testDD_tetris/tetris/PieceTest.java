package tetris;

import static org.junit.Assert.*;
import java.util.*;

import org.hamcrest.core.IsNull;
import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece stk1, stk2;
	private Piece sqr1;
	private Piece l1_1, l1_2, l1_3, l1_4;
	private Piece l2_1, l2_2, l2_3, l2_4;
	private Piece s1_1, s1_2;
	private Piece s2_1, s2_2;
	

	@Before
	public void setUp() throws Exception {
		// Set up pyramids
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		// Sticks
		stk1 = new Piece(Piece.STICK_STR);
		stk2 = stk1.computeNextRotation();
		
		// Square
		sqr1 = new Piece(Piece.SQUARE_STR);
		
		// L1
		l1_1 = new Piece(Piece.L1_STR);
		l1_2 = l1_1.computeNextRotation();
		l1_3 = l1_2.computeNextRotation();
		l1_4 = l1_3.computeNextRotation();
		
		// L2
		l2_1 = new Piece(Piece.L2_STR);
		l2_2 = l2_1.computeNextRotation();
		l2_3 = l2_2.computeNextRotation();
		l2_4 = l2_3.computeNextRotation();
		
		// S1
		s1_1 = new Piece(Piece.S1_STR);
		s1_2 = s1_1.computeNextRotation();
		
		// S2
		s2_1 = new Piece(Piece.S2_STR);
		s2_2 = s2_1.computeNextRotation();
	}
	
	@Test
	public void testWidth() { 
		// Check Pyramids
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr3.getWidth());
		assertEquals(2, pyr4.getWidth());
		
		// Sticks
		assertEquals(1, stk1.getWidth());
		assertEquals(4, stk2.getWidth());
		
		// L1
		assertEquals(2, l1_1.getWidth());
		assertEquals(3, l1_2.getWidth());
		assertEquals(2, l1_3.getWidth());
		assertEquals(3, l1_4.getWidth());
		
		// L2
		assertEquals(2, l2_1.getWidth());
		assertEquals(3, l2_2.getWidth());
		assertEquals(2, l2_3.getWidth());
		assertEquals(3, l2_4.getWidth());
		
		// S1
		assertEquals(3, s1_1.getWidth());
		assertEquals(2, s1_2.getWidth());

		// S2
		assertEquals(3, s2_1.getWidth());
		assertEquals(2, s2_2.getWidth());
		
		// Square
		assertEquals(2, sqr1.getWidth());
		
		return; 
	}
	
	@Test
	public void testHeight() { 
		// Check Pyramids
		assertEquals(2, pyr1.getHeight());
		assertEquals(3, pyr2.getHeight());
		assertEquals(2, pyr3.getHeight());
		assertEquals(3, pyr4.getHeight());
		
		// Sticks
		assertEquals(4, stk1.getHeight());
		assertEquals(1, stk2.getHeight());
		
		// L1
		assertEquals(3, l1_1.getHeight());
		assertEquals(2, l1_2.getHeight());
		assertEquals(3, l1_3.getHeight());
		assertEquals(2, l1_4.getHeight());
		
		// L2
		assertEquals(3, l2_1.getHeight());
		assertEquals(2, l2_2.getHeight());
		assertEquals(3, l2_3.getHeight());
		assertEquals(2, l2_4.getHeight());
		
		// S1
		assertEquals(2, s1_1.getHeight());
		assertEquals(3, s1_2.getHeight());

		// S2
		assertEquals(2, s2_1.getHeight());
		assertEquals(3, s2_2.getHeight());
		
		// Square
		assertEquals(2, sqr1.getHeight());
		
		return; 
	}

	@Test
	public void testSkirt() { 
		// Test pyramid
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0},    pyr2.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 1},    pyr4.getSkirt()));
		
		assertEquals(pyr1.getWidth(), pyr1.getSkirt().length); // skirt should always be same as width
		
		// stick
		assertTrue(Arrays.equals(new int[] {0},          stk1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, stk2.getSkirt()));
		
		assertEquals(stk2.getWidth(), stk2.getSkirt().length);
		
		// L1
		assertTrue(Arrays.equals(new int[] {0, 0},    l1_1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, l1_2.getSkirt()));
		assertTrue(Arrays.equals(new int[] {2, 0},    l1_3.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 1, 1}, l1_4.getSkirt()));

		// L2
		assertTrue(Arrays.equals(new int[] {0, 0},    l2_1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 1, 0}, l2_2.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 2},    l2_3.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, l2_4.getSkirt()));
		
		// S1
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s1_1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0},    s1_2.getSkirt()));
		
		// S2
		assertTrue(Arrays.equals(new int[] {1, 0, 0}, s2_1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 1},    s2_2.getSkirt()));
		
		assertEquals(s2_1.getWidth(), s2_1.getSkirt().length);
		
		// square
		assertTrue(Arrays.equals(new int[] {0, 0}, sqr1.getSkirt()));
		
		return; 
	}
	
	@Test
	public void testEquals() { 
		// Check a few simple cases that should be true/false
		// rotations aren't equal
		assertFalse(pyr1.equals(pyr2)); 
		assertFalse(stk1.equals(stk2));
		assertFalse(l1_1.equals(l1_4));
		
		// different shapes aren't equal
		assertFalse(pyr1.equals(sqr1));
		assertFalse(s1_1.equals(s2_1));
		assertFalse(l2_2.equals(l2_1));
		
		// self should be equal
		assertTrue(l1_1.equals(l1_1));
		assertTrue(s2_1.equals(s2_1));
		assertTrue(sqr1.equals(sqr1));
		assertTrue(sqr1.equals( sqr1.computeNextRotation() )); // square is always itself
	 	assertTrue(stk1.equals( stk2.computeNextRotation() )); // 2x stick rotations are equal
	 	assertTrue(s1_1.equals( s1_2.computeNextRotation() )); // 2x S rotations are equal
		assertTrue(l1_1.equals( l1_4.computeNextRotation() )); // 4x L rotations are equal
		
		// Test that equals is not sensitive to order of TPoints in body array
		Piece order_1    = new Piece("0 0  0 1  0 2  1 0");
		Piece order_2    = new Piece("0 1  1 0  0 2  0 0");
		Piece order_3    = new Piece("0 2  0 0  0 1  1 0");
		Piece order_diff = new Piece("0 3  0 0  0 1  1 0"); // a diff point should be diff
		
		assertTrue(order_1.equals(order_2));
		assertTrue(order_2.equals(order_3));
		assertFalse(order_2.equals(order_diff));
		
		// number of TPoints in body should not matter, 
		// and it should work for n-digit numbers too
		Piece size_1    = new Piece("55 5  0 2  5 600");
		Piece size_2    = new Piece("0 2  5 600  55 5");
		assertTrue(size_1.equals(size_2));
		
		// spacing should not matter either
		Piece space_1 = new Piece("5 5 0 2    5 6");
		Piece space_2 = new Piece("5 5 0 2 5   6");
		assertTrue(space_1.equals(space_2));
		assertFalse(size_2.equals(order_2)); // but different NUMBER of points should be diff

		return; 
	}

	@Test
	public void testPieces() { 
		
		Piece[] pieces = Piece.getPieces();
		
		//  check appropriate indices for a couple
		assertTrue(stk1.equals( pieces[Piece.STICK] ));
		assertTrue(sqr1.equals( pieces[Piece.SQUARE] ));
		assertTrue(l1_1.equals( pieces[Piece.L1] ));
		
		// check that pointers are correct rotations
		// S1
		Piece test_s1_1 = pieces[Piece.S1];
		Piece test_s1_2 = test_s1_1.fastRotation();
		
		assertTrue(s1_1.equals(test_s1_1));
		assertTrue(s1_2.equals(test_s1_2));
		
		// pyramid
		Piece test_pyr1 = pieces[Piece.PYRAMID];
		Piece test_pyr2 = test_pyr1.fastRotation();
		Piece test_pyr3 = test_pyr2.fastRotation();
		Piece test_pyr4 = test_pyr3.fastRotation();
		Piece test_pyr1_circle = test_pyr4.fastRotation(); // test full circle pointers
		Piece test_pyr2_circle = test_pyr1_circle.fastRotation();  
		
		assertTrue(pyr1.equals(test_pyr1));
		assertTrue(pyr2.equals(test_pyr2));
		assertTrue(pyr3.equals(test_pyr3));
		assertTrue(pyr4.equals(test_pyr4));
		assertTrue(pyr1.equals(test_pyr1_circle));
		assertTrue(pyr2.equals(test_pyr2_circle));
		
		return; 
	}
	
}
