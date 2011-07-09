// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Float comparison */
public class fcmp
{
    private Counter counter;
    private Function function;
    private Fcmp comparison;
    private String result;
    private String type;
    private String op1;
    private String op2;

    public fcmp (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set comparison */
    public fcmp comparison (Fcmp comparison) {
        this.comparison = comparison;
        return this;
    }

    /**
     * Required: Set result and operand type */
    public fcmp type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set operands */
    public fcmp operands (String op1, String op2) {
        this.op1 = op1;
        this.op2 = op2;
        return this;
    }

    /**
     * Set result register */
    public fcmp result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        function.add (String.format ("%s = fcmp %s %s %s, %s\n",
                                     result, comparison, type, op1, op1));

        return result;
    }

    /**
     * All possible comparisons. */
    public enum Fcmp {
        FALSE, TRUE,
            OEQ, OGT, OGE, OLT, OLE, ONE, ORD,
            UEQ, UGT, UGE, ULT, ULE, UNE, UNO;

        public String toString () {
            return super.toString ().toLowerCase ();
        }
    }
}
