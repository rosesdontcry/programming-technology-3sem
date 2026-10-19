package calculator.logic;

public final class CalculatorLogic {

    private static final int MAX_FRACTION_DIGITS = 5;

    private CalculatorLogic() {
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static double subtract(double a, double b) {
        return a - b;
    }

    public static double divine(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero")
        }
        return a / b;
    }


}
