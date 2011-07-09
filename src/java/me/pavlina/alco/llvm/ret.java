// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * ret */
public class ret
{
    private Counter counter;
    private Function function;
    private String type;
    private String value;

    public ret (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Set return type and value. Return is void if this is not done. */
    public ret value (String type, String value) {
        this.type = type;
        this.value = value;
        return this;
    }

    public void build () {
        if (type == null)
            function.add ("ret void\n");
        else
            function.add (String.format ("ret %s %s\n", type, value));
    }
}
