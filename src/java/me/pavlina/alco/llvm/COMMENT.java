// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Comment */
public class COMMENT implements Instruction {
    
    String text;

    public COMMENT () {}

    /**
     * Required: Comment text. Note that this does not check for newlines. */
    public COMMENT text (String t) { text = t; return this; }

    public String toString () {
        return ";### " + text + "\n";
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }
}
