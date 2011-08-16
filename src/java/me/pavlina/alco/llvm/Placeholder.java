// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * LLVM string placeholder. This is a blank instruction whose ID can be set to
 * anything you like. */
public class Placeholder implements Instruction {

    String id;
    String type;

    public Placeholder (String id, String type) {
        this.id = id;
        this.type = type;
    }

    public boolean needsId () { return false; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type; }
    public String toString () { return ""; }

}
