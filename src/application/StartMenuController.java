package application;

import java.io.File;
import java.util.function.UnaryOperator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.converter.IntegerStringConverter;
import model.*;

/**
 * A view for users to set the setting and start a game.
 * 
 * @author Fu-Yin Lin
 *
 */
public class StartMenuController {
	private GomokuGUI app;

	@FXML
	private BorderPane borderPaneArea;

	@FXML
	private ToggleGroup opponentGroup;

	@FXML
	private ToggleGroup difficultyGroup;

	@FXML
	private ToggleGroup userColorGroup;

	@FXML
	private ToggleGroup boardSizeGroup;

	@FXML
	private HBox hBoxDifficulty;

	@FXML
	private Spinner<Integer> spinnerGameTime;

	@FXML
	private CheckBox checkBoxUndo;

	@FXML
	private Button muteButton;

	@FXML
	private ImageView volumeImage;

	/**
	 * Grant access to the GomokuGUI main controller.
	 * 
	 * @param app the instance of GomokuGUI when running the program.
	 */
	void linkWithApplication(GomokuGUI app) {
		this.app = app;
		// Update image for background music button.
		if (app.getBackgroundPlayer().getStatus() == MediaPlayer.Status.STOPPED) {
			volumeImage.setImage(new Image(new File("src/resources/Volume-Muted-Icon.png").toURI().toString()));
		} else {
			volumeImage.setImage(new Image(new File("src/resources/Volume-Icon.png").toURI().toString()));
		}
	}

	/**
	 * Setup opponent player based on user selected options.
	 * 
	 * @return either a HumanPlayer or a ComputerPlayer as an opponent player.
	 */
	private Player setupOpponent() {
		Player opponent = new ComputerPlayer(Level.MEDIUM);
		if (opponentGroup.getSelectedToggle().getUserData().toString().equals("Computer")) {
			String difficultyLevel = difficultyGroup.getSelectedToggle().getUserData().toString();
			if (difficultyLevel.equals("Hard")) {
				opponent = new ComputerPlayer(Level.HARD);
			} else if (difficultyLevel.equals("Easy")) {
				opponent = new ComputerPlayer(Level.EASY);
			}
		} else {
			opponent = new HumanPlayer();
		}
		return opponent;
	}

	/**
	 * Setup colors for both players based on user selected color.
	 * 
	 * @param opponent the player to play against to.
	 */
	private void chooseColor(Player opponent) {
		if (userColorGroup.getSelectedToggle().getUserData().toString().equals("Black")) {
			app.setPlayerBlack(new HumanPlayer(Stone.BLACK));
			opponent.setPlayerColor(Stone.WHITE);
			app.setPlayerWhite(opponent);
		} else {
			app.setPlayerWhite(new HumanPlayer(Stone.WHITE));
			opponent.setPlayerColor(Stone.BLACK);
			app.setPlayerBlack(opponent);
		}
	}

	/**
	 * Updates variables in config object: boardSize, allowUndo, and gameTime.
	 */
	private void setupGameConfig() {
		GameConfiguration config = app.getGameConfiguration();
		Board board = new Board((Integer) boardSizeGroup.getSelectedToggle().getUserData());
		config.setChessBoard(board);
		config.setUndo(!checkBoxUndo.isSelected());
		config.setGameTime(spinnerGameTime.getValue());
	}

	/**
	 * Method that updates game setting based on user selected options and switch to
	 * OnGameView to start the game.
	 * 
	 * @param event an action event invokes when user clicked on "Start" button.
	 */
	@FXML
	private void onStartGame(ActionEvent event) {
		// Menu button sound is played when the start button is selected.
		app.playMenuSound();
		Player opponent = setupOpponent();
		chooseColor(opponent);
		setupGameConfig();
		app.playGame();
	}

	/**
	 * Method that toggles the background music on and off based on user choice.
	 * 
	 * @param event an action event invokes when user clicked on "Volume" button.
	 */
	@FXML
	private void onMute(ActionEvent event) {
		app.playMenuSound();
		if (app.getBackgroundPlayer().getStatus() == MediaPlayer.Status.STOPPED) {
			volumeImage.setImage(new Image(new File("src/resources/Volume-Icon.png").toURI().toString()));
			app.playBackgroundMusic();
		} else {
			volumeImage.setImage(new Image(new File("src/resources/Volume-Muted-Icon.png").toURI().toString()));
			app.stopBackgroundMusic();
		}
	}

	/**
	 * Method that ends the application when "Quit" button is clicked.
	 * 
	 * @param event an action event invokes when user clicked on "Quit" button.
	 */
	@FXML
	private void onExitGame(ActionEvent event) {
		app.exitGame();
	}

	/**
	 * Set user data to each toggle in a toggle group.
	 * 
	 * @param toggleGroup a toggle group that contains a number of toggle buttons.
	 */
	private void setToggleButton(ToggleGroup toggleGroup) {
		if (toggleGroup.equals(opponentGroup)) {
			ObservableList<Toggle> opponent = toggleGroup.getToggles();
			opponent.get(0).setUserData("Computer");
			opponent.get(1).setUserData("Human");
		} else if (toggleGroup.equals(difficultyGroup)) {
			ObservableList<Toggle> levels = toggleGroup.getToggles();
			levels.get(0).setUserData("Easy");
			levels.get(1).setUserData("Medium");
			levels.get(2).setUserData("Hard");
		} else if (toggleGroup.equals(userColorGroup)) {
			ObservableList<Toggle> playerColors = toggleGroup.getToggles();
			playerColors.get(0).setUserData("Black");
			playerColors.get(1).setUserData("White");
		} else if (toggleGroup.equals(boardSizeGroup)) {
			ObservableList<Toggle> boardSizes = toggleGroup.getToggles();
			boardSizes.get(0).setUserData(9);
			boardSizes.get(1).setUserData(13);
			boardSizes.get(2).setUserData(15);
			boardSizes.get(3).setUserData(19);
		}
	}

	/**
	 * Set user data to each toggle button and add listener to a toggle group.
	 * 
	 * @param toggleGroup a group of toggles whose selected variables should be
	 *                    managed such that only a single Toggle within the
	 *                    ToggleGroup may be selected at any one time.
	 */
	private void initToggleListener(ToggleGroup toggleGroup) {
		// Set user data to each toggle in a toggle group.
		setToggleButton(toggleGroup);
		// Add listener to a toggle group to monitor changes within the group.
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, final Toggle oldValue,
					final Toggle newValue) {
				// Menu button sound is played whenever each set of toggle buttons update.
				app.playMenuSound();
				/*
				 * Prohibits a toggle group with no selected toggle. Clicking on a toggle twice
				 * or more will set the selected value to previous selected toggle.
				 */
				if (newValue == null) {
					toggleGroup.selectToggle(oldValue);
				}
				/*
				 * Disable difficulty toggle group if opponent is chosen to be a human player,
				 * reset otherwise.
				 */
				if (opponentGroup.getSelectedToggle().getUserData().toString().equals("Human")) {
					hBoxDifficulty.setDisable(true);
				} else {
					hBoxDifficulty.setDisable(false);
				}
			}
		});
	}

	/**
	 * Add listener to the game time spinner so that only numeric input is allowed.
	 */
	private void initSpinnerListener() {
		// Allows only integer number as input.
		UnaryOperator<TextFormatter.Change> filter = change -> {
			if (change.isAdded() || change.isReplaced()) {
				change = !change.getText().matches("\\d+") ? null : change;
			}
			return change;
		};
		TextFormatter<Integer> gameTimeFormatter = new TextFormatter<Integer>(new IntegerStringConverter(), 5, filter);
		spinnerGameTime.getEditor().setTextFormatter(gameTimeFormatter);
		// Set spinner default to 5 min if spinner field is empty.
		spinnerGameTime.valueProperty().addListener((observable, oldValue, newValue) -> {
			// Menu button sound is played whenever spinner value is updated.
			app.playMenuSound();
			if (newValue == null) {
				spinnerGameTime.getValueFactory().setValue(5);
			}
		});
	}

	/**
	 * Add listener to undo checkbox to play sound effect when user clicks on the
	 * checkbox.
	 */
	private void initUndoListener() {
		checkBoxUndo.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// Menu sound is played when undo button is selected/deselected.
				app.playMenuSound();
			}
		});
	}

	/**
	 * Initialize method that is invoked once to set up this controller once the
	 * StartMenu.fxml file has been loaded. This method also setup event listener
	 * and update background image.
	 */
	@FXML
	private void initialize() {
		assert boardSizeGroup != null
				: "fx:id=\"boardSizeGroup\" was not injected: check your FXML file 'StartMenu.fxml'.";
		assert userColorGroup != null
				: "fx:id=\"userColorGroup\" was not injected: check your FXML file 'StartMenu.fxml'.";
		assert opponentGroup != null
				: "fx:id=\"opponentGroup\" was not injected: check your FXML file 'StartMenu.fxml'.";
		assert difficultyGroup != null
				: "fx:id=\"difficultyGroup\" was not injected: check your FXML file 'StartMenu.fxml'.";
		// Add listeners to controls.
		initToggleListener(opponentGroup);
		initToggleListener(difficultyGroup);
		initToggleListener(userColorGroup);
		initToggleListener(boardSizeGroup);
		initSpinnerListener();
		initUndoListener();
		// Set scene background image.
		Image backgroundImage = new Image("file:src/resources/Light-Wood-Background.png");
		BackgroundSize bSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
		Background background = new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bSize));
		borderPaneArea.setBackground(background);
	}
}
