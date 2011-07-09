// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Conversion operators */
public class Conversion
{
    private Counter counter;
    private Function function;
    private ConvOp op;
    private String result;
    private String stype;
    private String source;
    private String dtype;

    public Conversion (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set operation */
    public Conversion operation (ConvOp op) {
        this.op = op;
        return this;
    }

    /**
     * Required: Source value */
    public Conversion source (String type, String value) {
        this.stype = type;
        this.source = value;
        return this;
    }

    /**
     * Required: Destination type */
    public Conversion dest (String type) {
        this.dtype = type;
        return this;
    }

    /**
     * Set result register */
    public Conversion result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        function.add (String.format ("%s = %s %s %s to %s\n",
                                     result, op, stype, source, dtype));

        return result;
    }

    /**
     * All possible conversion operators. */
    public enum ConvOp {
        TRUNC, ZEXT, SEXT, FPTRUNC, FPEXT, FPTOUI, FPTOSI, UITOFP, SITOFP,
            PTRTOINT, INTTOPTR, BITCAST;

        public String toString () {
            return super.toString ().toLowerCase ();
        }
    }
}
