package calculator.logic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

/** An immutable rational value, optionally produced by an approximate operation. */
public final class ExactNumber {
    private final BigInteger numerator;
    private final BigInteger denominator;
    private final boolean approximate;

    private ExactNumber(BigInteger numerator, BigInteger denominator, boolean approximate) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger divisor = numerator.gcd(denominator);
        this.numerator = numerator.divide(divisor);
        this.denominator = denominator.divide(divisor);
        this.approximate = approximate;
    }

    static ExactNumber of(BigInteger numerator, BigInteger denominator, boolean approximate) {
        return new ExactNumber(numerator, denominator, approximate);
    }

    public BigInteger numerator() {
        return numerator;
    }

    public BigInteger denominator() {
        return denominator;
    }

    public boolean isApproximate() {
        return approximate;
    }

    public boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    public int signum() {
        return numerator.signum();
    }

    public boolean hasSameValue(ExactNumber other) {
        return other != null && numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }

    double doubleValue() {
        return new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), MathContext.DECIMAL128)
                .doubleValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExactNumber other)) {
            return false;
        }
        return approximate == other.approximate && hasSameValue(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator, approximate);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator + (approximate ? " (approximate)" : "");
    }
}
