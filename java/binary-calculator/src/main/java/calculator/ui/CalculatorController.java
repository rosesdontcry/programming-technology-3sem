package calculator.ui;

import calculator.logic.ExactNumber;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CalculatorController {
    private final NumberEntryRow number1Row = new NumberEntryRow();
    private final NumberEntryRow number2Row = new NumberEntryRow();
    private final NumberEntryRow resultRow = new NumberEntryRow();

    private final ToggleGroup operationGroup = new ToggleGroup();
    private Operation currentOperation = Operation.ADD;

    public CalculatorController() {
        resultRow.setFieldsEditable(false);
        resultRow.setBaseSelectable(true);
        number1Row.setOnChange(this::recalculate);
        number2Row.setOnChange(this::recalculate);
    }

    public Scene buildScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.getChildren().add(labeledRow("Первое число", number1Row));
        root.getChildren().add(buildOperationButtons());
        root.getChildren().add(labeledRow("Второе число", number2Row));
        root.getChildren().add(new Separator());
        root.getChildren().add(labeledRow("Результат", resultRow));
        Label legend = new Label("≈ — приближённое представление");
        legend.getStyleClass().add("precision-legend");
        root.getChildren().add(legend);
        return new Scene(root, 680, 380);
    }

    private VBox labeledRow(String text, NumberEntryRow row) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return new VBox(3, label, row.getView());
    }

    private HBox buildOperationButtons() {
        HBox box = new HBox(6);
        operationGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });

        for (Operation op : Operation.values()) {
            ToggleButton button = new ToggleButton(op.getSymbol());
            button.setToggleGroup(operationGroup);
            button.setUserData(op);
            if (op == Operation.ROOT) {
                button.setTooltip(new Tooltip("Первое число — степень корня, второе — подкоренное число"));
            } else if (op == Operation.MOD) {
                button.setTooltip(new Tooltip("Остаток от деления со знаком первого числа"));
            } else if (op == Operation.POWER) {
                button.setTooltip(new Tooltip("Первое число возводится в степень второго"));
            }
            if (op == currentOperation) {
                button.setSelected(true);
            }
            button.setOnAction(e -> {
                currentOperation = (Operation) button.getUserData();
                recalculate();
            });
            box.getChildren().add(button);
        }
        return box;
    }

    private void recalculate() {
        // Never leave a previous result on screen after either input changes.
        resultRow.clear();
        if (!number1Row.isValid() || !number2Row.isValid()) {
            return;
        }
        try {
            ExactNumber result = currentOperation.apply(number1Row.getExactValue(), number2Row.getExactValue());
            resultRow.setValue(result);
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            // Invalid operations and values outside the limits leave the result blank.
        }
    }
}
