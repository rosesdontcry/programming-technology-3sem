package calculator;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.IntSupplier;

final class NumberEntryRow extends VBox {
    private final TextField binary = new TextField();
    private final TextField converted = new TextField();
    private boolean syncing;
    private Fraction value;
    private final Runnable onChange;
    private final ComboBox<Integer> base = new ComboBox<>();


    NumberEntryRow(String title, boolean editable, Runnable onChange) {
        super(3);
        this.onChange = onChange;

        configureField(binary, "binary", editable);
        configureField(converted, "converted", editable);

        base.getItems().addAll(2, 8, 10, 16);
        base.setValue(10);

        base.valueProperty().addListener((obs, oldBase, newBase)
                -> changeBase());

        HBox row = new HBox(6, binary, converted, base);
        HBox.setHgrow(converted, Priority.ALWAYS);
        HBox.setHgrow(binary, Priority.ALWAYS);
        getChildren().addAll(new Label(title), row);

        binary.setTextFormatter(filter(() -> 2));
        converted.setTextFormatter(filter(base::getValue));

        binary.textProperty().addListener((obs, oldText, newText)
                -> convert(newText, 2, converted, base.getValue()));
        converted.textProperty().addListener((obs, oldText, newText)
                -> convert(newText, base.getValue(), binary, 2));
    }

    private TextFormatter<String> filter(IntSupplier radix) {
        return new TextFormatter<>(change -> {
            String digits = "0123456789ABCDEF".substring(0, radix.getAsInt());
            String pattern = "(?i)-?[" + digits + "]*(\\.[" + digits + "]*)?";
            return change.getControlNewText().matches(pattern) ? change : null;
        });
    }

    private void convert(String text, int from, TextField target, int to) {
        if (syncing) return;
        value = null;
        try {
            Fraction parsed = CalculatorLogic.parse(text, from);
            String shown = CalculatorLogic.format(parsed, to);
            sync(() -> target.setText(shown));
            value = parsed;
        } catch (IllegalArgumentException | ArithmeticException e) {
            sync(target::clear);
        }
        onChange.run();
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

    private void changeBase() {
        if (value == null) {
            sync(converted::clear);
            return;
        }
        try {
            String text = CalculatorLogic.format(value, base.getValue());
            sync(() -> converted.setText(text));
        } catch (IllegalArgumentException | ArithmeticException e) {
            value = null;
            sync(converted::clear);
            onChange.run();
        }
    }

    private void configureField(TextField field, String prompt, boolean editable) {
        field.setPromptText(prompt);
        field.setEditable(editable);
    }
}
