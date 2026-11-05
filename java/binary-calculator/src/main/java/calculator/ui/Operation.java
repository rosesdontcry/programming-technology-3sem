package calculator.ui;

import calculator.logic.CalculatorLogic;
import calculator.logic.ExactNumber;

public enum Operation {
    ADD("+") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.add(a, b);
        }
    },
    SUBTRACT("−") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.subtract(a, b);
        }
    },
    MULTIPLY("×") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.multiply(a, b);
        }
    },
    DIVIDE("÷") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.divide(a, b);
        }
    },
    MOD("mod") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.mod(a, b);
        }
    },
    ROOT("√") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
            return CalculatorLogic.root(a, b);
        }
    },
    POWER("^") {
        @Override
        public ExactNumber apply(ExactNumber a, ExactNumber b) {
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

    public abstract ExactNumber apply(ExactNumber a, ExactNumber b);
}
