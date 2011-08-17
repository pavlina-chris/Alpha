// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.llvm.Block;

/**
 * Loop parent class. */
public abstract class Loop extends Statement
{

    /**
     * Return the LLVM label for the continuation of the loop.
     * The 'continue' statement uses this. This method may only be expected
     * to work during genLLVM(). */
    public abstract Block getContinueLabel ();

    /**
     * Return the LLVM label for the bottom of the loop.
     * The 'break' statement uses this. This method may only be expected to
     * work during genLLVM(). */
    public abstract Block getBottomLabel ();

}
