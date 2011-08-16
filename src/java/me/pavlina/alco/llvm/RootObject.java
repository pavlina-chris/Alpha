// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Any object that sits at the root of an LLVM file. */
public abstract class RootObject implements Comparable<RootObject> {

    /**
     * Return the "level" of the object. Objects with lower levels will be
     * emitted before objects with higher levels. For example, type declarations
     * have level 10, and go before functions (level 30). */
    public abstract int getLevel ();

    /**
     * Return whether this object needs an indentifier */
    public abstract boolean needsId ();

    /**
     * Set this object's indentifier. If needsId() returns false,
     * this should be a no-op. */
    public abstract void setId (String id);

    /**
     * Get this object's identifier. This should throw a
     * RuntimeException if needsId() returns false. */
    public abstract String getId ();

    /**
     * Get this object's type. This should return null if needsId() returns
     * false. */
    public abstract String getType ();

    /**
     * Get the string representation of this object. */
    public abstract String toString ();

    public int compareTo (RootObject o) {
        return getLevel () - o.getLevel ();
    }

    public static final int LEVEL_TYPE = 10;
    public static final int LEVEL_GLOBAL = 20;
    public static final int LEVEL_FUNCTION = 30;
    public static final int LEVEL_DEBUG = 40;

}
