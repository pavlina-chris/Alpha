// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * This base class represents an LLVM code object that is valid at the root of
 * the file (is not nested inside anything). */
public abstract class RootCodeObj extends CodeObj
    implements Comparable<RootCodeObj>
{
    /**
     * Return the "level" of the object. Objects with lower levels will be
     * emitted before objects with higher levels. For example, type declarations
     * have level 10, and go before methods (level 30). */
    public abstract int getLevel ();

    public int compareTo (RootCodeObj o) {
        return getLevel () - o.getLevel ();
    }

    public static final int LEVEL_TYPE = 10;
    public static final int LEVEL_GLOBAL = 20;
    public static final int LEVEL_FUNCTION = 30;
    public static final int LEVEL_DEBUG = 40;
}
