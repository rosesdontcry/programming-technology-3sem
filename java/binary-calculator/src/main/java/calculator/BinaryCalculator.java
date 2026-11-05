package calculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BinaryCalculator extends Application {
    private NumberEntryRow first;
    private NumberEntryRow second;
    private NumberEntryRow result;
    private String operation = "+";

    @Override
    public void start(Stage stage) {
        Scene scene = buildScene();
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Калькулятор");
        stage.setMinWidth(550);
        stage.setMinHeight(350);
        stage.show();
    }

    Scene buildScene() {
        first = new NumberEntryRow("Первое число", true, this::recalculate);
        second = new NumberEntryRow("Второе число", true, this::recalculate);
        result = new NumberEntryRow("Результат", false, () -> {});
        VBox root = new VBox(10, first, operationButtons(), second, new Separator(), result);
        root.setPadding(new Insets(16));
        return new Scene(root, 680, 380);
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
            if (symbol.equals("√")) {
                button.setTooltip(new Tooltip("Первое число — степень корня, второе — подкоренное число"));
            } else if (symbol.equals("mod")) {
                button.setTooltip(new Tooltip("Остаток от деления со знаком первого числа"));
            } else if (symbol.equals("^")) {
                button.setTooltip(new Tooltip("Первое число возводится в степень второго"));
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

    private void recalculate() {
        result.clear();
        if (first.getValue() == null || second.getValue() == null) {
            return;
        }
        try {
            result.setValue(CalculatorLogic.calculate(first.getValue(), second.getValue(), operation));
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            // Если посчитать или отобразить число нельзя, результат остаётся пустым.
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
