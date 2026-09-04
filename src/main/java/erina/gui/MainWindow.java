package erina.gui;

import erina.Erina;
import erina.Ui;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main window: turns what the user types into a reply from
 * Erina, and shows both as dialog boxes.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Erina erina;

    private final Image userImage =
            new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private final Image erinaImage =
            new Image(this.getClass().getResourceAsStream("/images/DaErina.png"));

    /** Keeps the newest dialog box in view as the conversation grows. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Sets the chatbot this window talks to, and shows its opening messages.
     *
     * @param erina the chatbot to use
     */
    public void setErina(Erina erina) {
        this.erina = erina;

        dialogContainer.getChildren().add(
                DialogBox.getErinaDialog(Ui.GREETING, erinaImage));

        // A save file that could not be read is worth saying out loud, in the
        // same place the user reads everything else.
        String loadError = erina.getLoadError();
        if (loadError != null) {
            dialogContainer.getChildren().add(
                    DialogBox.getErinaDialog(loadError, erinaImage));
        }
    }

    /**
     * Shows the user's input and Erina's reply, then clears the input box.
     *
     * <p>After a {@code bye} command the window closes, but only once the
     * farewell has been shown, so the user sees it.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.trim().isEmpty()) {
            return;
        }

        String response = erina.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getErinaDialog(response, erinaImage)
        );
        userInput.clear();

        if (erina.isExit()) {
            // Give the farewell a moment on screen before the window goes.
            PauseTransitionFactory.runAfterPause(Platform::exit);
        }
    }
}
