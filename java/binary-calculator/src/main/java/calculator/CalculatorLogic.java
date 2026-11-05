package calculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;

final class CalculatorLogic {
    static final int MAX_INPUT_DIGITS = 2112;
    private static final int MAX_FRACTION_DIGITS = 2048;
    private static final int SIGNIFICANT_DIGITS = 32;
    private static final int MAX_POWER = 16384;
    private static final long MAX_POWER_BITS = 65536;
    private static final String DIGITS = "0123456789ABCDEF";

    // Например, 101.01₂ = 10101₂ / 100₂ = 21 / 4.
    static Fraction parse(String text, int base) {
        checkBase(base);
        if (text == null || text.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        if (text.length() > MAX_INPUT_DIGITS + 2) {
            throw new ArithmeticException("Input is too long");
        }
        int point = text.indexOf('.');
        if (point != text.lastIndexOf('.') || text.indexOf('+') >= 0 || text.indexOf('-') > 0) {
            throw new NumberFormatException("Invalid number");
        }
        String digits = text.replace(".", "");
        int digitCount = digits.length() - (digits.startsWith("-") ? 1 : 0);
        if (digitCount > MAX_INPUT_DIGITS) {
            throw new ArithmeticException("Input is too long");
        }
        if (digitCount == 0) {
            throw new NumberFormatException("No digits");
        }
        int places = point < 0 ? 0 : text.length() - point - 1;
        BigInteger numerator = new BigInteger(digits, base);
        return new Fraction(numerator, BigInteger.valueOf(base).pow(places));
    }

    // Делим целую часть, а затем по одной получаем цифры дробной части.
    static String format(Fraction number, int base) {
        checkBase(base);
        BigInteger[] parts = number.numerator.abs().divideAndRemainder(number.denominator);
        BigInteger whole = parts[0];
        BigInteger remainder = parts[1];
        BigInteger radix = BigInteger.valueOf(base);
        StringBuilder decimals = new StringBuilder();
        int significant = 0;

        while (remainder.signum() != 0 && decimals.length() < MAX_FRACTION_DIGITS
                && significant < SIGNIFICANT_DIGITS) {
            BigInteger[] digit = remainder.multiply(radix).divideAndRemainder(number.denominator);
            decimals.append(DIGITS.charAt(digit[0].intValue()));
            remainder = digit[1];
            if (digit[0].signum() != 0 || significant > 0) {
                significant++;
            }
        }
        if (remainder.shiftLeft(1).compareTo(number.denominator) >= 0 && roundUp(decimals, base)) {
            whole = whole.add(BigInteger.ONE);
        }
        while (!decimals.isEmpty() && decimals.charAt(decimals.length() - 1) == '0') {
            decimals.deleteCharAt(decimals.length() - 1);
        }
        if (number.numerator.signum() != 0 && whole.signum() == 0 && decimals.isEmpty()) {
            throw new ArithmeticException("Number is too small to display");
        }
        String sign = number.numerator.signum() < 0 ? "-" : "";
        String result = sign + whole.toString(base).toUpperCase(Locale.ROOT);
        return decimals.isEmpty() ? result : result + "." + decimals;
    }

    private static boolean roundUp(StringBuilder digits, int base) {
        for (int i = digits.length() - 1; i >= 0; i--) {
            int next = DIGITS.indexOf(digits.charAt(i)) + 1;
            if (next < base) {
                digits.setCharAt(i, DIGITS.charAt(next));
                return false;
            }
            digits.setCharAt(i, '0');
        }
        return true; // Перенос в целую часть, например 0.999... → 1.
    }

    static boolean keepsExactValue(Fraction value, String oldText, String newText, int base) {
        try {
            Fraction displayed = parse(oldText, base);
            return !displayed.sameValue(value) && displayed.sameValue(parse(newText, base));
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return false;
        }
    }

    static Fraction calculate(Fraction a, Fraction b, String operation) {
        BigInteger left = a.numerator.multiply(b.denominator);
        BigInteger right = b.numerator.multiply(a.denominator);
        BigInteger commonDenominator = a.denominator.multiply(b.denominator);
        return switch (operation) {
            case "+" -> new Fraction(left.add(right), commonDenominator);
            case "−" -> new Fraction(left.subtract(right), commonDenominator);
            case "×" -> new Fraction(a.numerator.multiply(b.numerator), commonDenominator);
            case "÷" -> new Fraction(left, right);
            case "mod" -> new Fraction(left.remainder(right), commonDenominator);
            case "^" -> power(a, b);
            case "√" -> root(a, b);
            default -> throw new IllegalArgumentException("Unknown operation");
        };
    }

    static Fraction power(Fraction base, Fraction exponent) {
        if (exponent.numerator.signum() == 0) {
            return integer(BigInteger.ONE); // В том числе 0^0 = 1.
        }
        if (base.numerator.signum() == 0) {
            if (exponent.numerator.signum() < 0) {
                throw new ArithmeticException("Zero to a negative power");
            }
            return integer(BigInteger.ZERO);
        }
        if (base.numerator.signum() < 0 && !exponent.isInteger()) {
            throw new ArithmeticException("Fractional power of a negative number");
        }
        if (base.numerator.abs().equals(base.denominator)) {
            boolean negative = base.numerator.signum() < 0 && exponent.numerator.abs().testBit(0);
            return integer(negative ? BigInteger.ONE.negate() : BigInteger.ONE);
        }
        if (exponent.isInteger()) {
            BigInteger magnitude = exponent.numerator.abs();
            if (magnitude.compareTo(BigInteger.valueOf(MAX_POWER)) > 0) {
                throw new ArithmeticException("Power is too large");
            }
            int n = magnitude.intValue();
            if ((long) base.numerator.abs().bitLength() * n > MAX_POWER_BITS
                    || (long) base.denominator.bitLength() * n > MAX_POWER_BITS) {
                throw new ArithmeticException("Power is too large");
            }
            BigInteger numerator = base.numerator.pow(n);
            BigInteger denominator = base.denominator.pow(n);
            return exponent.numerator.signum() < 0
                    ? new Fraction(denominator, numerator) : new Fraction(numerator, denominator);
        }
        return fromDouble(Math.pow(toDouble(base), toDouble(exponent)));
    }

    // Первое число — степень корня, второе — подкоренное число.
    static Fraction root(Fraction degree, Fraction number) {
        if (degree.numerator.signum() == 0) {
            throw new ArithmeticException("Root degree is zero");
        }
        boolean negative = number.numerator.signum() < 0;
        if (negative && (!degree.isInteger() || !degree.numerator.abs().testBit(0))) {
            throw new ArithmeticException("Invalid root of a negative number");
        }
        if (number.numerator.signum() == 0) {
            if (degree.numerator.signum() < 0) {
                throw new ArithmeticException("Negative root of zero");
            }
            return integer(BigInteger.ZERO);
        }
        if (number.numerator.abs().equals(number.denominator)) {
            return integer(negative ? BigInteger.ONE.negate() : BigInteger.ONE);
        }
        if (!negative && !degree.isInteger() && degree.numerator.abs().equals(BigInteger.ONE)) {
            BigInteger exponent = degree.numerator.signum() < 0 ? degree.denominator.negate() : degree.denominator;
            return power(number, integer(exponent));
        }
        if (degree.isInteger() && degree.numerator.abs().compareTo(BigInteger.valueOf(MAX_POWER)) <= 0) {
            int n = degree.numerator.abs().intValue();
            BigInteger numerator = exactRoot(number.numerator.abs(), n);
            BigInteger denominator = exactRoot(number.denominator, n);
            if (numerator != null && denominator != null) {
                if (negative) {
                    numerator = numerator.negate();
                }
                return degree.numerator.signum() < 0
                        ? new Fraction(denominator, numerator) : new Fraction(numerator, denominator);
            }
        }
        double result = Math.pow(Math.abs(toDouble(number)), 1.0 / toDouble(degree));
        return fromDouble(negative ? -result : result);
    }

    // Двоичным поиском проверяем, существует ли точный целый корень.
    private static BigInteger exactRoot(BigInteger value, int degree) {
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
            BigInteger middle = low.add(high).shiftRight(1);
            int comparison = middle.pow(degree).compareTo(value);
            if (comparison == 0) {
                return middle;
            }
            if (comparison < 0) {
                low = middle.add(BigInteger.ONE);
            } else {
                high = middle.subtract(BigInteger.ONE);
            }
        }
        return null;
    }

    private static Fraction integer(BigInteger number) {
        return new Fraction(number, BigInteger.ONE);
    }

    private static void checkBase(int base) {
        if (base != 2 && base != 8 && base != 10 && base != 16) {
            throw new IllegalArgumentException("Unsupported base");
        }
    }

    // Дробные степени и нецелые корни рассчитываются через Math.pow.
    private static double toDouble(Fraction number) {
        double value = new BigDecimal(number.numerator)
                .divide(new BigDecimal(number.denominator), MathContext.DECIMAL128).doubleValue();
        if (!Double.isFinite(value) || (number.numerator.signum() != 0 && value == 0.0)) {
            throw new ArithmeticException("Number is outside the floating-point range");
        }
        return value;
    }

    private static Fraction fromDouble(double value) {
        if (!Double.isFinite(value) || value == 0.0) {
            throw new ArithmeticException("Result is outside the floating-point range");
        }
        return parse(BigDecimal.valueOf(value).toPlainString(), 10);
    }
}
