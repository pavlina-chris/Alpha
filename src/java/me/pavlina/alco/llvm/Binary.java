// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Binary operators */
public class Binary
{
    private Counter counter;
    private Function function;
    private String result;
    private BinOp op;
    private String type;
    private String op1;
    private String op2;

    public Binary (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set operation */
    public Binary operation (BinOp op) {
        this.op = op;
        return this;
    }

    /**
     * Set result register */
    public Binary result (String result) {
        this.result = result;
        return this;
    }

    /**
     * Required: Set operand and result type */
    public Binary type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set operands */
    public Binary operands (String op1, String op2) {
        this.op1 = op1;
        this.op2 = op2;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        String text = String.format ("%s = %s %s %s, %s\n", result, op,
                                     type, op1, op2);
        function.add (text);

        return result;
    }

    /**
     * All possible binary operators. */
    public enum BinOp {
        ADD, ADD_NUW, ADD_NSW, ADD_NUW_NSW, FADD,
            SUB, SUB_NUW, SUB_NSW, SUB_NUW_NSW, FSUB,
            MUL, MUL_NUW, MUL_NSW, MUL_NUW_NSW, FMUL,
            UDIV, UDIV_EXACT, SDIV, SDIV_EXACT, FDIV,
            UREM, SREM, FREM,
            SHL, SHL_NUW, SHL_NSW, SHL_NUW_NSW,
            LSHR, LSHR_EXACT,
            ASHR, ASHR_EXACT,
            AND, OR, XOR;

        public String toString () {
            return super.toString ().toLowerCase ().replace ('_', ' ');
        }
    }

}
