// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Integer comparison */
public class icmp
{
    private Counter counter;
    private Function function;
    private Icmp comparison;
    private String result;
    private String type;
    private String op1;
    private String op2;

    public icmp (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set comparison */
    public icmp comparison (Icmp comparison) {
        this.comparison = comparison;
        return this;
    }

    /**
     * Required: Set result and operand type */
    public icmp type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set operands */
    public icmp operands (String op1, String op2) {
        this.op1 = op1;
        this.op2 = op2;
        return this;
    }

    /**
     * Set result register */
    public icmp result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        function.add (String.format ("%s = icmp %s %s %s, %s\n",
                                     result, comparison, type, op1, op1));

        return result;
    }

    /**
     * All possible comparisons. */
    public enum Icmp {
        EQ, NE, UGT, UGE, ULT, ULE, SGT, SGE, SLT, SLE;

        public String toString () {
            return super.toString ().toLowerCase ();
        }
    }
}
