package tetris;

import java.awt.*;
import javax.swing.*;

import tetris.DefaultBrain;

public class JBrainTetris extends JTetris {
	
	// Brain object for auto-play
	protected Brain brain; 
	protected Brain.Move bestMove; 
	
	// Brain controls, labels
	protected JLabel brainModeLabel;
	protected JCheckBox brainMode;
	protected boolean brainOn;
	protected int prevCount; // track when brain needs to find new best move
	protected JCheckBox animateMode; // toggles Piece dropping when brain active
	
	// Adversary controls, labels
	protected JLabel adversaryLabel;
	protected JSlider adversary;
	protected JLabel statusLabel;
	
	public JBrainTetris(int pixels) {
		super(pixels);
		brain = new DefaultBrain(); 
	}

	/**
	 * Adds brain components and adversary to the standard JTetris Frame
	 */
	@Override
	public JComponent createControlPanel() {
		JComponent panel = super.createControlPanel();
		
		// Brain
		panel.add(Box.createVerticalStrut(20));
		brainModeLabel = new JLabel("Brain:");
		panel.add(brainModeLabel);

		brainMode = new JCheckBox("Brain active");
		panel.add(brainMode);
		
		brainOn = brainMode.isSelected(); // default false
		
		// Brain drop vs fall animation 
		animateMode = new JCheckBox("Animate Falling", true);
		panel.add(animateMode);
		
		// Adversary
		panel.add(Box.createVerticalStrut(20));
		JPanel adversaryRow = new JPanel();	
		adversaryLabel = new JLabel("Adversary:");
		adversaryRow.add(adversaryLabel);
		
		adversary = new JSlider(0, 100, 0); // min, max, curr
		adversary.setPreferredSize(new Dimension(100, 15));
		adversaryRow.add(adversary);
		panel.add(adversaryRow);
		
		// Adversary status label 
		statusLabel = new JLabel("ok");
		panel.add(statusLabel);
		
		return panel;
	};
	
	/**
	 * Uses the brain to compute the best score possible for each Piece, given
	 * the current board. Returns the Piece with the WORST score of these. 
	 * The inner workings of the adversary, to make the game harder.
	 * @return Piece that gives the worst, best score given the current board
	 */
	private Piece findWorstPiece() {
		double worstScore = -1;
		double currScore;
		Brain.Move currMove;
		Piece worstPiece = null;
		
		// Loop through all pieces, find WORST (highest score) best move
		for (Piece piece : pieces) {
			currMove = brain.bestMove(board, piece, HEIGHT, null);
			
			if (currMove == null) throw new RuntimeException("Null 'worst move'"); 

			currScore = currMove.score;
			if (worstScore == -1 || currScore > worstScore) {
				worstScore = currScore;
				worstPiece = piece;
			}
		}
		return worstPiece;
	}
	
	/**
	 * Choose a random piece or choose the worst piece as predicted by
	 * the brain, depending on the adversary slider value. Low on slider =
	 * better chance of a good piece. Changes the status label to *ok* if 
	 * the 'worst' piece was chosen.
	 */
	@Override 
	public Piece pickNextPiece() {
		int adversaryLevel = adversary.getValue();
		int randomLevel    = random.nextInt(99) + 1; // 1-99 inclusive
		
		// The Brain will return null if we are 1 away from being over height
		boolean atMax = board.getMaxHeight() >= HEIGHT;
		
		// Randomly choose a piece based on adversary level and atMax boolean
		if (randomLevel >= adversaryLevel || atMax) {
			statusLabel.setText("ok");
			return super.pickNextPiece();
		} 
		// Otherwise choose the worst piece
		else { 
			statusLabel.setText("*ok*");
			return findWorstPiece();
		} 
	}
	
	/**
	 * Nudges the current Piece in the 'best' direction if it is off course.
	 * If animate falling mode is toggled off, also drops piece if we are
	 * at the best position / rotation
	 */
	private void brainNudge(Piece currPiece, Brain.Move bestMove) {
		// Drop the piece if placement is correct and animate mode is off
		if (!animateMode.isSelected() && 
			 currentX == bestMove.x   &&
			 currPiece.equals(bestMove.piece)) {
			super.tick(DROP);
			moved = false; // to not get stuck in prev frame's super.tick() call
		}
		// Move the piece left or right if we are not at best x
		if (currentX > bestMove.x) {
			super.tick(LEFT);
		} else if (currentX < bestMove.x) {
			super.tick(RIGHT);
		}
		// Rotate the piece if it's not the best rotation
		if (!currPiece.equals(bestMove.piece)) {
			super.tick(ROTATE);
		}
	}

	
	/**
	 Called to change the position of the current piece.
	 Each key press calls this once with the verbs
	 LEFT RIGHT ROTATE DROP for the user moves,
	 and the timer calls it with the verb DOWN to move
	 the piece down one square.

	 Before this is called, the piece is at some location in the board.
	 This advances the piece to be at its next location.
	 
	 Overriden by the brain when it plays.
	 @param verb verb (see super.tick()) specifying the type of move
	*/
	@Override
	public void tick(int verb) {
		if (!gameOn) return;
		
		brainOn = brainMode.isSelected();
		
		if (currentPiece != null) {
			board.undo();
		}
		// If a new Piece has been added, find the best move
		// bestMove is null if we are at the maximum board height (not over)
		if (prevCount != count) {
			bestMove = brain.bestMove(board, currentPiece, HEIGHT, bestMove);
			prevCount = count;
		}
		
		// If we're moving downward and the brain is on, move the piece on-track
		if (verb == DOWN && brainOn && bestMove != null) {
			brainNudge(currentPiece, bestMove);
		}
		super.tick(verb);
	}
	
	/**
	 * Creates a frame with a JBrainTetris
	 */
	public static void main(String[] args) {
		// Set GUI Look And Feel Boilerplate.
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JBrainTetris brainTetris = new JBrainTetris(16);
		JFrame frame = JBrainTetris.createFrame(brainTetris);
		frame.setVisible(true);

	}

}
