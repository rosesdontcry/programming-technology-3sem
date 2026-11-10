package calculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigInteger;

import static calculator.CalculatorLogic.power;
import static calculator.CalculatorLogic.root;

public class BinaryCalculator extends Application {
    private NumberEntryRow first;
    private NumberEntryRow second;
    private NumberEntryRow result;
    private String operation = "+";

    @Override
    public void start(Stage stage) {
        stage.setScene(buildScene());
        stage.setTitle("calc");
        stage.setMinWidth(500);
        stage.setMinHeight(230);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void recalculate() {
        result.clear();
        if (first.getValue() == null || second.getValue() == null) {
            return;
        }
        try {
            result.setValue(CalculatorLogic.calculate(first.getValue(), second.getValue(), operation));
        } catch (IllegalArgumentException ignored) {
        }
    }


    private HBox operationButtons() {
        HBox buttons = new HBox(6);
        ToggleGroup group = new ToggleGroup();
        for (String symbol : new String[]{"+", "−", "×", "÷", "mod", "√", "^"}) {
            ToggleButton button = new ToggleButton(symbol);
            button.setToggleGroup(group);
            if (symbol.equals(operation)) {
                button.setSelected(true);
            }
            button.setOnAction(event -> {
                button.setSelected(true);
                operation = symbol;
                recalculate();
            });
            buttons.getChildren().add(button);
        }
        return buttons;
    }


    Scene buildScene() {
        first = new NumberEntryRow("first number", true, this::recalculate);
        second = new NumberEntryRow("second number", true, this::recalculate);
        result = new NumberEntryRow("result", false, () -> {
        });
        VBox root = new VBox(10, first, operationButtons(), second, result);
        root.setPadding(new Insets(16));
        return new Scene(root, 500, 230);
    }
}