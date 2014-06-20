package assign4;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.io.BufferedReader;
import java.io.FileReader;

/** 
 * Bank class which executes Transactions between Accounts in a thread-safe
 * manner. The Bank class itself initializes Accounts, reads in Transactions,
 * and creates Workers to carry out the transactions as they are read.
 */
public class Bank {
	
	static final Transaction nullTrans = new Transaction(-1,0,0); 
	static final int QUEUE_CAPACITY = 1000;
	static final int INIT_BALANCE   = 1000;
	static final int NUM_ACCTS      = 20;
	
	private int NUM_WORKERS;
	private ArrayList<Account> accounts;
	BlockingQueue<Transaction> transactionQueue;
	private CountDownLatch latch;
	
	public Bank(int numWorkers) {
		// Determine the number of workers necessary, set latch for coordination
		NUM_WORKERS = numWorkers;
		latch = new CountDownLatch(numWorkers);
		
		// Initialize the blocking queue
		transactionQueue = new ArrayBlockingQueue<Transaction>(QUEUE_CAPACITY);
		
		// Initialize Accounts and TransactionWorkers
		initAccounts();
		initWorkers();
	}

	/**
	 * Initializes the Bank instance account array array of NUM_ACCTS Accounts, 
	 * each with INIT_BALANCE and an ID corresponding to their array index.
	 */
	private void initAccounts() {
		accounts = new ArrayList<Account>(NUM_ACCTS);
		for (int i = 0; i < NUM_ACCTS; i++) {
			accounts.add(i, new Account(i) );
		}
		return;
	}
	
	/**
	 * Initializes and calls .start() on NUM_WORKERS TransactionWorkers
	 */
	private void initWorkers() {
		TransactionWorker currWorker = null;
		for (int i = 0; i < NUM_WORKERS; i++) {
			currWorker = new TransactionWorker();
			currWorker.start();
		}
	}
	
	/**
	 * Reads transaction information from the specified file, and creates and
	 * enqueues the corresponding Transaction objects into the transactionQueue.
	 * Also adds NUM_WORKERS nullTransaction references to the end of the queue
	 * to signal to the workers that there are no more Transactions.
	 * @param fileName The (path/) filename of the transaction file to be parsed
	 */
	private void getTransactions(String fileName) {
		Transaction newTrans = null;
		int from, to, amount;
		String line = null;
		
		try { // Create one transaction per line, and enqueue
			BufferedReader reader = new BufferedReader( new FileReader(fileName) );
			
			while ((line = reader.readLine()) != null) {
			   
				String[] parts = line.split("\\s"); // from, to, amount
			    if (parts.length != 3) {
			    	throw new RuntimeException("3 values expected for all input lines");
			    }
			    from   = Integer.parseInt(parts[0]);
			    to     = Integer.parseInt(parts[1]);
			    amount = Integer.parseInt(parts[2]);
			    newTrans = new Transaction(from, to, amount);
			    transactionQueue.put(newTrans);
			    
			}
			reader.close();
		} catch (Exception e) { e.printStackTrace(); }
		
		// Finally, add one null transaction per worker so they know when to stop
		for (int i = 0; i < NUM_WORKERS; i++) {
			try {
				transactionQueue.put( nullTrans );
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		return;
	}
	
	/**
	 * Calls toString() on each Account in the accounts list
	 */
	public void printAccounts() {
		for (Account account : accounts) {
			System.out.println( account.toString() );
		}
	}
	
	/**
	 * Worker class which carries out Bank Transactions between Accounts
	 */
	private class TransactionWorker extends Thread{
		
		/**
		 * Takes Transactions from the transactionQueue and completes them
		 * When Worker gets a nullTrans Transaction, it stops working and 
		 * decrements the Bank latch.
		 */
		@Override
		public void run() {
			try {
				Transaction currTransaction;
				Account firstLock, secondLock;
				int amount;
				
				while (true) {
					currTransaction = transactionQueue.take();
					if (currTransaction == nullTrans) break; // done 
					
					// Determine order of locks, lowest ID first for consistency
					if (currTransaction.fromAccount < 
							currTransaction.toAccount) {
						firstLock  = accounts.get( currTransaction.fromAccount );
						secondLock = accounts.get( currTransaction.toAccount );
					} else if (currTransaction.fromAccount > 
							currTransaction.toAccount) {
						firstLock  = accounts.get( currTransaction.toAccount );
						secondLock = accounts.get( currTransaction.fromAccount );
					} else { // to and from are the same
						firstLock  = accounts.get( currTransaction.fromAccount );
						secondLock = accounts.get( currTransaction.fromAccount );
					}
					
					// Carry out the actual transaction, thread safe
					synchronized(firstLock) {
						synchronized(secondLock) {
							amount = currTransaction.amount;
							accounts.get(currTransaction.fromAccount).withdraw(amount);
							accounts.get(currTransaction.toAccount).deposit(amount);
						}
					}			
				}
				latch.countDown(); // signal done
				
			} catch (Exception e) { e.printStackTrace(); } 
		}
	}
		
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new RuntimeException("Two arguments expected ...");
		}
		Bank bank = new Bank( Integer.parseInt(args[1]) ); 
		bank.getTransactions( args[0] );
		
		try { // Wait for workers to finish
			bank.latch.await();
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		bank.printAccounts();
	}
}
