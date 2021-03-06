package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The class that contains data and logic related to the artificial intelligence
 * of the game.
 * 
 * @author Fu-Yin Lin
 * 
 */
public class ComputerPlayer extends Player {
	private static final HashMap<Level, Integer> levelMap = new HashMap<Level, Integer>();
	private Level difficultyLevel = Level.MEDIUM;

	/**
	 * Constructor with only difficulty level defined. Set the computer player
	 * default name to be "AI".
	 * 
	 * @param difficultyLevel the difficulty level of the computer moves chosen by
	 *                        the user in this game (ie. how smart the computer
	 *                        moves).
	 */
	public ComputerPlayer(Level difficultyLevel) {
		super("AI");
		this.difficultyLevel = difficultyLevel;
		initLevelMap();
	}

	/**
	 * Constructor with both stone color and difficulty known. Set the computer
	 * player default name to be "AI".
	 * 
	 * @param color           the stone color that the computer player will be
	 *                        playing in this game.
	 * @param difficultyLevel the difficulty level of the computer moves chosen by
	 *                        the user in this game (ie. how smart the computer
	 *                        moves).
	 */
	public ComputerPlayer(Stone color, Level difficultyLevel) {
		super("AI", color);
		this.difficultyLevel = difficultyLevel;
		initLevelMap();
	}

	/**
	 * A helper method that will be called when a ComputerPlayer object is
	 * initialized. It restricts the number of possible moves that the computer can
	 * randomly select from. For example, the hardest level only has one value
	 * available in the score table and that is the one with the highest overall
	 * score.
	 */
	private void initLevelMap() {
		levelMap.put(Level.EASY, 3);
		levelMap.put(Level.MEDIUM, 2);
		levelMap.put(Level.HARD, 1);
	}

	/**
	 * Get the difficulty level of this computer player.
	 * 
	 * @return the level of difficulty of the current computer player.
	 */
	public Level getDifficultyLevel() {
		return difficultyLevel;
	}

	/**
	 * Depending on the difficulty level set by the user, this method determines the
	 * next computer move by using both generateMoveList and calculateScores helper
	 * methods to generate a list of possible moves ranked in descending order and
	 * randomly picks a move from the list.
	 * 
	 * @param currentConfig current game configuration used to access current board data.
	 * @return the computer player's next move in the current game.
	 */
	@Override
	public Move getMove(GameConfiguration currentConfig) {
		Stone[][] board = currentConfig.getChessBoard().getBoard();
		int boardSize = currentConfig.getChessBoard().getBoardSize();
		int targetRow = -1;
		int targetCol = -1;
		Move computerMove = null;
		/*
		 * Get a move list that includes a list of coord(s) that has the highest scores
		 * from the score table. The number of coords in the list is determined by the
		 * difficulty level of the computer player.
		 */
		ArrayList<int[]> moveList = generateMoveList(board, boardSize);
		// Uses Random class to randomly pick one coord from the move list.
		Random rand = new Random();
		int[] pickedMove = moveList.get(rand.nextInt(moveList.size()));
		targetRow = pickedMove[0];
		targetCol = pickedMove[1];
		/*
		 * Make sure the target move index does not exceed the board size before
		 * creating the new move object.
		 */
		if (targetRow > -1 && targetCol > -1 && targetRow < boardSize && targetCol < boardSize) {
			computerMove = new Move(targetRow, targetCol, getPlayerColor());
		}
		return computerMove;
	}

	/**
	 * A helper method that uses score table and difficulty level of the game to
	 * generate a list of possible moves for the computer player.
	 * 
	 * @param board     current board of the game.
	 * @param boardSize the size of the board.
	 * @return an arrayList with a number of array items of possible moves.
	 */
	private ArrayList<int[]> generateMoveList(Stone[][] board, int boardSize) {
		ArrayList<int[]> moveList = new ArrayList<int[]>();
		/*
		 * Number of times the for loop will run is determined by the level of
		 * difficulty.
		 */
		for (int counter = 0; counter < levelMap.get(difficultyLevel); counter++) {
			// Generate next highest move and add it to the move list.
			int[] moveCoord = getHighestMoveCoord(moveList, board, boardSize);
			moveList.add(moveCoord);
		}
		return moveList;
	}

	/**
	 * Get the highest score coord in the current score table.
	 * 
	 * @param moveList  current list of possible moves.
	 * @param board     current board of the game.
	 * @param boardSize the size of the board.
	 * @return the current highest score coord data in the score table.
	 */
	private int[] getHighestMoveCoord(ArrayList<int[]> moveList, Stone[][] board, int boardSize) {
		int[][] scoreTable = calculateScores(board, boardSize);
		/*
		 * Set all the coords in the moveList to be -1, so next loop won't repeat the
		 * same index positions.
		 */
		for (int[] move : moveList) {
			scoreTable[move[0]][move[1]] = -1;
		}
		int maxScore = 0;
		int goalRow = -1;
		int goalCol = -1;
		/*
		 * Loop through all the scores in the score table and get the empty coordinate
		 * with the highest score in the board.
		 */
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (board[i][j] == Stone.EMPTY && scoreTable[i][j] > maxScore) {
					maxScore = scoreTable[i][j];
					goalRow = i;
					goalCol = j;
				}
			}
		}
		// Put row and column indices in an array and return it.
		int[] moveCoord = new int[2];
		moveCoord[0] = goalRow;
		moveCoord[1] = goalCol;
		return moveCoord;
	}

	/**
	 * Initiate a 2D array with initial score, higher score is given toward the
	 * center of the board. The array size is the same size as the board of the
	 * game.
	 * 
	 * @param board     current board of the game.
	 * @param boardSize the size of the board.
	 * @return a score table with initial score based on each coord's position with
	 *         respective to the board border.
	 */
	private int[][] initScoreTable(Stone[][] board, int boardSize) {
		int[][] scoreTable = new int[boardSize][boardSize];
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				int mid = boardSize / 2;
				int min = 0;
				int maxIdx = boardSize - 1;
				if (i <= mid && j <= mid) {
					min = i < j ? i : j;
				} else if (i > mid && j <= mid) {
					min = maxIdx - i < j ? maxIdx - i : j;
				} else if (i <= mid && j > mid) {
					min = i < maxIdx - j ? i : maxIdx - j;
				} else {
					min = maxIdx - i < maxIdx - j ? maxIdx - i : maxIdx - j;
				}
				scoreTable[i][j] = min;
			}
		}
		return scoreTable;
	}

	/**
	 * Helper method to count stones in left to right direction and update score
	 * table accordingly.
	 * 
	 * @param i     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countLeftRightStones(int i, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int k = j;
		while (k < j + 5) {
			if (board[i][k] == Stone.BLACK) numOfBlack++;
			else if (board[i][k] == Stone.WHITE) numOfWhite++;
			k++;
		}
		tempScore = tupleScore(numOfBlack, numOfWhite);
		// Add the score to each of the five coordinates in the tuple
		for (k = j; k < j + 5; k++) {
			score[i][k] += tempScore;
		}
	}

	/**
	 * Helper method to count stones in top to bottom direction and update score
	 * table accordingly.
	 * 
	 * @param i     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countTopBottomStones(int i, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int k = j;
		while (k < j + 5) {
			if (board[k][i] == Stone.BLACK) numOfBlack++;
			else if (board[k][i] == Stone.WHITE) numOfWhite++;
			k++;
		}
		tempScore = tupleScore(numOfBlack, numOfWhite);
		for (k = j; k < j + 5; k++) {
			score[k][i] += tempScore;
		}
	}

	/**
	 * Helper method to count stones in top left to bottom right direction to cover
	 * bottom left triangle of the board and update score table accordingly.
	 * 
	 * @param k     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countTLBRStones(int k, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int m = k;
		int n = j;
		while (m < k + 5) {
			if (board[m][n] == Stone.BLACK) numOfBlack++;
			else if (board[m][n] == Stone.WHITE) numOfWhite++;
			m++;
			n++;
		}
		if (m == k + 5) {
			tempScore = tupleScore(numOfBlack, numOfWhite);
			for (m = k, n = j; m < k + 5; m++, n++) {
				score[m][n] += tempScore;
			}
		}
	}

	/**
	 * Helper method to count stones in bottom right to top left direction to cover
	 * top right triangle of the board and update score table accordingly.
	 * 
	 * @param k     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countTLBRStonesReversed(int k, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int m = k;
		int n = j;
		while (m > k - 5) {
			if (board[m][n] == Stone.BLACK) numOfBlack++;
			else if (board[m][n] == Stone.WHITE) numOfWhite++;
			m--;
			n--;
		}
		if (m == k - 5) {
			tempScore = tupleScore(numOfBlack, numOfWhite);
			for (m = k, n = j; m > k - 5; m--, n--) {
				score[m][n] += tempScore;
			}
		}
	}

	/**
	 * Helper method to count stones in bottom left to top right direction to cover
	 * top left triangle of the board and update score table accordingly.
	 * 
	 * @param k     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countBLTRStones(int k, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int m = k;
		int n = j;
		while (m > k - 5) {
			if (board[m][n] == Stone.BLACK) numOfBlack++;
			else if (board[m][n] == Stone.WHITE) numOfWhite++;
			m--;
			n++;
		}
		if (m == k - 5) {
			tempScore = tupleScore(numOfBlack, numOfWhite);
			for (m = k, n = j; m > k - 5; m--, n++) {
				score[m][n] += tempScore;
			}
		}
	}

	/**
	 * Helper method to count stones in Top right to bottom left direction to cover
	 * bottom right triangle of the board and update score table accordingly.
	 * 
	 * @param k     counter for outer loop.
	 * @param j     counter for inner loop.
	 * @param score current score table.
	 * @param board current board of the game.
	 */
	private void countBLTRStonesReversed(int k, int j, int[][] score, Stone[][] board) {
		int numOfBlack = 0; // number of black in a tuple
		int numOfWhite = 0; // number of white in a tuple
		int tempScore = 0; // temp score for a tuple
		int m = k;
		int n = j;
		while (m < k + 5) {
			if (board[m][n] == Stone.BLACK) numOfBlack++;
			else if (board[m][n] == Stone.WHITE) numOfWhite++;
			m++;
			n--;
		}
		if (m == k + 5) {
			tempScore = tupleScore(numOfBlack, numOfWhite);
			for (m = k, n = j; m < k + 5; m++, n--) {
				score[m][n] += tempScore;
			}
		}
	}

	/**
	 * The computer move algorithm was adapted from Chess class line 93 to 337 in a
	 * post by ccnuacmhdu on 2018-10-20 in a CSDN blog here:
	 * https://blog.csdn.net/ccnuacmhdu/article/details/83152946 Because the google
	 * translated link will automatically redirect to CSDN home page, a translated
	 * page url will not be provided here, but readers are welcome to use
	 * translation tools to refer to the original algorithm. Below is a brief
	 * description of the algorithm: The winning condition of the gomuku game is to
	 * form a line of five same color stones. Therefore, for each five connected
	 * coordinates (so-called a five-tuple) in the board, we will first determine
	 * its number of black stones and number of white stones, and use the helper
	 * method tupleScore to give this tuple a score. This score will be added to
	 * each of these five coordinates. Repeat this process until all tuples in the
	 * board populate a score and add to the coordinates, hence the score of each
	 * coordinate is the sum of the scores of all five-tuples that contain this
	 * coordinate. The coordinate with the highest score is the best next move of
	 * the current game.
	 * 
	 * @param board     current board of the game.
	 * @param boardSize the size of the board.
	 * @return a 2D array that contains a score for each coordinate in the board.
	 */
	public int[][] calculateScores(Stone[][] board, int boardSize) {
		// Initialize a 2D array with more scores towards center.
		int[][] score = initScoreTable(board, boardSize);

		// Left to right (covers all horizontal tuples)
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize - 4; j++) {
				countLeftRightStones(i, j, score, board);
			}
		}
		// Top to bottom (covers all vertical tuples)
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize - 4; j++) {
				countTopBottomStones(i, j, score, board);
			}
		}
		// Top left to bottom right (covers all diagonal tuples in bottom left triangle)
		for (int i = 0; i < boardSize - 4; i++) {
			for (int j = 0, k = i; j < boardSize - 4 && k < boardSize - 4; j++, k++) {
				countTLBRStones(k, j, score, board);
			}
		}
		// Bottom right to top left (covers all diagonal tuples in top right triangle)
		for (int i = boardSize - 2; i >= 4; i--) {
			for (int j = boardSize - 1, k = i; j >= 4 && k >= 4; j--, k--) {
				countTLBRStonesReversed(k, j, score, board);
			}
		}
		// Bottom left to top right (covers all diagonal tuples in top left triangle)
		for (int i = boardSize - 1; i >= 4; i--) {
			for (int j = 0, k = i; j < boardSize - 4 && k >= 4; j++, k--) {
				countBLTRStones(k, j, score, board);
			}
		}
		// Top right to bottom left (covers all diagonal tuples in bottom right triangle)
		for (int i = 1; i < boardSize - 4; i++) {
			for (int j = boardSize - 1, k = i; j >= 4 && k < boardSize - 4; j--, k++) {
				countBLTRStonesReversed(k, j, score, board);
			}
		}
		return score;
	}

	/**
	 * A helper method that will evaluate a tuple and give it a score.
	 * 
	 * @param numOfBlack number of black stones in a tuple.
	 * @param numOfWhite number of white stones in a tuple.
	 * @return a score based on number of black and white stones.
	 */
	private int tupleScore(int numOfBlack, int numOfWhite) {
		// The worst case: a tuple contains at least one black and at least one white
		if (numOfBlack > 0 && numOfWhite > 0) return 0;
		// The tuple is empty
		if (numOfBlack == 0 && numOfWhite == 0) return 7;
		// The tuple only contains one black
		if (numOfBlack == 1) return 15;
		// The tuple contains two black and no white.
		if (numOfBlack == 2) return 400;
		// The tuple contains three black and no white.
		if (numOfBlack == 3) return 1800;
		// The tuple contains four black and no white.
		if (numOfBlack == 4) return 100000;
		// The tuple only contains one white.
		if (numOfWhite == 1) return 35;
		// The tuple contains two white and no black.
		if (numOfWhite == 2) return 800;
		// The tuple contains three white and no black.
		if (numOfWhite == 3) return 15000;
		// The tuple contains four white and no black.
		if (numOfWhite == 4) return 800000;
		// The none of above cases, this indicates something went wrong in the program.
		return -1;
	}
}