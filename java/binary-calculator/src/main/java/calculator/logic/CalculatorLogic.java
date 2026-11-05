package calculator.logic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

public final class CalculatorLogic {
    public static final int MAX_FORMATTED_FRACTION_DIGITS = 2048;
    public static final int MAX_INPUT_DIGITS = MAX_FORMATTED_FRACTION_DIGITS + 64;

    private static final int SIGNIFICANT_FRACTION_DIGITS = 32;
    private static final int MAX_EXACT_POWER = 16_384;
    private static final long MAX_POWER_BITS = 65_536;
    private static final String DIGITS = "0123456789ABCDEF";
    private static final BigInteger MAX_ABSOLUTE_INTEGER = BigInteger.ONE.shiftLeft(53);

    private CalculatorLogic() {
    }

    /** Text for a field, and whether it is only an approximation of the value. */
    public record FormattedNumber(String text, boolean approximate) {
    }

    public static ExactNumber add(ExactNumber a, ExactNumber b) {
        BigInteger numerator = a.numerator().multiply(b.denominator())
                .add(b.numerator().multiply(a.denominator()));
        return checked(numerator, a.denominator().multiply(b.denominator()), approximate(a, b));
    }

    public static ExactNumber subtract(ExactNumber a, ExactNumber b) {
        BigInteger numerator = a.numerator().multiply(b.denominator())
                .subtract(b.numerator().multiply(a.denominator()));
        return checked(numerator, a.denominator().multiply(b.denominator()), approximate(a, b));
    }

    public static ExactNumber multiply(ExactNumber a, ExactNumber b) {
        return checked(a.numerator().multiply(b.numerator()),
                a.denominator().multiply(b.denominator()), approximate(a, b));
    }

    public static ExactNumber divide(ExactNumber a, ExactNumber b) {
        if (b.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return checked(a.numerator().multiply(b.denominator()),
                a.denominator().multiply(b.numerator()), approximate(a, b));
    }

    /** Remainder with a quotient truncated toward zero, like Java's % operator. */
    public static ExactNumber mod(ExactNumber a, ExactNumber b) {
        if (b.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        BigInteger quotient = a.numerator().multiply(b.denominator())
                .divide(a.denominator().multiply(b.numerator()));
        BigInteger numerator = a.numerator().multiply(b.denominator())
                .subtract(quotient.multiply(b.numerator()).multiply(a.denominator()));
        return checked(numerator, a.denominator().multiply(b.denominator()), approximate(a, b));
    }

    public static ExactNumber power(ExactNumber base, ExactNumber exponent) {
        boolean approximate = approximate(base, exponent);
        if (exponent.signum() == 0) {
            return integer(BigInteger.ONE, approximate); // Includes 0^0, as in Math.pow.
        }
        if (base.signum() == 0) {
            if (exponent.signum() < 0) {
                throw new ArithmeticException("Zero cannot be raised to a negative power");
            }
            return integer(BigInteger.ZERO, approximate);
        }
        if (base.signum() < 0 && !exponent.isInteger()) {
            throw new ArithmeticException("Negative base with a fractional exponent");
        }
        if (base.numerator().equals(base.denominator())) {
            return integer(BigInteger.ONE, approximate);
        }

        if (exponent.isInteger()) {
            BigInteger magnitude = exponent.numerator().abs();
            if (base.numerator().negate().equals(base.denominator())) {
                return integer(magnitude.testBit(0) ? BigInteger.ONE.negate() : BigInteger.ONE,
                        approximate);
            }
            if (magnitude.compareTo(BigInteger.valueOf(MAX_EXACT_POWER)) > 0) {
                throw new ArithmeticException("Exponent exceeds the supported limit");
            }
            int n = magnitude.intValue();
            if ((long) base.numerator().abs().bitLength() * n > MAX_POWER_BITS
                    || (long) base.denominator().bitLength() * n > MAX_POWER_BITS) {
                throw new ArithmeticException("Power exceeds the supported precision");
            }
            BigInteger numerator = base.numerator().pow(n);
            BigInteger denominator = base.denominator().pow(n);
            if (exponent.signum() < 0) {
                return checked(denominator, numerator, approximate);
            }
            return checked(numerator, denominator, approximate);
        }

        double result = Math.pow(toFiniteDouble(base), toFiniteDouble(exponent));
        return approximateResult(result, true);
    }

    /** The first argument is the degree, the second is the radicand. */
    public static ExactNumber root(ExactNumber degree, ExactNumber number) {
        if (degree.signum() == 0) {
            throw new ArithmeticException("Root degree cannot be zero");
        }
        boolean negative = number.signum() < 0;
        if (negative && (!degree.isInteger() || !degree.numerator().abs().testBit(0))) {
            throw new ArithmeticException("A negative radicand needs an odd integer degree");
        }
        boolean approximate = approximate(degree, number);
        if (number.signum() == 0) {
            if (degree.signum() < 0) {
                throw new ArithmeticException("Zero cannot have a negative root degree");
            }
            return integer(BigInteger.ZERO, approximate);
        }
        if (number.numerator().abs().equals(number.denominator())) {
            return integer(negative ? BigInteger.ONE.negate() : BigInteger.ONE, approximate);
        }

        // A degree such as 1/2 has an integer reciprocal, so the result can be exact.
        if (!negative && !degree.isInteger() && degree.numerator().abs().equals(BigInteger.ONE)) {
            BigInteger exponent = degree.signum() < 0 ? degree.denominator().negate() : degree.denominator();
            return power(number, ExactNumber.of(exponent, BigInteger.ONE, degree.isApproximate()));
        }

        if (degree.isInteger()
                && degree.numerator().abs().compareTo(BigInteger.valueOf(MAX_EXACT_POWER)) <= 0) {
            int n = degree.numerator().abs().intValue();
            BigInteger numeratorRoot = exactIntegerRoot(number.numerator().abs(), n);
            BigInteger denominatorRoot = exactIntegerRoot(number.denominator(), n);
            if (numeratorRoot != null && denominatorRoot != null) {
                if (negative) {
                    numeratorRoot = numeratorRoot.negate();
                }
                if (degree.signum() < 0) {
                    return checked(denominatorRoot, numeratorRoot, approximate);
                }
                return checked(numeratorRoot, denominatorRoot, approximate);
            }
        }

        ExactNumber positive = ExactNumber.of(number.numerator().abs(), number.denominator(), number.isApproximate());
        double result = Math.pow(toFiniteDouble(positive), 1.0 / toFiniteDouble(degree));
        return approximateResult(negative ? -result : result, true);
    }

    public static ExactNumber parseExact(String numberString, int base) {
        return parse(numberString, base, MAX_INPUT_DIGITS);
    }

    /** Compares two displayed strings without losing the exact value behind an approximation. */
    public static boolean sameDisplayedValue(String first, String second, int base) {
        try {
            int maxDigits = MAX_FORMATTED_FRACTION_DIGITS + 64;
            return parse(first, base, maxDigits).hasSameValue(parse(second, base, maxDigits));
        } catch (IllegalArgumentException | ArithmeticException e) {
            return false;
        }
    }

    private static ExactNumber parse(String numberString, int base, int maxDigits) {
        validateBase(base);
        if (numberString == null || numberString.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        // Allow for a leading minus and a decimal point, but reject oversized input early.
        if (numberString.length() > maxDigits + 2) {
            throw new ArithmeticException("Input exceeds the supported length");
        }
        boolean negative = numberString.charAt(0) == '-';
        int index = negative ? 1 : 0;
        boolean dotSeen = false;
        int digits = 0;
        int fractionalDigits = 0;
        BigInteger numerator = BigInteger.ZERO;
        BigInteger radix = BigInteger.valueOf(base);
        for (; index < numberString.length(); index++) {
            char c = Character.toUpperCase(numberString.charAt(index));
            if (c == '.') {
                if (dotSeen) {
                    throw new NumberFormatException("Multiple decimal points");
                }
                dotSeen = true;
                continue;
            }
            int digit = DIGITS.indexOf(c);
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
            }
            if (++digits > maxDigits) {
                throw new ArithmeticException("Input exceeds the supported length");
            }
            numerator = numerator.multiply(radix).add(BigInteger.valueOf(digit));
            if (dotSeen) {
                fractionalDigits++;
            }
        }
        if (digits == 0) {
            throw new NumberFormatException("No digits found");
        }
        if (negative) {
            numerator = numerator.negate();
        }
        return checked(numerator, radix.pow(fractionalDigits), false);
    }

    /** Formats by long division. Recurring or rounded representations are marked approximate. */
    public static FormattedNumber format(ExactNumber number, int base) {
        validateBase(base);
        checkMagnitude(number);
        BigInteger[] parts = number.numerator().abs().divideAndRemainder(number.denominator());
        BigInteger whole = parts[0];
        BigInteger remainder = parts[1];
        BigInteger radix = BigInteger.valueOf(base);
        StringBuilder fraction = new StringBuilder();
        int significantDigits = 0;
        boolean significantStarted = false;

        while (remainder.signum() != 0 && fraction.length() < MAX_FORMATTED_FRACTION_DIGITS
                && significantDigits < SIGNIFICANT_FRACTION_DIGITS) {
            BigInteger[] digitAndRemainder = remainder.multiply(radix).divideAndRemainder(number.denominator());
            int digit = digitAndRemainder[0].intValue();
            fraction.append(DIGITS.charAt(digit));
            remainder = digitAndRemainder[1];
            if (digit != 0 || significantStarted) {
                significantStarted = true;
                significantDigits++;
            }
        }

        boolean rounded = remainder.signum() != 0;
        if (rounded && remainder.shiftLeft(1).compareTo(number.denominator()) >= 0
                && incrementFraction(fraction, base)) {
            whole = whole.add(BigInteger.ONE);
        }
        while (fraction.length() > 0 && fraction.charAt(fraction.length() - 1) == '0') {
            fraction.deleteCharAt(fraction.length() - 1);
        }
        if (number.signum() != 0 && whole.signum() == 0 && fraction.isEmpty()) {
            throw new ArithmeticException("Number is too small to display within the precision limit");
        }

        StringBuilder text = new StringBuilder();
        if (number.signum() < 0) {
            text.append('-');
        }
        text.append(whole.toString(base).toUpperCase(Locale.ROOT));
        if (!fraction.isEmpty()) {
            text.append('.').append(fraction);
        }
        return new FormattedNumber(text.toString(), number.isApproximate() || rounded);
    }

    /** @return whether rounding carried into the whole part. */
    private static boolean incrementFraction(StringBuilder fraction, int base) {
        for (int i = fraction.length() - 1; i >= 0; i--) {
            int digit = DIGITS.indexOf(fraction.charAt(i)) + 1;
            if (digit < base) {
                fraction.setCharAt(i, DIGITS.charAt(digit));
                return false;
            }
            fraction.setCharAt(i, '0');
        }
        return true;
    }

    public static boolean isValidDigitForBase(char digit, int base) {
        validateBase(base);
        int digitValue = DIGITS.indexOf(Character.toUpperCase(digit));
        return digitValue >= 0 && digitValue < base;
    }

    private static ExactNumber integer(BigInteger value, boolean approximate) {
        return checked(value, BigInteger.ONE, approximate);
    }

    private static ExactNumber checked(BigInteger numerator, BigInteger denominator, boolean approximate) {
        ExactNumber result = ExactNumber.of(numerator, denominator, approximate);
        checkMagnitude(result);
        return result;
    }

    private static void checkMagnitude(ExactNumber number) {
        if (number.numerator().abs().compareTo(number.denominator().multiply(MAX_ABSOLUTE_INTEGER)) > 0) {
            throw new ArithmeticException("Number exceeds the supported range");
        }
    }

    private static boolean approximate(ExactNumber a, ExactNumber b) {
        return a.isApproximate() || b.isApproximate();
    }

    private static BigInteger exactIntegerRoot(BigInteger value, int degree) {
        if (degree == 1 || value.equals(BigInteger.ZERO) || value.equals(BigInteger.ONE)) {
            return value;
        }
        if (degree > value.bitLength()) {
            return null;
        }
        if (degree == 2) {
            BigInteger root = value.sqrt();
            return root.multiply(root).equals(value) ? root : null;
        }
        int rootBits = (value.bitLength() + degree - 1) / degree;
        BigInteger low = BigInteger.ONE;
        BigInteger high = BigInteger.ONE.shiftLeft(rootBits);
        while (low.compareTo(high) <= 0) {
            BigInteger mid = low.add(high).shiftRight(1);
            int comparison = mid.pow(degree).compareTo(value);
            if (comparison == 0) {
                return mid;
            }
            if (comparison < 0) {
                low = mid.add(BigInteger.ONE);
            } else {
                high = mid.subtract(BigInteger.ONE);
            }
        }
        return null;
    }

    private static ExactNumber approximateResult(double value, boolean rejectUnderflow) {
        if (!Double.isFinite(value) || (rejectUnderflow && value == 0.0)) {
            throw new ArithmeticException("Result is outside the supported range");
        }
        return fromDecimal(BigDecimal.valueOf(value));
    }

    private static ExactNumber fromDecimal(BigDecimal value) {
        BigInteger numerator = value.unscaledValue();
        int scale = value.scale();
        if (scale < 0) {
            return checked(numerator.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE, true);
        }
        return checked(numerator, BigInteger.TEN.pow(scale), true);
    }

    private static double toFiniteDouble(ExactNumber value) {
        double result = value.doubleValue();
        if (!Double.isFinite(result) || (value.signum() != 0 && result == 0.0)) {
            throw new ArithmeticException("Number is outside the floating-point range");
        }
        return result;
    }

    private static void validateBase(int base) {
        if (base != 2 && base != 8 && base != 10 && base != 16) {
            throw new IllegalArgumentException("Unsupported base: " + base);
        }
    }
}
