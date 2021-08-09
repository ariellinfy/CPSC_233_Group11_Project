package application;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.time.Duration;
import model.*;
import javafx.beans.binding.Bindings;

/**
 * A view that displays the game board, and handles board-related events such as
 * stone placements.
 * 
 * @author Fu-Yin Lin, Justin Chua
 *
 */
public class OnGameController {
	private static final int LINE_SPACING = 40;
	private int boardSize = 15;
	private boolean blackTurn = true;
	private GomokuGUI app;
	private GameConfiguration config;
	private Timeline blackTimeline = new Timeline();
	private Timeline whiteTimeline = new Timeline();

	// A layout container containing the grid game board.
	@FXML
	private Pane paneBoard;

	@FXML
	private StackPane paneBoardArea;

	@FXML
	private Label labelBlackTime;

	@FXML
	private Label labelWhiteTime;

	@FXML
	private Label labelBlackName;

	@FXML
	private Label labelWhiteName;

	@FXML
	private Button buttonUndo;

	/**
	 * Set boardSize and draw board based on user selected size in the start menu.
	 * 
	 * @param boardSize the specified board size selected by the user.
	 */
	void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
		drawBoard();
	}

	/**
	 * Method used to link this controller with the GomokuGUI.
	 * 
	 * @param app the instance of GomokuGUI when running the program.
	 */
	void linkWithApplication(GomokuGUI app) {
		this.app = app;
		this.config = app.getGameConfiguration();
		labelBlackName.setText(app.getPlayerBlack().getPlayerName());
		labelWhiteName.setText(app.getPlayerWhite().getPlayerName());
		if (config.getUndo()) {
			buttonUndo.setDisable(false);
		}
		setupCountdownTimer(blackTimeline, labelBlackTime);
		setupCountdownTimer(whiteTimeline, labelWhiteTime);
		blackTimerContinue();
		/*
		 * If playerBlack (which goes first by default) is an instance of
		 * ComputerPlayer, the firstMove() method is invoked.
		 */
		if (app.getPlayerBlack() instanceof ComputerPlayer) {
			firstMove();
		}
	}

	// https://asgteach.com/2011/10/javafx-animation-and-binding-simple-countdown-timer-2/
	// https://www.coder.work/article/5519239
	private void setupCountdownTimer(Timeline timeline, Label label) {
		ObjectProperty<Duration> timeLeft = new SimpleObjectProperty<>();
		timeLeft.set(Duration.ofMinutes(config.getGameTime()));
		label.textProperty()
				.bind(Bindings.createStringBinding(() -> getTimeStringFromDuration(timeLeft.get()), timeLeft));
		timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(1), e -> updateTimer(timeLeft)));
		timeline.setCycleCount(Timeline.INDEFINITE);
	}

	private void updateTimer(ObjectProperty<Duration> timeLeft) {
		timeLeft.set(timeLeft.get().minusSeconds(1));
		if (timeLeft.get().isZero()) {
			if (blackTurn) {
				blackTimeline.stop();
				app.gameOver(Result.BLACK);
			} else {
				whiteTimeline.stop();
				app.gameOver(Result.WHITE);
			}
		}
	}

	private static String getTimeStringFromDuration(Duration duration) {
		int min = (int) (duration.getSeconds() / 60);
		int sec = (int) (duration.getSeconds() % 60);
		String time = sec < 10 ? min + ":0" + sec : min + ":" + sec;
		return time;
	}

	private void continueTimer(Timeline timeline) {
		timeline.play();
	}

	private void pauseTimer(Timeline timeline) {
		timeline.stop();
	}

	private void blackTimerContinue() {
		continueTimer(blackTimeline);
		pauseTimer(whiteTimeline);
	}

	private void whiteTimerContinue() {
		continueTimer(whiteTimeline);
		pauseTimer(blackTimeline);
	}

	private void stopAllTimers() {
		labelBlackTime.textProperty().unbind();
		labelWhiteTime.textProperty().unbind();
		blackTimeline.stop();
		whiteTimeline.stop();
	}

	/**
	 * Method that is responsible for drawing the grid game board in the OnGame
	 * scene.
	 */
	private void drawBoard() {
		int boardLength = LINE_SPACING * (boardSize - 1);
		// The size of the layout container is set equal to boardLength.
		paneBoard.setMinSize(boardLength, boardLength);
		paneBoard.setPrefSize(boardLength, boardLength);
		paneBoard.setMaxSize(boardLength, boardLength);
		/*
		 * For loop creates and adds horizontal/vertical lines to "paneBoard" (layout
		 * container) to produce the grid game board.
		 */
		for (int i = 0; i < boardSize; i++) {
			Line hLine = new Line(0, LINE_SPACING * i, boardLength, LINE_SPACING * i);
			Line vLine = new Line(LINE_SPACING * i, 0, LINE_SPACING * i, boardLength);
			paneBoard.getChildren().add(hLine);
			paneBoard.getChildren().add(vLine);
		}
		/*
		 * drawPoint() is invoked at the end of the method to draw a series of dots on
		 * the completed grid game board.
		 */
		drawPoint();
	}

	/**
	 * Method that is used to draw a series of dots on the game board. These dots
	 * are meant to mimic the 5 or 9 dots commonly found on traditional Gomoku game
	 * boards.
	 */
	private void drawPoint() {
		/*
		 * The location of these dots differs depending on the board size selected by
		 * the user.
		 */
		int top = 3 * LINE_SPACING;
		int center = Math.round(boardSize / 2) * LINE_SPACING;
		int bottom = (boardSize - 4) * LINE_SPACING;
		if (boardSize < 13) {
			top = 2 * LINE_SPACING;
			bottom = (boardSize - 3) * LINE_SPACING;
		}
		/*
		 * Regardless of board size, a center dot surrounded by 4 dots diagonally is
		 * drawn on the board.
		 */
		Circle circle1 = new Circle(top, top, 4);
		Circle circle2 = new Circle(bottom, top, 4);
		Circle circle3 = new Circle(center, center, 4);
		Circle circle4 = new Circle(top, bottom, 4);
		Circle circle5 = new Circle(bottom, bottom, 4);
		paneBoard.getChildren().addAll(circle1, circle2, circle3, circle4, circle5);
		/*
		 * For board sizes larger than 15, 4 additional diagonal dots are drawn on the
		 * board.
		 */
		if (boardSize > 15) {
			Circle circle6 = new Circle(center, top, 4);
			Circle circle7 = new Circle(top, center, 4);
			Circle circle8 = new Circle(bottom, center, 4);
			Circle circle9 = new Circle(center, bottom, 4);
			paneBoard.getChildren().addAll(circle6, circle7, circle8, circle9);
		}
	}

	/**
	 * Method that is used to draw the stone on the game board whenever a stone is
	 * placed by the user/computer.
	 * 
	 * @param move a Move object containing the coordinates of where the stone is
	 *             placed.
	 */
	private void drawStone(Move move) {
		/*
		 * To calculate the x and y distance of the stone with respective to the pane.
		 */
		int y = move.getRow() * LINE_SPACING;
		int x = move.getCol() * LINE_SPACING;
		Circle circle = new Circle(x, y, 17.5);
		/*
		 * If/else statement sets the color of the stone dependent on whether the move
		 * was made by player Black or White.
		 */
		if (move.getStone() == Stone.BLACK) {
			circle.setStroke(Color.BLACK);
			circle.setFill(Color.BLACK);
		} else {
			circle.setStroke(Color.WHITE);
			circle.setFill(Color.WHITE);
		}
		/*
		 * For aesthetic purposes, a drop shadow is added to the circles to portray a
		 * more reflective, stone-like effect.
		 */
		DropShadow ds = new DropShadow();
		ds.setOffsetX(2.0);
		ds.setOffsetY(2.0);
		circle.setEffect(ds);
		paneBoard.getChildren().add(circle);
	}

	private void removeStone(Move move) {
		paneBoard.getChildren().remove(paneBoard.getChildren().size() - 1);
	}

	/**
	 * Method that handles the location of mouse clicks made by the user, and passes
	 * these coordinates to stone placement/move related methods.
	 * 
	 * @param e a MouseEvent object that is invoked whenever a mouse click occurs.
	 */
	@FXML
	private void onNextMove(MouseEvent e) {
		int boardLength = LINE_SPACING * (boardSize - 1);
		double x = e.getX();
		double y = e.getY();
		/*
		 * If condition checks to see if the mouse click was valid (i.e. clicked within
		 * the game board). If not, the method is exited using return.
		 */
		if (x < 0 || x > boardLength + 20 || y < 0 || y > boardLength + 20) {
			return;
		}
		/*
		 * The mouse click is rounded to the nearest intersection at which a stone can
		 * be placed.
		 */
		int col = (int) Math.round(x / LINE_SPACING);
		int row = (int) Math.round(y / LINE_SPACING);
		System.out.println(row + ", " + col);
		try {
			if (blackTurn) {
				nextBlackMove(row, col, app.getPlayerBlack());
			} else {
				nextWhiteMove(row, col, app.getPlayerWhite());
			}
		} catch (InvalidPlacementException ex) {
//			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Method that is used to invoke the nextAIMove() method when the computer is
	 * selected to go first by the user.
	 */
	private void firstMove() {
		nextAIMove(app.getPlayerBlack(), app.getPlayerWhite());
		whiteTimerContinue();
	}

	/**
	 * Method that is responsible for updating the board and move-related instance
	 * variables with the new move made by the Computer.
	 * 
	 * @param nextPlayer    a Player object of the next player.
	 * @param currentPlayer a Player object of the current player.
	 */
	private void nextAIMove(Player nextPlayer, Player currentPlayer) {
		// If conditions checks if "nextPlayer" is an instance of ComputerPlayer.

		if (nextPlayer instanceof ComputerPlayer) {
			Move move = nextPlayer.getMove(config);
			placeMove(move, nextPlayer, currentPlayer);
			if (nextPlayer.getPlayerColor() == Stone.BLACK) {
				whiteTimerContinue();
			} else {
				blackTimerContinue();
			}
		}
	}

	/**
	 * Method used to handle the next move made by player Black.
	 * 
	 * @param row         the row index of the stone.
	 * @param col         the column index of the stone.
	 * @param playerBlack a Player object for player Black, containing related info
	 *                    such as num of moves/stone color.
	 * @throws InvalidPlacementException if the move is invalid.
	 */
	private void nextBlackMove(int row, int col, Player playerBlack) throws InvalidPlacementException {
		Move blackMove = playerBlack.getMove(config, row, col);
		Player opponent = app.getPlayerWhite();
		if (blackMove != null) {
			placeMove(blackMove, playerBlack, opponent);
			whiteTimerContinue();
			nextAIMove(opponent, playerBlack);
		} else {
			throw new InvalidPlacementException("Invalid move, please try again");
		}
	}

	/**
	 * Method that is used to handle the next move made by player White.
	 * 
	 * @param row         the row index of the stone.
	 * @param col         the column index of the stone.
	 * @param playerWhite a Player object for player White, containing related info
	 *                    such as num of moves/stone color.
	 * @throws InvalidPlacementException if the move is invalid.
	 */
	private void nextWhiteMove(int row, int col, Player playerWhite) throws InvalidPlacementException {
		Move whiteMove = playerWhite.getMove(config, row, col);
		Player opponent = app.getPlayerBlack();
		if (whiteMove != null) {
			placeMove(whiteMove, playerWhite, opponent);
			blackTimerContinue();
			nextAIMove(opponent, playerWhite);
		} else {
			throw new InvalidPlacementException("Invalid move, please try again");
		}
	}

	/**
	 * Method that is responsible for updating the board and move-related instance
	 * variables with the new move made by the user.
	 * 
	 * @param move          a Move object containing information
	 * @param currentPlayer a Player object of the current player.
	 * @param opponent      a Player object of the opposing player.
	 */
	private void placeMove(Move move, Player currentPlayer, Player opponent) {
		/*
		 * Current player's "validMoveList" and "numOfMoves" instance variables are
		 * updated with the latest valid move.
		 */
		currentPlayer.getAllValidMoves().add(move);
		currentPlayer.incrementMoveCount();
		/*
		 * updateBoard() is invoked on instance variable "config" to update the board
		 * (i.e. 2D array) with new move.
		 */
		config.updateBoard(move);
		// drawstone() is invoked to draw the stone onto the OnGame scene.
		drawStone(move);
		// Winning lines are checked, as well as if the board is full.
		Result roundResult = config.checkWinningLine(move);
		Result checkNumOfMoves = config.isBoardFull(currentPlayer, opponent);
		/*
		 * Only if "Result.CONTINUE" is returned from both checkWinningLine() and
		 * isBoardFull(), the game continues and it becomes the other player's turn.
		 */
		if (roundResult == Result.CONTINUE && checkNumOfMoves == Result.CONTINUE) {
			blackTurn = !blackTurn;
			/*
			 * If the board has no empty spot, the game will end with a draw result.
			 */
		} else if (checkNumOfMoves == Result.DRAW) {
			stopAllTimers();
			app.gameOver(checkNumOfMoves);
		} else {
			/*
			 * For all the other cases (ie. if round result is not an enum Continue), the
			 * game will reach the end with the round result.
			 */
			stopAllTimers();
			app.gameOver(roundResult);
		}
	}

	@FXML
	private void onUndo(ActionEvent event) {
		Player playerBlack = app.getPlayerBlack();
		Player playerWhite = app.getPlayerWhite();
		try {
			ArrayList<Move> undoMoves = config.undoMove(blackTurn, playerBlack, playerWhite);
			for (Move move : undoMoves) {
				removeStone(move);
			}
		} catch (InvalidUndoException e) {
			// Need to add warning message box to show the error
			System.out.println(e.getMessage());
//			e.printStackTrace();
		}
	}

	@FXML
	private void onDraw(ActionEvent event) {
		stopAllTimers();
		app.gameOver(Result.DRAW);
	}

	/**
	 * Method that exits the game by terminating the application.
	 * 
	 * @param event an ActionEvent object that is invoked whenever the "Quit" button
	 *              is pressed by the user.
	 */
	@FXML
	private void onExitGame(ActionEvent event) {
		// exitGame() is invoked on instance variable "app" to terminate the program.
		app.exitGame();
	}

	/**
	 * Method that restarts the game by returning the user to the start menu.
	 * 
	 * @param event an ActionEvent object that is invoked whenever the "Start new
	 *              game" button is pressed by the user.
	 */
	@FXML
	private void onRestart(ActionEvent event) {
		/*
		 * restartGame() is invoked on instance variable "app" to return the user to the
		 * start menu.
		 */
		app.restartGame();
	}

	/**
	 * Initialize method is called once to implement this controller when the
	 * OnGameView.fxml file is completely loaded.
	 */
	@FXML
	private void initialize() {
		assert labelBlackName != null
				: "fx:id=\"labelBlackName\" was not injected: check your FXML file 'OnGameView.fxml'.";
		assert paneBoard != null : "fx:id=\"paneBoard\" was not injected: check your FXML file 'OnGameView.fxml'.";
		assert labelBlackTime != null
				: "fx:id=\"labelBlackTime\" was not injected: check your FXML file 'OnGameView.fxml'.";
		assert labelWhiteTime != null
				: "fx:id=\"labelWhiteTime\" was not injected: check your FXML file 'OnGameView.fxml'.";
		assert paneBoardArea != null
				: "fx:id=\"paneBoardArea\" was not injected: check your FXML file 'OnGameView.fxml'.";
		assert labelWhiteName != null
				: "fx:id=\"labelWhiteName\" was not injected: check your FXML file 'OnGameView.fxml'.";
	}
}