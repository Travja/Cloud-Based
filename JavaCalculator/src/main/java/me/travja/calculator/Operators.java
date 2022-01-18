package me.travja.calculator;

public class Operators {

    public static class AddOperation implements IOperation {
        @Override
        public double execute(double operand1, double operand2) {
            return operand1 + operand2;
        }
    }

    public static class SubtractOperation implements IOperation {
        @Override
        public double execute(double operand1, double operand2) {
            return operand1 - operand2;
        }
    }

    public static class MultiplyOperation implements IOperation {
        @Override
        public double execute(double operand1, double operand2) {
            return operand1 * operand2;
        }
    }

    public static class DivideOperation implements IOperation {
        @Override
        public double execute(double operand1, double operand2) {
            return operand1 / operand2;
        }
    }

}
