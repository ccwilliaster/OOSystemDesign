@author: ccwilliams
@date:   2014-02 

Contains four Java projects for Stanford's OOP course aimed at gaining practice
with concurrency as well as Java command line programs:

Bank.java -
Bank which consists of 20 accounts initialized to some default amount, 
reads in file of transactions between specified accounts (ranged from 20-100k). 
Use threads to speed up transactions, but must properly lock both accounts 
involved in the transaction to preserve data integrity.

Divided into Account class (withdraw/deposit), Transaction class (immutable, 
to account/from account/amount), and Bank class containing an inner 
TransactionWorker class. Worker instances pull transactions from a 
BlockingQueue and lock accounts during transfers.

Cracker.java -
Contains two sub-routines "generation" and "cracking," which generate a
password hash for a given input string or determine the password for a given
hash using a specified number of threads, respectively:
 # generate SHA1 hash for "molly"
 > java Cracker molly 
 4181eecbd7a755d19fdf73887c54837cbecf63fd

 # determine the string (length <= 5) corresponding to this hash, 
 # using 8 threads for brute force
 > java Cracker 4181eecbd7a755d19fdf73887c54837cbecf63fd 5 8
 molly
 all done

For cracking, each thread is assigned a string space to recursively test.

JCounter.java -
![JCounter](jcounter.png)
GUI with several JCount objects which independently fork off a worker 
and count up to a value specified in their text field. This was meant to
demonstrate the utility of moving computationally intense routines off
of the GUI thread to prevent appearance of "freezing"

WebFrame.java, WebWorker.java - (see webloader.png)
WebFrame implements a GUI interface which uses WebWorker instances to 
download the contents of several URLs, specified in an input file.
Utilizes semaphores to coordinate workers.
