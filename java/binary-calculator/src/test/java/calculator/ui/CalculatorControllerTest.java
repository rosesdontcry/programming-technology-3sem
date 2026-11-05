package calculator.ui;

import calculator.BinaryCalculator;
import calculator.logic.CalculatorLogic;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "javaFxAvailable", disabledReason = "JavaFX UI tests require a display or Monocle")
class CalculatorControllerTest {
    private static boolean toolkitStarted;

    static boolean javaFxAvailable() {
        return "Monocle".equalsIgnoreCase(System.getProperty("glass.platform", ""))
                || !GraphicsEnvironment.isHeadless();
    }

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(started::countDown);
        }
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
        Platform.setImplicitExit(false);
        toolkitStarted = true;
    }

    @AfterAll
    static void stopToolkit() {
        if (toolkitStarted) {
            Platform.exit();
        }
    }

    @Test
    void oversizedInputClearsTheResultAndTheRowRecovers() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("1");
            screen.second.converted.setText("2");
            assertEquals("3", screen.result.converted.getText());

            screen.first.converted.setText("9007199254740994");
            assertEquals("", screen.result.converted.getText());
            assertEquals("", screen.first.binary.getText());
            assertFalse(screen.first.converted.getStyleClass().contains("error"));

            screen.first.converted.setText("7");
            assertEquals("111", screen.first.binary.getText());
            assertEquals("9", screen.result.converted.getText());
            return null;
        });
    }

    @Test
    void pastingMoreThanTheInputLengthLimitClearsThePreviousResult() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("1");
            screen.second.converted.setText("2");
            assertEquals("3", screen.result.converted.getText());

            screen.first.converted.setText("9".repeat(5000));
            assertTrue(screen.first.converted.getText().length() > CalculatorLogic.MAX_INPUT_DIGITS);
            assertEquals("", screen.first.binary.getText());
            assertEquals("", screen.result.converted.getText());

            screen.first.converted.setText("7");
            assertEquals("9", screen.result.converted.getText());
            return null;
        });
    }

    @Test
    void decimalArithmeticAndApproximatedFieldsRemainConsistent() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("0.8");
            screen.second.converted.setText("0.7");
            screen.select("−");
            assertEquals("0.1", screen.result.converted.getText());
            assertFalse(screen.result.convertedApproximation.isVisible());
            assertTrue(screen.result.binaryApproximation.isVisible());
            assertTrue(screen.first.binaryApproximation.isVisible());

            screen.first.binary.appendText("0");
            assertEquals("0.8", screen.first.converted.getText());
            assertEquals("0.1", screen.result.converted.getText());

            screen.select("+");
            screen.first.converted.setText("0.00001");
            screen.second.converted.setText("0");
            assertEquals("0.00001", screen.result.converted.getText());
            assertTrue(screen.result.binary.getText().contains("1"));
            assertTrue(screen.result.binaryApproximation.isVisible());
            return null;
        });
    }

    @Test
    void invalidOperationsAndOverflowLeaveTheResultBlankWithoutAnErrorStyle() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("7");
            screen.second.converted.setText("0");
            screen.select("÷");
            assertEquals("", screen.result.converted.getText());
            assertFalse(screen.result.converted.getStyleClass().contains("error"));
            screen.second.converted.setText("2");
            assertEquals("3.5", screen.result.converted.getText());

            screen.select("+");
            screen.first.converted.setText("9007199254740992");
            screen.second.converted.setText("1");
            assertEquals("", screen.result.converted.getText());
            screen.second.converted.setText("0");
            assertEquals("9007199254740992", screen.result.converted.getText());

            screen.first.converted.setText("9".repeat(400));
            screen.second.converted.setText("8".repeat(400));
            screen.select("−");
            assertEquals("", screen.result.converted.getText());
            screen.first.converted.setText("1");
            screen.second.converted.setText("2");
            assertEquals("-1", screen.result.converted.getText());

            screen.first.converted.setText("2");
            screen.second.converted.setText("1024.5");
            screen.select("^");
            assertEquals("", screen.result.converted.getText());
            screen.second.converted.setText("0.5");
            assertTrue(screen.result.converted.getText().startsWith("1.414213562373"));
            assertTrue(screen.result.convertedApproximation.isVisible());
            return null;
        });
    }

    @Test
    void supportsOddRootsWhileKeepingRemainderAndZeroPowerBehavior() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("3");
            screen.second.converted.setText("-8");
            screen.select("√");
            assertEquals("-2", screen.result.converted.getText());
            screen.first.converted.setText("2");
            assertEquals("", screen.result.converted.getText());
            screen.first.converted.setText("3");
            screen.second.converted.setText("64");
            assertEquals("4", screen.result.converted.getText());

            screen.first.converted.setText("-5");
            screen.second.converted.setText("3");
            screen.select("mod");
            assertEquals("-2", screen.result.converted.getText());
            screen.first.converted.setText("0");
            screen.second.converted.setText("0");
            screen.select("^");
            assertEquals("1", screen.result.converted.getText());
            return null;
        });
    }

    @Test
    void changingBasesKeepsTheNumericValue() throws Exception {
        onFxThread(() -> {
            Screen screen = new Screen();
            screen.first.converted.setText("5.625");
            screen.second.converted.setText("2.5");
            assertFalse(screen.first.base.isDisabled());
            assertFalse(screen.second.base.isDisabled());
            assertFalse(screen.result.base.isDisabled());
            screen.first.base.setValue(16);
            assertEquals("5.A", screen.first.converted.getText());
            assertEquals("8.125", screen.result.converted.getText());
            screen.result.base.setValue(16);
            assertEquals("8.2", screen.result.converted.getText());
            return null;
        });
    }

    @Test
    void longNumbersKeepTheFixedFontSize() throws Exception {
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                new BinaryCalculator().start(stage);
                VBox root = (VBox) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                Row first = new Row((VBox) root.getChildren().get(0));
                Row second = new Row((VBox) root.getChildren().get(2));
                Row result = new Row((VBox) root.getChildren().get(4));
                assertEquals(14, first.binary.getFont().getSize());
                second.converted.setText("0");
                String longBinary = "0." + "0".repeat(85) + "1";
                first.binary.setText(longBinary);
                assertEquals(longBinary, result.binary.getText());

                root.applyCss();
                root.layout();
                assertEquals(14, first.binary.getFont().getSize());
                assertEquals(14, result.binary.getFont().getSize());
                first.binary.positionCaret(first.binary.getLength());
                result.binary.positionCaret(result.binary.getLength());
                assertEquals(first.binary.getLength(), first.binary.getCaretPosition());
                assertEquals(result.binary.getLength(), result.binary.getCaretPosition());
            } finally {
                stage.close();
            }
            return null;
        });
    }

    private static <T> T onFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private static class Screen {
        final Row first;
        final Row second;
        final Row result;
        final HBox operations;

        Screen() {
            Scene scene = new CalculatorController().buildScene();
            VBox root = (VBox) scene.getRoot();
            first = new Row((VBox) root.getChildren().get(0));
            operations = (HBox) root.getChildren().get(1);
            second = new Row((VBox) root.getChildren().get(2));
            result = new Row((VBox) root.getChildren().get(4));
        }

        void select(String symbol) {
            operations.getChildren().stream().map(node -> (ToggleButton) node)
                    .filter(button -> button.getText().equals(symbol)).findFirst().orElseThrow().fire();
        }
    }

    private static class Row {
        final Label binaryApproximation;
        final TextField binary;
        final Label convertedApproximation;
        final TextField converted;
        final ComboBox<Integer> base;

        @SuppressWarnings("unchecked")
        Row(VBox labeledRow) {
            HBox row = (HBox) labeledRow.getChildren().get(1);
            binaryApproximation = (Label) row.getChildren().get(0);
            binary = (TextField) row.getChildren().get(1);
            convertedApproximation = (Label) row.getChildren().get(2);
            converted = (TextField) row.getChildren().get(3);
            base = (ComboBox<Integer>) row.getChildren().get(4);
        }
    }
}
