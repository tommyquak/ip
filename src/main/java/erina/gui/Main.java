package erina.gui;

import java.io.IOException;
import java.nio.file.Path;

import erina.Erina;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The JavaFX application: builds the window and hands it an {@link Erina}.
 */
public class Main extends Application {
    /** Where the task list is kept between runs. */
    private static final Path SAVE_FILE = Path.of("data", "erina.txt");

    /** The chatbot the window talks to. */
    private final Erina erina = new Erina(SAVE_FILE);

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            stage.setTitle("Erina");
            stage.setMinHeight(220.0);
            stage.setMinWidth(417.0);
            fxmlLoader.<MainWindow>getController().setErina(erina);
            stage.show();
        } catch (IOException e) {
            // The FXML is packaged with the application, so failing to load it
            // means the build is broken rather than anything the user did.
            System.err.println("Could not load the main window: " + e.getMessage());
        }
    }
}
