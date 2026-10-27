package calculator.logic;


public final class CalculatorLogic {

    private static final int MAX_FRACTION_DIGITS = 15;
    private static final String DIGITS = "0123456789ABCDEF";
    private static final double MAX_SAFE_INTEGER = 9_007_199_254_740_992.0;

    private CalculatorLogic() {
    }


    public static double add(double a, double b) {
        return a + b;
    }

    public static double subtract(double a, double b) {
        return a - b;
    }

    public static double multiply(double a, double b) {
        return a * b;
    }

    public static double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }

    public static double mod(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a % b;
    }

    public static double power(double base, double exponent) {
        if (base == 0 && exponent < 0) {
            throw new ArithmeticException("Zero cannot be raised to negative power");
        }
        if (base < 0 && exponent != Math.floor(exponent)) {
            throw new ArithmeticException("Negative base with fractional exponent produces complex result");
        }

        double result = Math.pow(base, exponent);

        if (Double.isInfinite(result)) {
            throw new ArithmeticException("Result overflow: number too large");
        }

        return result;
    }

    public static double root(double degree, double number) {
        if (degree == 0) {
            throw new ArithmeticException("Root degree cannot be zero");
        }
        if (number < 0) {
            throw new ArithmeticException("Cannot compute root of negative number");
        }

        double result = Math.pow(number, 1.0 / degree);

        if (Double.isInfinite(result) || Double.isNaN(result)) {
            throw new ArithmeticException("Invalid root operation");
        }

        return result;
    }

    public static double parseNumber(String numberString, int base) {
        validateBase(base);

        if (numberString == null || numberString.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }

        boolean isNegative = numberString.startsWith("-");
        String unsignedString = isNegative ? numberString.substring(1) : numberString;

        if (unsignedString.isEmpty()) {
            throw new NumberFormatException("No digits found");
        }

        String[] parts = unsignedString.toUpperCase().split("\\.", -1);

        if (parts.length > 2) {
            throw new NumberFormatException("Multiple decimal points");
        }

        boolean hasWholeDigits = !parts[0].isEmpty();
        boolean hasFractionalDigits = parts.length > 1 && !parts[1].isEmpty();

        if (!hasWholeDigits && !hasFractionalDigits) {
            throw new NumberFormatException("No digits found");
        }

        double wholePart = hasWholeDigits ? parseWholePart(parts[0], base) : 0;
        double fractionalValue = hasFractionalDigits ? parseFractionalPart(parts[1], base) : 0;

        double result = wholePart + fractionalValue;
        return isNegative ? -result : result;
    }

    public static String toStringInBase(double number, int base) {
        validateBase(base);

        if (Math.abs(number) > MAX_SAFE_INTEGER) {
            throw new ArithmeticException("Number too large to represent precisely: " + number);
        }

        boolean isNegative = number < 0;
        number = Math.abs(number);

        long wholePart = (long) number;
        double fractionalPart = number - wholePart;

        StringBuilder result = new StringBuilder();
        result.append(convertWholePart(wholePart, base));

        if (fractionalPart > 0) {
            result.append(".");
            result.append(convertFractionalPart(fractionalPart, base));
        }

        return isNegative ? "-" + result : result.toString();
    }

    public static boolean isValidDigitForBase(char digit, int base) {
        int digitValue = DIGITS.indexOf(Character.toUpperCase(digit));
        return digitValue != -1 && digitValue < base;
    }

    private static double parseWholePart(String wholeString, int base) {
        double result = 0;
        for (char c : wholeString.toCharArray()) {
            int digitValue = DIGITS.indexOf(c);
            if (digitValue == -1 || digitValue >= base) {
                throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
            }
            result = result * base + digitValue;
        }
        return result;
    }

    private static double parseFractionalPart(String fractionString, int base) {
        double result = 0;
        double positionValue = 1.0 / base;

        for (char c : fractionString.toCharArray()) {
            int digitValue = DIGITS.indexOf(c);
            if (digitValue == -1 || digitValue >= base) {
                throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
            }
            result += digitValue * positionValue;
            positionValue /= base;
        }

        return result;
    }

    private static String convertWholePart(long number, int base) {
        if (number == 0) {
            return "0";
        }

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            int digit = (int) (number % base);
            result.insert(0, DIGITS.charAt(digit));
            number /= base;
        }

        return result.toString();
    }

    private static String convertFractionalPart(double fractionalPart, int base) {
        StringBuilder result = new StringBuilder();
        int digitsCount = 0;

        while (fractionalPart > 0 && digitsCount < MAX_FRACTION_DIGITS) {
            fractionalPart *= base;
            int digit = (int) fractionalPart;
            result.append(DIGITS.charAt(digit));
            fractionalPart -= digit;
            digitsCount++;
        }

        return result.toString();
    }

    private static void validateBase(int base) {
        if (base != 2 && base != 8 && base != 10 && base != 16) {
            throw new IllegalArgumentException("Unsupported base: " + base);
        }
    }
}
