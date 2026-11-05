package calculator.ui;

import calculator.logic.CalculatorLogic;
import calculator.logic.CalculatorLogic.FormattedNumber;
import calculator.logic.ExactNumber;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NumberEntryRow {
    private static final int MAX_CONTROL_LENGTH = CalculatorLogic.MAX_FORMATTED_FRACTION_DIGITS + 66;

    private final TextField binaryField = new TextField();
    private final TextField convertedField = new TextField();
    private final ComboBox<Integer> baseSelector = new ComboBox<>();
    private final Label binaryApproximation = createApproximationLabel();
    private final Label convertedApproximation = createApproximationLabel();
    private final HBox view = new HBox(6, binaryApproximation, binaryField,
            convertedApproximation, convertedField, baseSelector);

    private boolean isSyncing;
    private boolean valid;
    private ExactNumber currentValue;
    private Runnable onChangeCallback;

    public NumberEntryRow() {
        configureField(binaryField, "Двоичное число");
        configureField(convertedField, "Число в выбранной системе");
        HBox.setHgrow(binaryField, Priority.ALWAYS);
        HBox.setHgrow(convertedField, Priority.ALWAYS);
        view.setPadding(new Insets(2));

        setupBaseSelector();
        setupFormatters();
        setupListeners();
        AutoResizeFontHelper.applyAutoResize(binaryField);
        AutoResizeFontHelper.applyAutoResize(convertedField);
    }

    private static void configureField(TextField field, String prompt) {
        field.getStyleClass().add("number-field");
        field.setPromptText(prompt);
        field.setAccessibleText(prompt);
        field.setPrefHeight(35);
        field.setPrefWidth(200);
        field.setMinWidth(80);
        field.setMaxWidth(Double.MAX_VALUE);
    }

    private static Label createApproximationLabel() {
        Label label = new Label("≈");
        label.getStyleClass().add("approximation-indicator");
        label.setTooltip(new Tooltip("Приближённое представление числа"));
        setApproximation(label, false);
        return label;
    }

    private void setupBaseSelector() {
        baseSelector.getItems().addAll(2, 8, 10, 16);
        baseSelector.setValue(10);
        baseSelector.setAccessibleText("Основание системы счисления второго поля");
        baseSelector.valueProperty().addListener((obs, oldBase, newBase) -> {
            if (newBase == null) {
                baseSelector.setValue(oldBase);
            } else {
                onBaseChanged();
            }
        });
    }

    private void setupFormatters() {
        binaryField.setTextFormatter(new TextFormatter<>(createFilter(() -> 2)));
        convertedField.setTextFormatter(new TextFormatter<>(createFilter(baseSelector::getValue)));
    }

    private void setupListeners() {
        binaryField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isSyncing) {
                handleChanged(oldText, newText, binaryApproximation, convertedField,
                        convertedApproximation, 2, baseSelector.getValue());
            }
        });
        convertedField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isSyncing) {
                handleChanged(oldText, newText, convertedApproximation, binaryField,
                        binaryApproximation, baseSelector.getValue(), 2);
            }
        });
    }

    private UnaryOperator<TextFormatter.Change> createFilter(Supplier<Integer> baseSupplier) {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.length() > MAX_CONTROL_LENGTH) {
                // Keep one excess character so parsing invalidates the row instead of
                // silently rejecting a paste and leaving an old result on screen.
                String clipped = newText.substring(0, MAX_CONTROL_LENGTH + 1);
                if (!isValidPartialInput(clipped, baseSupplier.get())) {
                    return null;
                }
                change.setRange(0, change.getControlText().length());
                change.setText(clipped);
                change.selectRange(clipped.length(), clipped.length());
                return change;
            }
            if (newText.isEmpty() || isValidPartialInput(newText, baseSupplier.get())) {
                return change;
            }
            return null;
        };
    }

    private boolean isValidPartialInput(String text, int base) {
        int index = text.charAt(0) == '-' ? 1 : 0;
        boolean dotSeen = false;
        for (int i = index; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.') {
                if (dotSeen) {
                    return false;
                }
                dotSeen = true;
            } else if (!CalculatorLogic.isValidDigitForBase(c, base)) {
                return false;
            }
        }
        return true;
    }

    private void handleChanged(String oldText, String newText, Label sourceApproximation,
                               TextField target, Label targetApproximation, int sourceBase, int targetBase) {
        // Adding or removing trailing zeroes from an approximate display must not change
        // the more precise value that was originally entered in the other field.
        if (valid && sourceApproximation.isVisible()
                && CalculatorLogic.sameDisplayedValue(oldText, newText, sourceBase)) {
            notifyChange();
            return;
        }

        valid = false;
        currentValue = null;
        try {
            ExactNumber value = CalculatorLogic.parseExact(newText, sourceBase);
            FormattedNumber converted = CalculatorLogic.format(value, targetBase);
            sync(() -> {
                target.setText(converted.text());
                setApproximation(sourceApproximation, false);
                setApproximation(targetApproximation, converted.approximate());
            });
            currentValue = value;
            valid = true;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            // Invalid input and out-of-range values leave the result blank, without an error message.
        } finally {
            if (!valid) {
                sync(() -> {
                    target.clear();
                    setApproximation(sourceApproximation, false);
                    setApproximation(targetApproximation, false);
                });
            }
            notifyChange();
        }
    }

    private void onBaseChanged() {
        if (!valid) {
            sync(() -> {
                convertedField.clear();
                setApproximation(convertedApproximation, false);
            });
            return;
        }
        try {
            FormattedNumber converted = CalculatorLogic.format(currentValue, baseSelector.getValue());
            sync(() -> {
                convertedField.setText(converted.text());
                setApproximation(convertedApproximation, converted.approximate());
            });
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            valid = false;
            currentValue = null;
            sync(() -> {
                convertedField.clear();
                setApproximation(convertedApproximation, false);
            });
            notifyChange();
        }
    }

    private void sync(Runnable action) {
        boolean previousSyncing = isSyncing;
        isSyncing = true;
        try {
            action.run();
        } finally {
            isSyncing = previousSyncing;
        }
    }

    private static void setApproximation(Label label, boolean approximate) {
        label.setVisible(approximate);
        label.setManaged(approximate);
    }

    public void setOnChange(Runnable callback) {
        onChangeCallback = callback;
    }

    private void notifyChange() {
        if (onChangeCallback != null) {
            onChangeCallback.run();
        }
    }

    public boolean isValid() {
        return valid;
    }

    public ExactNumber getExactValue() {
        return currentValue;
    }

    /** Retains the original accessor for callers using doubles. */
    public double getValue() {
        return currentValue == null ? 0 : CalculatorLogic.toDouble(currentValue);
    }

    public void setValue(ExactNumber value) {
        // Prepare both strings before modifying the displayed or stored state.
        FormattedNumber binary = CalculatorLogic.format(value, 2);
        FormattedNumber converted = CalculatorLogic.format(value, baseSelector.getValue());
        sync(() -> {
            binaryField.setText(binary.text());
            convertedField.setText(converted.text());
            setApproximation(binaryApproximation, binary.approximate());
            setApproximation(convertedApproximation, converted.approximate());
        });
        currentValue = value;
        valid = true;
    }

    public void setValue(double value) {
        setValue(CalculatorLogic.fromDouble(value));
    }

    public void clear() {
        valid = false;
        currentValue = null;
        sync(() -> {
            binaryField.clear();
            convertedField.clear();
            setApproximation(binaryApproximation, false);
            setApproximation(convertedApproximation, false);
        });
    }

    public void setFieldsEditable(boolean editable) {
        binaryField.setEditable(editable);
        convertedField.setEditable(editable);
    }

    public void setBaseSelectable(boolean selectable) {
        baseSelector.setDisable(!selectable);
    }

    public HBox getView() {
        return view;
    }
}
