package calculator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BinaryCalculator extends Application {
    private boolean syncing;

    @Override
    public void start(Stage stage) {
        TextField binary = new TextField();
        binary.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[01]*") ? change : null));

        TextField decimal = new TextField();
        decimal.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[0-9]*") ? change : null));


        VBox root = new VBox(10, binary, decimal);
        stage.setScene(new Scene(root, 400, 200));
        binary.textProperty().addListener((observable, oldText, newText) -> {
            if (syncing) return;
            syncing = true;               
            try {
                int number = Integer.parseInt(newText, 2);
                decimal.setText(String.valueOf(number));
            } catch (NumberFormatException e) {
                decimal.clear();
            } finally {
                syncing = false;
            }
        });
        decimal.textProperty().addListener((observable, oldText, newText) -> {
            if (syncing) return;
            syncing = true;
            try {
                int number = Integer.parseInt(newText);
                binary.setText(Integer.toBinaryString(number));
            } catch (NumberFormatException e) {
                binary.clear();
            } finally {
                syncing = false;
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}