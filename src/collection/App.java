package collection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // charge le fichier FXML de la vue principale
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/collection/view/main-view.fxml")
        );

        // crée la scène
        Scene scene = new Scene(loader.load(), 1000, 650);

        stage.setTitle("Collection musicale"); // titre
        stage.setScene(scene); // lier la scène
        stage.show(); // afficher
    }

    public static void main(String[] args) {
        launch(args);
    }
}