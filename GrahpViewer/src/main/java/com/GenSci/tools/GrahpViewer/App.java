package com.GenSci.tools.GrahpViewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
    	scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("GA Viewer Type Axelrod ver 1.0");
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

}