package calculator.logic;

import calculator.logic.CalculatorLogic.FormattedNumber;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorLogicTest {
    private static ExactNumber decimal(String value) {
        return CalculatorLogic.parseExact(value, 10);
    }

    private static String decimalText(ExactNumber value) {
        return CalculatorLogic.format(value, 10).text();
    }

    @Test
    void parsesAndFormatsAllSupportedBases() {
        ExactNumber binary = CalculatorLogic.parseExact("101.101", 2);
        assertEquals("5.625", decimalText(binary));
        assertEquals("101.101", CalculatorLogic.format(binary, 2).text());
        assertEquals("5.5", CalculatorLogic.format(binary, 8).text());
        assertEquals("5.A", CalculatorLogic.format(binary, 16).text());
        assertFalse(CalculatorLogic.format(binary, 16).approximate());
        assertEquals("15.5", decimalText(CalculatorLogic.parseExact("17.4", 8)));
        assertEquals("-10.9375", decimalText(CalculatorLogic.parseExact("-a.f", 16)));
        assertEquals("0.5", decimalText(CalculatorLogic.parseExact(".1", 2)));
        assertEquals("1", decimalText(decimal("1.")));
    }

    @Test
    void rejectsMalformedInputAndUnsupportedBases() {
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parseExact("", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parseExact("-", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parseExact(".", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parseExact("1.2.3", 10));
        assertThrows(NumberFormatException.class, () -> CalculatorLogic.parseExact("102", 2));
        assertThrows(IllegalArgumentException.class, () -> CalculatorLogic.parseExact("1", 3));
        assertFalse(CalculatorLogic.isValidDigitForBase('8', 8));
        assertTrue(CalculatorLogic.isValidDigitForBase('f', 16));
    }

    @Test
    void decimalArithmeticDoesNotIntroduceBinaryFloatingPointArtifacts() {
        assertEquals("0.1", decimalText(CalculatorLogic.subtract(decimal("0.8"), decimal("0.7"))));
        assertEquals("0.3", decimalText(CalculatorLogic.add(decimal("0.1"), decimal("0.2"))));
        assertEquals("0.02", decimalText(CalculatorLogic.multiply(decimal("0.1"), decimal("0.2"))));
        assertEquals("0.125", decimalText(CalculatorLogic.divide(decimal("1"), decimal("8"))));
    }

    @Test
    void recurringFractionsAreMarkedAndKeptExactForFurtherArithmetic() {
        ExactNumber third = CalculatorLogic.divide(decimal("1"), decimal("3"));
        FormattedNumber result = CalculatorLogic.format(third, 10);
        assertTrue(result.approximate());
        assertTrue(result.text().startsWith("0.333333333333"));
        assertEquals("1", decimalText(CalculatorLogic.multiply(third, decimal("3"))));

        FormattedNumber binaryTenth = CalculatorLogic.format(decimal("0.1"), 2);
        assertTrue(binaryTenth.approximate());
        assertTrue(binaryTenth.text().startsWith("0.000110011"));
    }

    @Test
    void smallNonzeroValuesDoNotDisplayAsZero() {
        FormattedNumber binary = CalculatorLogic.format(decimal("0.00001"), 2);
        assertTrue(binary.approximate());
        assertTrue(binary.text().contains("1"));
        assertNotEquals("0", binary.text());

        ExactNumber tiny = CalculatorLogic.power(decimal("2"), decimal("-600"));
        FormattedNumber exactBinary = CalculatorLogic.format(tiny, 2);
        assertFalse(exactBinary.approximate());
        assertTrue(exactBinary.text().endsWith("1"));
        assertEquals(600, exactBinary.text().substring(2).length());
    }

    @Test
    void neverFormatsAnOutOfPrecisionNonzeroValueAsZero() {
        ExactNumber tiny = CalculatorLogic.power(decimal("0.1"), decimal("2048"));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.format(tiny, 2));
    }

    @Test
    void roundsDisplayedFractionsAndMarksTheRoundedValue() {
        ExactNumber almostOne = decimal("0." + "9".repeat(40));
        FormattedNumber formatted = CalculatorLogic.format(almostOne, 10);
        assertEquals("1", formatted.text());
        assertTrue(formatted.approximate());
    }

    @Test
    void keepsJavaRemainderSemanticsForNegativeAndFractionalValues() {
        assertEquals("-2", decimalText(CalculatorLogic.mod(decimal("-5"), decimal("3"))));
        assertEquals("2", decimalText(CalculatorLogic.mod(decimal("5"), decimal("-3"))));
        assertEquals("-1.5", decimalText(CalculatorLogic.mod(decimal("-5.5"), decimal("2"))));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.mod(decimal("5"), decimal("0")));
    }

    @Test
    void handlesPowersIncludingZeroToZero() {
        assertEquals("1", decimalText(CalculatorLogic.power(decimal("0"), decimal("0"))));
        assertEquals("-8", decimalText(CalculatorLogic.power(decimal("-2"), decimal("3"))));
        assertEquals("0.125", decimalText(CalculatorLogic.power(decimal("2"), decimal("-3"))));
        assertEquals("-1", decimalText(CalculatorLogic.power(decimal("-1"), decimal("9007199254740991"))));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.power(decimal("0"), decimal("-1")));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.power(decimal("-2"), decimal("0.5")));
        FormattedNumber fractionalPower = CalculatorLogic.format(
                CalculatorLogic.power(decimal("2"), decimal("0.5")), 10);
        assertTrue(fractionalPower.approximate());
        assertTrue(fractionalPower.text().startsWith("1.414213562373"));
    }

    @Test
    void calculatesOddRootsOfNegativeNumbersAndExactPerfectRoots() {
        assertEquals("-2", decimalText(CalculatorLogic.root(decimal("3"), decimal("-8"))));
        assertEquals("-0.5", decimalText(CalculatorLogic.root(decimal("-3"), decimal("-8"))));
        assertEquals("-0.5", decimalText(CalculatorLogic.root(decimal("3"), decimal("-0.125"))));
        ExactNumber cubeRoot = CalculatorLogic.root(decimal("3"), decimal("64"));
        assertEquals("4", decimalText(cubeRoot));
        assertFalse(CalculatorLogic.format(cubeRoot, 10).approximate());
        ExactNumber fractionalDegree = CalculatorLogic.root(decimal("0.5"), decimal("9"));
        assertEquals("81", decimalText(fractionalDegree));
        assertFalse(CalculatorLogic.format(fractionalDegree, 10).approximate());
    }

    @Test
    void handlesInvalidRootsAndMarksIrrationalResults() {
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.root(decimal("2"), decimal("-8")));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.root(decimal("0"), decimal("8")));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.root(decimal("-2"), decimal("0")));
        assertTrue(CalculatorLogic.format(CalculatorLogic.root(decimal("2"), decimal("2")), 10).approximate());
    }

    @Test
    void detectsRangeAndInputLimitsBeforeRounding() {
        ExactNumber limit = decimal("9007199254740992");
        assertEquals("9007199254740992", decimalText(limit));
        assertThrows(ArithmeticException.class, () -> decimal("9007199254740993"));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.add(limit, decimal("1")));
        assertEquals("0", decimalText(decimal("0".repeat(CalculatorLogic.MAX_INPUT_DIGITS))));
        assertThrows(ArithmeticException.class,
                () -> decimal("0".repeat(CalculatorLogic.MAX_INPUT_DIGITS + 1)));
    }

    @Test
    void rejectsDivisionByZeroAndUnrepresentableApproximateResults() {
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.divide(decimal("1"), decimal("0")));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.power(decimal("2"), decimal("1024.5")));
        assertThrows(ArithmeticException.class, () -> CalculatorLogic.power(decimal("0.5"), decimal("1200.5")));
    }

    @Test
    void comparesEquivalentDisplaysWithoutChangingTheStoredValue() {
        assertTrue(CalculatorLogic.sameDisplayedValue("0.0101", "0.01010", 2));
        assertFalse(CalculatorLogic.sameDisplayedValue("0.0101", "0.01011", 2));
        assertFalse(CalculatorLogic.sameDisplayedValue("0.0101", "-", 2));
    }

    @Test
    void conversionsRoundToTheNearestDisplayedDigit() {
        Random random = new Random(20261105L);
        for (int i = 0; i < 250; i++) {
            long numerator = random.nextLong(2_000_000_000_001L) - 1_000_000_000_000L;
            long denominator = random.nextLong(999_999L) + 1;
            ExactNumber value = CalculatorLogic.divide(decimal(Long.toString(numerator)),
                    decimal(Long.toString(denominator)));
            for (int base : new int[]{2, 8, 10, 16}) {
                FormattedNumber formatted = CalculatorLogic.format(value, base);
                ExactNumber displayed = CalculatorLogic.parseExact(formatted.text(), base);
                BigInteger error = value.numerator().multiply(displayed.denominator())
                        .subtract(displayed.numerator().multiply(value.denominator())).abs();
                if (!formatted.approximate()) {
                    assertEquals(BigInteger.ZERO, error, "Unmarked approximation: " + formatted.text());
                } else {
                    assertNotEquals(BigInteger.ZERO, error, "Unnecessary approximation: " + formatted.text());
                }
                int point = formatted.text().indexOf('.');
                int fractionalDigits = point < 0 ? 0 : formatted.text().length() - point - 1;
                BigInteger scaledError = error.multiply(BigInteger.TWO)
                        .multiply(BigInteger.valueOf(base).pow(fractionalDigits));
                BigInteger denominatorProduct = value.denominator().multiply(displayed.denominator());
                assertTrue(scaledError.compareTo(denominatorProduct) <= 0,
                        "Rounding error exceeded half a displayed digit: " + formatted.text());
            }
        }
    }
}
