package assign4;

import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

/**
 * Class for generating SHA hash codes for input passwords, which can also
 * be used to crack a given hash code using a specified number of Cracker
 * thread objects
 */
public class Cracker extends Thread {
	
	// Constants
	public static final char[] // Array of chars used to produce strings
			CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();	
	private static final int MAX_THREADS = CHARS.length; 

	// Statics
	private static int MAX_LEN;
	private static int NUM_THREADS = 1;
	private static byte[] HASH_TO_MATCH;
	private static CountDownLatch latch;
	
	// Instance
	private int startIdx, stopIdx;
	
	// Each Cracker Thread is responsible for its assigned CHARS indices
	public Cracker(int startIdx, int stopIdx) {
		this.startIdx = startIdx;  
		this.stopIdx  = stopIdx;  
	}
	
	/**
	 * Signals to a Cracker worker that it should start searching its
	 * password space. Begins a recursive password with each length 1
	 * string within the Cracker's password space. Decrements the latch when
	 * done
	 */
	@Override
	public void run() {
		// Start recursive password search based on Cracker's specified space
		for (int i = startIdx; i <= stopIdx; i++) {
			recCrackPassword( CHARS[i] + "" );
		}
		latch.countDown(); // signal done
	}
	
	/**
	 * Recursive password cracking function. First checks the input String
	 * against the target hash value, prints it to screen if it matches, and
	 * recurses on all 1-char extensions of the input password if they do 
	 * not exceed the maximum password length.
	 * @param curr
	 */
	private void recCrackPassword(String curr) {
		// Print the current password if it matches the target HASH
		byte[] currAsBytes = passwordToHashArray(curr);
		
		if (hashesMatch(currAsBytes, HASH_TO_MATCH)) {
			System.out.println( curr );
		}
		
		// Recurse for all extensions of curr if we're under max length
		if ( curr.length() < MAX_LEN ) {
			for (int i = 0; i < CHARS.length; i++) {
				recCrackPassword( curr + CHARS[i] );
			}
		}
	}
	
	/**
	 * Checks the byte arrays of two hashes for equality.
	 * @param hash1 byte representation of one hash
	 * @param hash2 byte representation of other hash
	 * @return whether the hashes are the same
	 */
	private boolean hashesMatch(byte[] hash1, byte[] hash2) {
		return Arrays.equals(hash1, hash2);
	}
	
	/**
	 * Initializes the Crackers sub-program. Divides password space among the
	 * specified number of Cracker worker objects, and tells them to start.
	 * @param args the input argument vector, expected to be length 3
	 */
	private static void initCrackers(String[] args) {
		HASH_TO_MATCH = hexToArray( args[0] );
		MAX_LEN       = Integer.parseInt( args[1] );
		NUM_THREADS   = Integer.parseInt( args[2] );
		
		if (NUM_THREADS > MAX_THREADS || NUM_THREADS < 1) {
			throw new RuntimeException("Invalid number of Threads requested");
		}
		// Divide password space between Cracker worker threads
		ArrayList<ArrayList<Integer>> idxSplits = splitIdxs();
		
		// Initialize and start threads
		Cracker currCracker = null;
		for (int i = 0; i < NUM_THREADS; i++) {
			currCracker = new Cracker( idxSplits.get(i).get(0),
									   idxSplits.get(i).get(1) );
			currCracker.start();
		}
	}
	
	/**
	 * Splits the CHARS index space across the NUM_THREADS as evenly as 
	 * possible.
	 * @return array of indices representing the inclusive start, stop
	 * indices to be assigned to each Thread in NUM_THREADS
	 */
	private static ArrayList<ArrayList<Integer>> splitIdxs() {
		ArrayList<ArrayList<Integer>> splits = 
				new ArrayList<ArrayList<Integer>>(NUM_THREADS);
		
		int idxsPerCracker, remIdxs, start, stop;
		
		idxsPerCracker = CHARS.length / NUM_THREADS;
		remIdxs        = CHARS.length % NUM_THREADS;
		start          = 0;
		stop           = (idxsPerCracker - 1) + Math.min(remIdxs, 1); // 0 or 1
		
		for (int i = 0; i < NUM_THREADS; i++) {
			ArrayList<Integer> currIdxs = new ArrayList<Integer>(2);
			currIdxs.add(start);
			currIdxs.add(stop);
			splits.add(currIdxs);
			
			// Update remainder and indices
			remIdxs = Math.max(0, remIdxs-1); 
			start   = stop + 1;
			stop    = start + (idxsPerCracker - 1) + Math.min(remIdxs, 1);
		}
		//System.out.println(splits.toString());
		return splits;
	}
	
	/**
	 * Runs the Generator sub-program, which returns the String representation
	 * of an input password's hash value
	 * @param args the input argument vector, expected to be length 1
	 */
	private static void runGenerator(String[] args) {
		String hash = hexToString( passwordToHashArray( args[0] ) );
		System.out.println( hash );
	} 
	
	/**
	 * Returns the byte[] representation of the hash value of an input password
	 * @param password the password to hash
	 */
	private static byte[] passwordToHashArray(String password) {
		byte[] hashAsBytes = null;
		byte[] passwordAsBytes = password.getBytes();

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update( passwordAsBytes );
			hashAsBytes = digest.digest();
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		
		return hashAsBytes;
	}
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	public static void main (String[] args) {
		// Sub-routine based on the number of inputs
		if (args.length == 1) {
			runGenerator(args);
			
		} else if (args.length == 3) {
			latch = new CountDownLatch( Integer.parseInt(args[2]) );
			initCrackers( args );
			
			try {
				latch.await();
			} catch (Exception e) { e.printStackTrace(); }
			
			System.out.println("all done");
			
		} else { // wrong number of args
			throw new RuntimeException("Expected 1 or 3 input arguments.");
		}
	}
	
	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3
	// .,- 2f024f15758ce31567c1abc17e1ef4b747f30c7a
	// chr15 7a11012cc5931ca58b537e6ae1d17a8beffd5148
}
