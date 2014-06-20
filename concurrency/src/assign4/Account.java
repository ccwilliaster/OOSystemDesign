package assign4;

public class Account {

	private int id;
	private int balance;
	private int transactions;
	
	public Account(int id) {
		this.id      = id;
		balance      = Bank.INIT_BALANCE;
		transactions = 0;
	}
	
	/**
	 * Removes the specified amount from the account, and increments the
	 * number of transactions for this account.
	 * @param amount
	 */
	public void withdraw(int amount) {
		balance -= amount;
		transactions++;
	}
	
	/**
	 * Adds the specified amount from the account, and increments the
	 * number of transactions for this account.
	 * @param amount
	 */
	public void deposit(int amount) {
		balance += amount;
		transactions++;
	}
	
	/**
	 * Returns a string summarizing the account: its ID, balance, and number
	 * of transactions
	 */
	@Override
	public String toString() {
		return "acct: " + id + " bal: " + balance + " trans: " + transactions;
	}
}
