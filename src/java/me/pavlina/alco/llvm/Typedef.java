// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Typedef. There is no object-oriented representation of a type itself, so you
 * must specify this as a string. */
public class Typedef extends RootObject {

    String name, text;

    /**
     * Make a typedef.
     * @param name Typedef name. Should start with %
     * @param value Typedef value. Should be valid LLVM. Omit the "type"
     * prefix. */
    public Typedef (String name, String value) {
        this.name = name;
        this.text = value;
    }

    public int getLevel () { return RootObject.LEVEL_TYPE; }
    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }
    public String toString () {
        return name + " = type " + text + "\n";
    }

}
