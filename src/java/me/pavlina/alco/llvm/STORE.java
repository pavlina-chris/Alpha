// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * store */
public class STORE implements Instruction {
    String pointer, value, type;
    Instruction iPointer, iValue;
    int align;
    boolean _volatile;

    public STORE () {}

    /**
     * Required: Set the pointer */
    public STORE pointer (String p) { pointer = p; return this; }

    /**
     * Set the pointer */
    public STORE pointer (Instruction i) { iPointer = i; return this; }

    /**
     * Required: Set the value */
    public STORE value (String v) { value = v; return this; }

    /**
     * Required: Set the type */
    public STORE type (String t) { type = t; return this; }

    /**
     * Set the value and type */
    public STORE value (Instruction i) { iValue = i; return this; }

    /**
     * Set the alignment */
    public STORE alignment (int a) { align = a; return this; }

    /**
     * Set whether the operation is volatile */
    public STORE _volatile (boolean v) { _volatile = v; return this; }

    public String toString () {
        if (iPointer != null) pointer = iPointer.getId ();
        if (iValue != null) {
            value = iValue.getId ();
            type = iValue.getType ();
        }
        StringBuilder sb = new StringBuilder ();
        if (_volatile) sb.append ("volatile ");
        sb.append ("store ");
        sb.append (type).append (' ').append (value).append (", ")
            .append (type).append ("* ").append (pointer);
        if (align != 0)
            sb.append (", align ").append (align);
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }

}
