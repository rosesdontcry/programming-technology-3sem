package calculator;

import java.math.BigInteger;

public class CalculatorLogicTest {
    public static void main(String[] args) {
        String f = CalculatorLogic.format(new Fraction(BigInteger.valueOf(5), BigInteger.valueOf(1)), 10);
        System.out.println(f);

    }
}
