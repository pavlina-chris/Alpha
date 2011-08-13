// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * store */
public class store
{
    private Function function;
    private String pointer;
    private String value;
    private String type;
    private int align;
    private boolean _volatile;

    public store (Counter counter, Function function) {
        this.function = function;
        align = 0;
        _volatile = false;
    }

    /**
     * Required: Set the pointer */
    public store pointer (String pointer) {
        this.pointer = pointer;
        return this;
    }

    /**
     * Required: Set the value and type */
    public store value (String type, String value) {
        this.type = type;
        this.value = value;
        return this;
    }

    /**
     * Set the alignment */
    public store alignment (int alignment) {
        this.align = alignment;
        return this;
    }

    /**
     * Set whether the operation is volatile */
    public store _volatile (boolean _volatile) {
        this._volatile = _volatile;
        return this;
    }

    public void build () {
        StringBuilder sb = new StringBuilder ();
        if (_volatile) sb.append ("volatile ");
        sb.append ("store ");
        sb.append (type).append (' ');
        sb.append (value).append (", ");
        sb.append (type).append ("* ");
        sb.append (pointer);
        if (align != 0)
            sb.append (", align ").append (align);
        sb.append ('\n');
        function.add (sb.toString ());
    }
}
