package calculator.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        root.getChildren().add(number1Row.getView());
        root.getChildren().add(buildOperationButtons());
        root.getChildren().add(number2Row.getView());
        root.getChildren().add(new javafx.scene.control.Separator());
        root.getChildren().add(resultRow.getView());

        return new Scene(root, 550, 300);
    }

    private HBox buildOperationButtons() {
        HBox box = new HBox(6);

        operationGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });

        for (Operation op : Operation.values()) {
            ToggleButton button = new ToggleButton(op.getSymbol());
            button.setToggleGroup(operationGroup);
            button.setUserData(op);

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
        if (!number1Row.isValid() || !number2Row.isValid()) {
            resultRow.clear();
            return;
        }

        try {
            double a = number1Row.getValue();
            double b = number2Row.getValue();
            double result = currentOperation.apply(a, b);
            resultRow.setValue(result);

        } catch (RuntimeException e) {
            resultRow.clear();
        }
    }
}
