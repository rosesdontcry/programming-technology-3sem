package calculator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BinaryCalculator extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Привет, JavaFX!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Бинарный калькулятор");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}