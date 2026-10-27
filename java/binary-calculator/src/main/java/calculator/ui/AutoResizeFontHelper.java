package calculator.ui;


import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class AutoResizeFontHelper {

    private static final double DEFAULT_FONT_SIZE = 14;
    private static final double MIN_FONT_SIZE = 8;
    private static final double FONT_STEP = 0.5;

    private AutoResizeFontHelper() {
    }

    public static void applyAutoResize(TextField textField) {
        textField.textProperty().addListener((obs, oldText, newText) -> {
            adjustFontSize(textField, newText);
        });
    }

    private static void adjustFontSize(TextField textField, String text) {
        double availableWidth = textField.getWidth() - textField.getPadding().getLeft()
                - textField.getPadding().getRight() - 10;

        double fontSize = DEFAULT_FONT_SIZE;

        while (fontSize > MIN_FONT_SIZE) {
            double textWidth = measureTextWidth(text, fontSize);
            if (textWidth <= availableWidth) {
                break;
            }
            fontSize -= FONT_STEP;
        }

        textField.setFont(Font.font(fontSize));
    }

    private static double measureTextWidth(String text, double fontSize) {
        Text helper = new Text(text);
        helper.setFont(Font.font(fontSize));
        return helper.getLayoutBounds().getWidth();
    }
}
