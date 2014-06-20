package assign4;

public class Transaction {

	final int fromAccount;
	final int toAccount;
	final int amount;  
	
	public Transaction(int from, int to, int amount) {
		fromAccount = from;
		toAccount   = to;
		this.amount = amount;
	}
}
