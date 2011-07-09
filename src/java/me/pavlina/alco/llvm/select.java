// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * select */
public class select
{
    private Counter counter;
    private Function function;
    private String result;
    private String cond;
    private String type;
    private String ifTrue;
    private String ifFalse;

    public select (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set the condition. The condition must be of type i1. */
    public select cond (String cond) {
        this.cond = cond;
        return this;
    }

    /**
     * Required: Set the value type. */
    public select type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set the values. */
    public select values (String ifTrue, String ifFalse) {
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        return this;
    }

    /**
     * Set the result register */
    public select result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        function.add (String.format ("%s = select i1 %s, %s %s, %s %s\n",
                                     result, cond, type, ifTrue,
                                     type, ifFalse));

        return result;
    }
}
