package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Instantiate the Welcome Screen and display it.
        WelcomeScreen welcomeScreen = new WelcomeScreen();
        welcomeScreen.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args); 
    }
}