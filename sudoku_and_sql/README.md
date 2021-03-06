####@author: ccwilliams
####@date:   2014-02

This directory contains two Java programs for Stanford's OOP class:
a **Sudoku game solver** with a Java Swing interface, and a basic 
**model-view-controller** project for interacting with a MySQL database 
(will not work without valid information in MyDBInfo.java file)

####SudokuFrame.java 
Provides the view for entering and solving a Sudoku game, 
the logic of which is encapsulated in `Sudoku.java`. Games are entered as a 
string, where 0s represent empty grid locations. It solves the game 
recursively starting with the MOST constrained locations first. It will 
attempt to find all possible solutions, and will display one randomly upon 
finish, along with the time it took to solve.

![Sudoku solver](./sudoku.png)

####MetropolisFrame.java
Provides the `main()` that initializes the GUI implementation 
that provides a view for the metropolis database. `MetropolisTableModel.java`
implements controller logic and interacts with the SQL database/model. The doc/ folder contains 
javadoc-generated documentation for `MetropolisTableModel.java`

**note: implementation subject to SQL injections (learned later)

![Metropolis view](./metropolis_view.png)
