package calculator.ui;

import calculator.logic.CalculatorLogic;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NumberEntryRow {

    private static final String FIELD_STYLE_CLASS = "number-field";
    private static final String ERROR_STYLE_CLASS = "error";

    private final TextField binaryField = new TextField();
    private final TextField convertedField = new TextField();
    private final ComboBox<Integer> baseSelector = new ComboBox<>();

    private boolean isSyncing = false;
    private boolean valid = false;
    private double currentValue = 0;
    private Runnable onChangeCallback;

    public NumberEntryRow() {
        binaryField.getStyleClass().add(FIELD_STYLE_CLASS);
        convertedField.getStyleClass().add(FIELD_STYLE_CLASS);
        binaryField.setPrefHeight(35);
        convertedField.setPrefHeight(35);
        binaryField.setPrefWidth(200);
        convertedField.setPrefWidth(200);
        binaryField.setMinWidth(200);
        convertedField.setMinWidth(200);

        setupBaseSelector();
        setupFormatters();
        setupListeners();
        AutoResizeFontHelper.applyAutoResize(binaryField);
        AutoResizeFontHelper.applyAutoResize(convertedField);
    }

    // === Инициализация ===

    private void setupBaseSelector() {
        baseSelector.getItems().addAll(2, 8, 10, 16);
        baseSelector.setValue(10);
        baseSelector.valueProperty().addListener((obs, oldBase, newBase) -> onBaseChanged());
    }

    private void setupFormatters() {
        binaryField.setTextFormatter(new TextFormatter<>(createFilter(() -> 2)));
        convertedField.setTextFormatter(new TextFormatter<>(createFilter(baseSelector::getValue)));
    }

    private void setupListeners() {
        binaryField.textProperty().addListener((obs, oldText, newText) -> {
            if (isSyncing) {
                return;
            }
            handleBinaryChanged(newText);
        });

        convertedField.textProperty().addListener((obs, oldText, newText) -> {
            if (isSyncing) {
                return;
            }
            handleConvertedChanged(newText);
        });
    }

    // === Блокировка недопустимых символов ===

    private UnaryOperator<TextFormatter.Change> createFilter(Supplier<Integer> baseSupplier) {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || isValidPartialInput(newText, baseSupplier.get())) {
                return change;
            }
            return null;
        };
    }

    private boolean isValidPartialInput(String text, int base) {
        int index = 0;
        if (text.charAt(0) == '-') {
            index = 1;
        }

        boolean dotSeen = false;
        for (int i = index; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.') {
                if (dotSeen) {
                    return false;
                }
                dotSeen = true;
                continue;
            }
            if (!CalculatorLogic.isValidDigitForBase(c, base)) {
                return false;
            }
        }
        return true;
    }

    private void handleBinaryChanged(String newText) {
        try {
            double value = CalculatorLogic.parseNumber(newText, 2);
            currentValue = value;
            valid = true;
            clearErrorStyle(binaryField);

            isSyncing = true;
            convertedField.setText(CalculatorLogic.toStringInBase(value, baseSelector.getValue()));
            clearErrorStyle(convertedField);
            isSyncing = false;

        } catch (NumberFormatException e) {
            valid = false;
            applyErrorStyle(binaryField);

            isSyncing = true;
            convertedField.clear();
            isSyncing = false;
        }

        notifyChange();
    }

    private void handleConvertedChanged(String newText) {
        int base = baseSelector.getValue();
        try {
            double value = CalculatorLogic.parseNumber(newText, base);
            currentValue = value;
            valid = true;
            clearErrorStyle(convertedField);

            isSyncing = true;
            binaryField.setText(CalculatorLogic.toStringInBase(value, 2));
            clearErrorStyle(binaryField);
            isSyncing = false;

        } catch (NumberFormatException e) {
            valid = false;
            applyErrorStyle(convertedField);

            isSyncing = true;
            binaryField.clear();
            isSyncing = false;
        }

        notifyChange();
    }

    private void onBaseChanged() {
        if (valid) {
            isSyncing = true;
            convertedField.setText(CalculatorLogic.toStringInBase(currentValue, baseSelector.getValue()));
            clearErrorStyle(convertedField);
            isSyncing = false;
        } else {
            isSyncing = true;
            convertedField.clear();
            isSyncing = false;
        }
    }

    private static void applyErrorStyle(TextField field) {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            field.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private static void clearErrorStyle(TextField field) {
        field.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    public void setOnChange(Runnable callback) {
        this.onChangeCallback = callback;
    }

    private void notifyChange() {
        if (onChangeCallback != null) {
            onChangeCallback.run();
        }
    }

    public boolean isValid() {
        return valid;
    }

    public double getValue() {
        return currentValue;
    }

    public void setValue(double value) {
        valid = true;
        currentValue = value;

        isSyncing = true;
        binaryField.setText(CalculatorLogic.toStringInBase(value, 2));
        convertedField.setText(CalculatorLogic.toStringInBase(value, baseSelector.getValue()));
        clearErrorStyle(binaryField);
        clearErrorStyle(convertedField);
        isSyncing = false;
    }

    public void clear() {
        valid = false;
        currentValue = 0;

        isSyncing = true;
        binaryField.clear();
        convertedField.clear();
        clearErrorStyle(binaryField);
        clearErrorStyle(convertedField);
        isSyncing = false;
    }

    public void setFieldsEditable(boolean editable) {
        binaryField.setEditable(editable);
        convertedField.setEditable(editable);
    }

    public void setBaseSelectable(boolean selectable) {
        baseSelector.setDisable(!selectable);
    }

    public HBox getView() {
        HBox box = new HBox(10, binaryField, convertedField, baseSelector);
        box.setPadding(new Insets(5));
        return box;
    }
}