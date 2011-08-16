// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * LLVM instruction */
public interface Instruction {

    /**
     * Return whether this instruction needs a return identifier */
    public boolean needsId ();

    /**
     * Set this instruction's return identifier. If needsId() returns false,
     * this should be a no-op. */
    public void setId (String id);

    /**
     * Get this instruction's return identifier. This should throw a
     * RuntimeException if needsId() returns false. */
    public String getId ();

    /**
     * Get this instruction's return type. This should return null if
     * needsId() returns false. */
    public String getType ();

    /**
     * Get the string representation of this instruction. */
    public String toString ();

}
