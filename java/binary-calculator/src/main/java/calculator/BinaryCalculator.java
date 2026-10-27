package calculator;

import calculator.ui.CalculatorController;
import javafx.application.Application;
import javafx.stage.Stage;

public class BinaryCalculator extends Application {

    @Override
    public void start(Stage primaryStage) {
        CalculatorController controller = new CalculatorController();
        var scene = controller.buildScene();
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Какулятор");
        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(300);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
