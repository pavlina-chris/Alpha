// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.PrintStream;

/**
 * This class is an emitter for LLVM pseudoassembly code. To use this, you
 * should create LLVM code items (see the sibling classes), then add them to
 * this. When you are done, you can emit code with emit().
 *
 * One emitter represents one output file. */
public class LLVMEmitter extends Counter {

    List<RootCodeObj> codeObjects;

    public LLVMEmitter () {
        super ();
        codeObjects = new ArrayList<RootCodeObj> ();
    }

    public void add (RootCodeObj o) {
        codeObjects.add (o);
    }

    public void emit (PrintStream out) {
        Collections.sort (codeObjects);
        for (RootCodeObj i: codeObjects) {
            i.write (out);
        }
    }

}
