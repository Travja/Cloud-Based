package me.travja.calculator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static me.travja.calculator.Operators.*;

public class CalcHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {
    private static final String
            OPERAND_1 = "operand1",
            OPERAND_2 = "operand2",
            OPERATION = "operation";

    private enum Operation {
        ADD(new AddOperation()),
        SUB(new SubtractOperation()),
        MUL(new MultiplyOperation()),
        DIV(new DivideOperation());

        @Getter private final IOperation operation;

        Operation(IOperation operation) {
            this.operation = operation;
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {

        double    operand1 = Double.parseDouble(event.getOrDefault(OPERAND_1, "0"));
        double    operand2 = Double.parseDouble(event.getOrDefault(OPERAND_2, "0"));
        Operation op       = Operation.valueOf(event.getOrDefault(OPERATION, "add").toUpperCase());

        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", 200);
        res.put("result", op.getOperation().execute(operand1, operand2));
        return res;
    }
}
