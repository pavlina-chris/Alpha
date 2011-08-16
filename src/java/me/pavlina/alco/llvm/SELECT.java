// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * select */
public class SELECT implements Instruction {

    String type, cond, ifTrue, ifFalse, id;
    Instruction iCond, iTrue, iFalse;

    public SELECT () {}

    /**
     * Required: Set the condition. The condition must be of type i1. */
    public SELECT cond (String c) { cond = c; return this; }

    /**
     * Set the condition. */
    public SELECT cond (Instruction i) { iCond = i; return this; }

    /**
     * Required: Set the value type */
    public SELECT type (String t) { type = t; return this; }

    /**
     * Required: Set the value if true */
    public SELECT T (String v) { ifTrue = v; return this; }

    /**
     * Set the value if true */
    public SELECT T (Instruction i) { iTrue = i; return this; }

    /**
     * Required: Set the value if false */
    public SELECT F (String v) { ifFalse = v; return this; }

    /**
     * Set the value if false */
    public SELECT F (Instruction i) { iFalse = i; return this; }

    public String toString () {
        if (iTrue != null) ifTrue = iTrue.getId ();
        if (iFalse != null) ifFalse = iFalse.getId ();
        if (iCond != null) cond = iCond.getId ();
        return String.format ("%s = select i1 %s, %s %s, %s %s\n",
                              id, cond, type, ifTrue, type, ifFalse);
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type; }

}
