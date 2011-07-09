// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * insertvalue */
public class insertvalue
{
    private Counter counter;
    private Function function;
    private String result;
    private String dtype;
    private String stype;
    private String dest;
    private String source;
    private int index;
    private int[] indices;

    public insertvalue (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set destination value */
    public insertvalue dest (String type, String value) {
        this.dtype = type;
        this.dest = value;
        return this;
    }

    /**
     * Required: Set source value */
    public insertvalue source (String type, String value) {
        this.stype = type;
        this.source = value;
        return this;
    }

    /**
     * Required: Set indices
     * @param index First index
     * @param indices Further indices */
    public insertvalue indices (int index, int... indices) {
        this.index = index;
        this.indices = indices;
        return this;
    }

    /**
     * Set result register */
    public insertvalue result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }
        StringBuilder sb = new StringBuilder
            (String.format
             ("%s = insertvalue %s %s, %s %s, %d",
              result, dtype, dest, stype, source, index));
        for (int i: indices) {
            sb.append (", ");
            sb.append (i);
        }
        sb.append ('\n');

        function.add (sb.toString ());

        return result;
    }
}
