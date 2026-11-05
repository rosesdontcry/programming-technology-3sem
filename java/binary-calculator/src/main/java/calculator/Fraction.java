package calculator;

import java.math.BigInteger;

/** Число хранится как обыкновенная дробь: числитель / знаменатель. */
final class Fraction {
    private static final BigInteger LIMIT = BigInteger.ONE.shiftLeft(53);
    final BigInteger numerator;
    final BigInteger denominator;

    Fraction(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        // Сокращение дроби помогает сравнивать числа и не раздувать вычисления.
        BigInteger divisor = numerator.gcd(denominator);
        this.numerator = numerator.divide(divisor);
        this.denominator = denominator.divide(divisor);
        if (this.numerator.abs().compareTo(this.denominator.multiply(LIMIT)) > 0) {
            throw new ArithmeticException("Result is too large");
        }
    }

    boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    boolean sameValue(Fraction other) {
        return numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }
}
