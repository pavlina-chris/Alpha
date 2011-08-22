// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * unreachable */
public class UNREACHABLE implements Terminator {

    public UNREACHABLE () {}

    public String toString () {
        return "unreachable\n";
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }

}
