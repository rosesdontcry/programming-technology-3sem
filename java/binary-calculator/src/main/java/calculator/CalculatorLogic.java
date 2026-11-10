package calculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class CalculatorLogic {
    private static final String DIGITS = "0123456789ABCDEF";


    static Fraction parse(String text, int base) {
        if (text == null || text.isEmpty()) {
            throw new NumberFormatException("empty input");
        }

        int point = text.indexOf('.');

        if (point != text.lastIndexOf('.')) {
            throw new NumberFormatException("invalid number");
        }

        String digits = text.replace(".", "");
        int digitCount = digits.length() - (digits.startsWith("-") ? 1 : 0);
        if (digitCount == 0) {
            throw new NumberFormatException("no digits");
        }

        int places = point < 0 ? 0 : text.length() - point - 1;
        BigInteger numerator = new BigInteger(digits, base);
        return new Fraction(numerator, BigInteger.valueOf(base).pow(places));
    }

    static String format(Fraction number, int base) {
        BigInteger[] parts = number.numerator.abs().divideAndRemainder(number.denominator);
        BigInteger whole = parts[0];
        BigInteger reminder = parts[1];

        BigInteger radix = BigInteger.valueOf(base);

        StringBuilder decimals = new StringBuilder();

        while (reminder.signum() != 0 && decimals.length() < 20) {
            BigInteger[] digit = reminder.multiply(radix).divideAndRemainder(number.denominator);
            decimals.append(DIGITS.charAt(digit[0].intValue()));
            reminder = digit[1];
        }
        String sign = number.numerator.signum() < 0 ? "-": "";
        String result = sign + whole.toString(base).toUpperCase();
        return decimals.isEmpty() ? result : result + "." + decimals;
    }

    static Fraction power(Fraction base, Fraction exponent) {
        if (!exponent.isInteger()) {
            throw new ArithmeticException("exponent must be an integer");
        }

        int n = exponent.numerator.abs().intValueExact();
        BigInteger numerator = base.numerator.pow(n);
        BigInteger denominator = base.denominator.pow(n);

        return exponent.numerator.signum() < 0
                ? new Fraction(denominator, numerator)
                : new Fraction(numerator, denominator);
    }

    private static double toDouble(Fraction number) {
        return new BigDecimal(number.numerator)
                .divide(new BigDecimal(number.denominator), MathContext.DECIMAL128)
                        .doubleValue();
    }

    private static Fraction fromDouble(double value) {
        if (!Double.isFinite(value)) {
            throw new ArithmeticException("result is not a finite number");
        }
        return parse(BigDecimal.valueOf(value).toPlainString(), 10);
    }

    static Fraction root(Fraction number, Fraction degree) {
        double result = Math.pow(toDouble(number), 1.0 / toDouble(degree));
        return fromDouble(result);
    }

    static Fraction calculate(Fraction a, Fraction b, String operation) {
        BigInteger left = a.numerator.multiply(b.denominator);
        BigInteger right = b.numerator.multiply(a.denominator);
        BigInteger commonDenominator = a.denominator.multiply(b.denominator);
        return switch (operation){
            case "+" -> new Fraction(left.add(right), commonDenominator);
            case "-" -> new Fraction(left.subtract(right), commonDenominator);
            case "*" -> new Fraction(a.numerator.multiply(b.numerator), commonDenominator);
            case "/" -> new Fraction(left, right);
            case "mod" -> new Fraction(left.remainder(right), commonDenominator);
            case "^" -> power(a, b);
            case "√" -> root(b, a);
            default -> throw new IllegalArgumentException("unknown operation");
        };
    }
}