package calculator;

import java.math.BigInteger;

final class Fraction {
    private static final BigInteger LIMIT = BigInteger.ONE.shiftLeft(53);
    final BigInteger numerator;
    final BigInteger denominator;

    Fraction(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) throw new ArithmeticException("division by zero");
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }

        BigInteger divisor = numerator.gcd(denominator);
        this.numerator = numerator.divide(divisor);
        this.denominator = denominator.divide(divisor);
        if (this.numerator.abs().compareTo(this.denominator.multiply(LIMIT)) > 0) {
            throw new ArithmeticException("result is too large");
        }
    }

    boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    boolean sameValue(Fraction other) {
        return numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }
}

