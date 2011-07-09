// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import me.pavlina.IndentOutputStream;
import java.io.PrintStream;

/**
 * This base class represents any LLVM code object. */
public abstract class CodeObj {

    public abstract void write (PrintStream out);

    public void write (PrintStream out, int indent) {
        this.write (new PrintStream (new IndentOutputStream (out, indent)));
    }

}
