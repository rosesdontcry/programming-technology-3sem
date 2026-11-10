package calculator;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorLogicTest {
    private static Fraction decimal(String value) {
        return CalculatorLogic.parse(value, 10);
    }

    private static String decimalText(Fraction value) {
        return CalculatorLogic.format(value, 10);
    }

    private static Fraction calculate(String first, String second, String operation) {
        return CalculatorLogic.calculate(decimal(first), decimal(second), operation);
    }

    @Test
    void fractionsAreReducedAndRespectTheLimit() {
        Fraction fraction = new Fraction(BigInteger.valueOf(10), BigInteger.valueOf(-20));
        assertEquals(BigInteger.valueOf(-1), fraction.numerator);
        assertEquals(BigInteger.TWO, fraction.denominator);
        assertThrows(ArithmeticException.class, () -> new Fraction(BigInteger.ONE, BigInteger.ZERO));
        assertThrows(ArithmeticException.class,
                () -> new Fraction(BigInteger.ONE.shiftLeft(53).add(BigInteger.ONE), BigInteger.ONE));
    }

    @Test
    void parsesAndFormatsAllSupportedBases() {
        Fraction binary = CalculatorLogic.parse("101.101", 2);
        assertEquals("5.625", decimalText(binary));
        assertEquals("101.101", CalculatorLogic.format(binary, 2));
        assertEquals("5.5", CalculatorLogic.format(binary, 8));
        assertEquals("5.A", CalculatorLogic.format(binary, 16));
        assertEquals("15.5", decimalText(CalculatorLogic.parse("17.4", 8)));
        assertEquals("-10.9375", decimalText(CalculatorLogic.parse("-a.f", 16)));
        assertEquals("0.5", decimalText(CalculatorLogic.parse(".1", 2)));
        assertEquals("1", decimalText(decimal("1.")));
    }

    @Test
    void rejectsMalformedInputAndUnsupportedBases() {
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse("", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse("-", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse(".", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse("1.2.3", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse("102", 2));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse("8", 8));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse(".+1", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parse(".-1", 10));
        assertThrows(IllegalArgumentException.class, () -> CalculatorLogic.parse("1", 3));
        assertEquals("15", decimalText(CalculatorLogic.parse("f", 16)));
    }

    @Test
    void decimalArithmeticDoesNotIntroduceFloatingPointArtifacts() {
        assertEquals("0.1", decimalText(calculate("0.8", "0.7", "−")));
        assertEquals("0.3", decimalText(calculate("0.1", "0.2", "+")));
        assertEquals("0.02", decimalText(calculate("0.1", "0.2", "×")));
        assertEquals("0.125", decimalText(calculate("1", "8", "÷")));
    }

    @Test
    void recurringFractionsStayExactForFurtherArithmetic() {
        Fraction third = calculate("1", "3", "÷");
        assertTrue(decimalText(third).startsWith("0.333333333333"));
        assertEquals("1", decimalText(CalculatorLogic.calculate(third, decimal("3"), "×")));
        assertTrue(CalculatorLogic.format(decimal("0.1"), 2).startsWith("0.000110011"));
    }

    @Test
    void smallNonzeroValuesDoNotDisplayAsZero() {
        String binary = CalculatorLogic.format(decimal("0.00001"), 2);
        assertTrue(binary.contains("1"));
        assertNotEquals("0", binary);

        Fraction tiny = CalculatorLogic.power(decimal("2"), decimal("-600"));
        String exactBinary = CalculatorLogic.format(tiny, 2);
        assertTrue(exactBinary.endsWith("1"));
        assertEquals(600, exactBinary.substring(2).length());
    }

    @Test
    void neverFormatsAnOutOfPrecisionNonzeroValueAsZero() {
        Fraction tiny = CalculatorLogic.power(decimal("0.1"), decimal("2048"));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.format(tiny, 2));
    }

    @Test
    void roundsDisplayedFractions() {
        assertEquals("1", decimalText(decimal("0." + "9".repeat(40))));
        assertEquals("-1", decimalText(decimal("-0." + "9".repeat(40))));
    }

    @Test
    void keepsJavaRemainderSemanticsForNegativeAndFractionalValues() {
        assertEquals("-2", decimalText(calculate("-5", "3", "mod")));
        assertEquals("2", decimalText(calculate("5", "-3", "mod")));
        assertEquals("-1.5", decimalText(calculate("-5.5", "2", "mod")));
        assertThrows(ArithmeticException.class, () -> calculate("5", "0", "mod"));
    }

    @Test
    void handlesPowersIncludingZeroToZero() {
        assertEquals("1", decimalText(calculate("0", "0", "^")));
        assertEquals("-8", decimalText(calculate("-2", "3", "^")));
        assertEquals("0.125", decimalText(calculate("2", "-3", "^")));
        assertEquals("-1", decimalText(calculate("-1", "9007199254740991", "^")));
        assertThrows(ArithmeticException.class, () -> calculate("0", "-1", "^"));
        assertThrows(ArithmeticException.class, () -> calculate("-2", "0.5", "^"));
        assertTrue(decimalText(calculate("2", "0.5", "^")).startsWith("1.414213562373"));
    }

    @Test
    void calculatesOddRootsOfNegativeNumbersAndExactPerfectRoots() {
        assertEquals("-2", decimalText(calculate("3", "-8", "√")));
        assertEquals("-0.5", decimalText(calculate("-3", "-8", "√")));
        assertEquals("-0.5", decimalText(calculate("3", "-0.125", "√")));
        assertEquals("4", decimalText(calculate("3", "64", "√")));
        assertEquals("81", decimalText(calculate("0.5", "9", "√")));
    }

    @Test
    void findsPerfectRootsOfVerySmallExactFractions() {
        Fraction tiny = decimal("0." + "0".repeat(1499) + "1");
        Fraction root = CalculatorLogic.root(decimal("3"), tiny);
        assertEquals("0." + "0".repeat(499) + "1", decimalText(root));
        assertTrue(CalculatorLogic.format(root, 2).contains("1"));
    }

    @Test
    void handlesInvalidAndIrrationalRoots() {
        assertThrows(ArithmeticException.class, () -> calculate("2", "-8", "√"));
        assertThrows(ArithmeticException.class, () -> calculate("0", "8", "√"));
        assertThrows(ArithmeticException.class, () -> calculate("-2", "0", "√"));
        assertTrue(decimalText(calculate("2", "2", "√")).startsWith("1.414213562373"));
    }

    @Test
    void detectsRangeAndInputLimitsBeforeRounding() {
        Fraction limit = decimal("9007199254740992");
        assertEquals("9007199254740992", decimalText(limit));
        assertThrows(ArithmeticException.class, () -> decimal("9007199254740993"));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.calculate(limit, decimal("1"), "+"));
        assertEquals("0", decimalText(decimal("0".repeat(CalculatorLogic.MAX_INPUT_DIGITS))));
        assertThrows(ArithmeticException.class,
                () -> decimal("0".repeat(CalculatorLogic.MAX_INPUT_DIGITS + 1)));
    }

    @Test
    void rejectsDivisionByZeroAndUnrepresentableResults() {
        assertThrows(ArithmeticException.class, () -> calculate("1", "0", "÷"));
        assertThrows(ArithmeticException.class, () -> calculate("2", "1024.5", "^"));
        assertThrows(ArithmeticException.class, () -> calculate("0.5", "1200.5", "^"));
    }

    @Test
    void keepsTheStoredValueWhenOnlyZeroesChangeInARoundedDisplay() {
        Fraction value = decimal("0.8");
        String binary = CalculatorLogic.format(value, 2);
        assertTrue(CalculatorLogic.keepsExactValue(value, binary, binary + "0", 2));
        assertFalse(CalculatorLogic.keepsExactValue(value, binary, binary + "1", 2));
        assertFalse(CalculatorLogic.keepsExactValue(value, binary, "-", 2));
        assertFalse(CalculatorLogic.keepsExactValue(decimal("0.5"), "0.1", "0.10", 2));
    }

    @Test
    void conversionsRoundToTheNearestDisplayedDigit() {
        Random random = new Random(20261105L);
        for (int i = 0; i < 250; i++) {
            long numerator = random.nextLong(2_000_000_000_001L) - 1_000_000_000_000L;
            long denominator = random.nextLong(999_999L) + 1;
            Fraction value = CalculatorLogic.calculate(decimal(Long.toString(numerator)),
                    decimal(Long.toString(denominator)), "÷");
            for (int base : new int[]{2, 8, 10, 16}) {
                String text = CalculatorLogic.format(value, base);
                Fraction displayed = CalculatorLogic.parse(text, base);
                BigInteger error = value.numerator.multiply(displayed.denominator)
                        .subtract(displayed.numerator.multiply(value.denominator)).abs();
                int point = text.indexOf('.');
                int fractionalDigits = point < 0 ? 0 : text.length() - point - 1;
                BigInteger scaledError = error.multiply(BigInteger.TWO)
                        .multiply(BigInteger.valueOf(base).pow(fractionalDigits));
                BigInteger denominatorProduct = value.denominator.multiply(displayed.denominator);
                assertTrue(scaledError.compareTo(denominatorProduct) <= 0,
                        "Rounding error exceeded half a displayed digit: " + text);
            }
        }
    }
}
