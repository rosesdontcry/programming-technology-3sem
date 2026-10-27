package calculator.ui;

import calculator.logic.CalculatorLogic;

public enum Operation {
    ADD("+") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.add(a, b);
        }
    },
    SUBTRACT("−") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.subtract(a, b);
        }
    },
    MULTIPLY("×") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.multiply(a, b);
        }
    },
    DIVIDE("÷") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.divide(a, b);
        }
    },
    MOD("mod") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.mod(a, b);
        }
    },
    ROOT("√") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.root(a, b);
        }
    },
    POWER("^") {
        @Override
        public double apply(double a, double b) {
            return CalculatorLogic.power(a, b);
        }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public abstract double apply(double a, double b);
}