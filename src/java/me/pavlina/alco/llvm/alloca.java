// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * alloca */
public class alloca
{
    private Counter counter;
    private Function function;
    private String result;
    private String type;
    private String nElemType;
    private String numElements;
    private int alignment;

    public alloca (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
        this.type = null;
        this.nElemType = null;
        this.numElements = null;
        this.alignment = 0;
    }

    /**
     * Required: Type of value to go in memory */
    public alloca type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Set result register */
    public alloca result (String result) {
        this.result = result;
        return this;
    }

    /**
     * Allocate an array.
     * @param type Type of 'number'
     * @param number Number of elements
     */
    public alloca elements (String type, String number) {
        this.nElemType = type;
        this.numElements = number;
        return this;
    }

    /**
     * Set alignment */
    public alloca align (int alignment) {
        this.alignment = alignment;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        String text = String.format ("%s = alloca %s", result, type);
        if (nElemType != null)
            text += String.format (", %s %s", nElemType, numElements);
        if (alignment != 0)
            text += String.format (", align %d", alignment);
        text += "\n";

        function.add (text);
        return result;
    }
}
