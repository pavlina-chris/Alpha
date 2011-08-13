// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Label */
public class label
{
    Function function;
    String name;

    public label (Counter counter, Function function) {
        this.function = function;
    }

    /**
     * Required: Set name */
    public label name (String name) {
        this.name = name;
        return this;
    }

    public void build () {
        function.add ("\n" + name + ":\n");
    }
}
