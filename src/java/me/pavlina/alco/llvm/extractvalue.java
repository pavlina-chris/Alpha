// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * extractvalue */
public class extractvalue
{
    private Counter counter;
    private Function function;
    private String result;
    private String type;
    private String value;
    private int index;
    private int[] indices;

    public extractvalue (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set value */
    public extractvalue value (String type, String value) {
        this.type = type;
        this.value = value;
        return this;
    }

    /**
     * Required: Set indices
     * @param index First index
     * @param indices Further indices into the type */
    public extractvalue indices (int index, int... indices) {
        this.index = index;
        this.indices = indices;
        return this;
    }

    /**
     * Set result register */
    public extractvalue result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        StringBuilder sb = new StringBuilder
            (String.format
             ("%s = extractvalue %s %s, %d", result, type, value, index));
        if (indices != null)
            for (int i: indices) {
                sb.append (", ");
                sb.append (i);
            }

        function.add (sb.toString ());

        return result;
    }
}
