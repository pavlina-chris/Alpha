// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * load */
public class load
{
    private Counter counter;
    private Function function;
    private String result;
    private boolean _volatile;
    private String type;
    private String pointer;
    private int alignment;

    public load (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
        _volatile = false;
        alignment = 0;
    }

    /**
     * Required: Set pointer
     * @param type Pointer type, omitting *
     * @param pointer Pointer */
    public load pointer (String type, String pointer) {
        this.type = type;
        this.pointer = pointer;
        return this;
    }

    /**
     * Set alignment
     */
    public load alignment (int align) {
        alignment = align;
        return this;
    }

    /**
     * Set volatility */
    public load _volatile (boolean _volatile) {
        this._volatile = _volatile;
        return this;
    }

    /**
     * Set result register */
    public load result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        String text = String.format ("%s =%s load %s* %s",
                                     result,
                                     _volatile ? " volatile" : "",
                                     type, pointer);
        if (alignment != 0) {
            text += String.format (", align %d", alignment);
        }
        text += "\n";

        function.add (text);

        return result;
    }
}
