package calculator;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

final class NumberEntryRow extends VBox {
    private final TextField binary = new TextField();
    private final TextField converted = new TextField();
    private final ComboBox<Integer> base = new ComboBox<>();
    private final Runnable onChange;
    private Fraction value;
    private boolean syncing;

    NumberEntryRow(String title, boolean editable, Runnable onChange) {
        super(3);
        this.onChange = onChange;
        Label label = new Label(title);
        label.getStyleClass().add("section-label");
        configureField(binary, "Двоичное число", editable);
        configureField(converted, "Число в выбранной системе", editable);
        base.getItems().addAll(2, 8, 10, 16);
        base.setValue(10);
        base.setAccessibleText("Основание системы счисления второго поля");
        HBox row = new HBox(6, binary, converted, base);
        row.setPadding(new Insets(2));
        HBox.setHgrow(binary, Priority.ALWAYS);
        HBox.setHgrow(converted, Priority.ALWAYS);
        getChildren().addAll(label, row);

        binary.setTextFormatter(filter(true));
        converted.setTextFormatter(filter(false));
        binary.textProperty().addListener((obs, oldText, newText) ->
                update(oldText, newText, converted, 2, base.getValue()));
        converted.textProperty().addListener((obs, oldText, newText) ->
                update(oldText, newText, binary, base.getValue(), 2));
        base.valueProperty().addListener((obs, oldBase, newBase) -> {
            if (newBase == null) {
                base.setValue(oldBase);
            } else {
                changeBase();
            }
        });
    }

    private void configureField(TextField field, String prompt, boolean editable) {
        field.getStyleClass().add("number-field");
        field.setPromptText(prompt);
        field.setAccessibleText(prompt);
        field.setEditable(editable);
    }

    private TextFormatter<String> filter(boolean binaryField) {
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            int radix = binaryField ? 2 : base.getValue();
            boolean tooLong = text.length() > CalculatorLogic.MAX_INPUT_DIGITS + 2;
            if (tooLong) {
                text = text.substring(0, CalculatorLogic.MAX_INPUT_DIGITS + 3);
            }
            String digits = "0123456789ABCDEF".substring(0, radix);
            String pattern = "(?i)-?[" + digits + "]*(\\.[" + digits + "]*)?";
            if (!text.matches(pattern)) {
                return null;
            }
            if (tooLong) {
                change.setRange(0, change.getControlText().length());
                change.setText(text);
                change.selectRange(text.length(), text.length());
            }
            return change;
        });
    }

    private void update(String oldText, String newText, TextField target, int sourceBase, int targetBase) {
        if (syncing) {
            return;
        }

        if (value != null && CalculatorLogic.keepsExactValue(value, oldText, newText, sourceBase)) {
            return;
        }
        value = null;
        try {
            Fraction parsed = CalculatorLogic.parse(newText, sourceBase);
            String text = CalculatorLogic.format(parsed, targetBase);
            sync(() -> target.setText(text));
            value = parsed;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sync(target::clear);
        }
        onChange.run();
    }

    private void changeBase() {
        if (value == null) {
            sync(converted::clear);
            return;
        }
        try {
            String text = CalculatorLogic.format(value, base.getValue());
            sync(() -> converted.setText(text));
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            value = null;
            sync(converted::clear);
            onChange.run();
        }
    }


    private void sync(Runnable action) {
        boolean wasSyncing = syncing;
        syncing = true;
        try {
            action.run();
        } finally {
            syncing = wasSyncing;
        }
    }

    Fraction getValue() {
        return value;
    }

    void setValue(Fraction number) {
        String binaryText = CalculatorLogic.format(number, 2);
        String convertedText = CalculatorLogic.format(number, base.getValue());
        sync(() -> {
            binary.setText(binaryText);
            converted.setText(convertedText);
        });
        value = number;
    }

    void clear() {
        value = null;
        sync(() -> {
            binary.clear();
            converted.clear();
        });
    }

}
