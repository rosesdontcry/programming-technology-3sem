package calculator.ui;

import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Locale;

public final class AutoResizeFontHelper {
    private static final double DEFAULT_FONT_SIZE = 14;
    private static final double MIN_FONT_SIZE = 8;
    private static final double FONT_STEP = 0.5;

    private AutoResizeFontHelper() {
    }

    public static void applyAutoResize(TextField textField) {
        textField.textProperty().addListener((obs, oldText, newText) -> adjustFontSize(textField));
        textField.widthProperty().addListener((obs, oldWidth, newWidth) -> adjustFontSize(textField));
        adjustFontSize(textField);
    }

    private static void adjustFontSize(TextField textField) {
        double availableWidth = textField.getWidth() - textField.getPadding().getLeft()
                - textField.getPadding().getRight() - 10;
        if (availableWidth <= 0) {
            return;
        }

        String text = textField.getText() == null ? "" : textField.getText();
        String family = textField.getFont().getFamily();
        double fontSize = DEFAULT_FONT_SIZE;
        while (fontSize > MIN_FONT_SIZE && measureTextWidth(text, family, fontSize) > availableWidth) {
            fontSize -= FONT_STEP;
        }

        // Inline CSS has precedence over the theme when JavaFX reapplies styles on a pulse.
        String style = String.format(Locale.ROOT, "-fx-font-size: %.1fpx;", fontSize);
        if (!style.equals(textField.getStyle())) {
            textField.setStyle(style);
        }
        if (textField.getFont().getSize() != fontSize) {
            textField.setFont(Font.font(family, fontSize));
        }
    }

    private static double measureTextWidth(String text, String family, double fontSize) {
        Text helper = new Text(text);
        helper.setFont(Font.font(family, fontSize));
        return helper.getLayoutBounds().getWidth();
    }
}
