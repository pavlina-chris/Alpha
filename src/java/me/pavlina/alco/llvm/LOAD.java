// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * load */
public class LOAD implements Instruction {

    String type, pointer, id;
    Instruction iPointer;
    boolean _volatile;
    int alignment;

    public LOAD () {}

    /**
     * Required: Type of value */
    public LOAD type (String t) { type = t; return this; }

    /**
     * Required: Pointer */
    public LOAD pointer (String p) { pointer = p; return this; }

    /**
     * Set pointer */
    public LOAD pointer (Instruction i) { iPointer = i; return this; }

    /**
     * Set alignment */
    public LOAD alignment (int a) { alignment = a; return this; }

    /**
     * Set volatility */
    public LOAD _volatile (boolean v) { _volatile = v; return this; }

    public String toString () {
        if (iPointer != null) pointer = iPointer.getId ();
        StringBuilder sb = new StringBuilder ();
        sb.append (id).append (" = ");
        if (_volatile) sb.append ("volatile ");
        sb.append ("load ").append (type).append ("* ").append (pointer);
        if (alignment != 0)
            sb.append (", align ").append (alignment);
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type; }

}
