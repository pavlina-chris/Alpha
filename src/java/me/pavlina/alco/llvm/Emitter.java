// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is an emitter for LLVM pseudoassembly code. To use this, you
 * should create LLVM code items (see the sibling classes), then add them to
 * this. When you are done, you can generate code with toString().
 *
 * One emitter represents one output file. */
public class Emitter {

    List<RootObject> code;

    public Emitter () {
        code = new ArrayList<RootObject> ();
    }

    public void add (RootObject o) {
        code.add (o);
    }

    public String toString () {
        Collections.sort (code);
        // Number everything
        int n = 0;
        for (RootObject i: code) {
            if (i.needsId ()) {
                i.setId ("@" + Integer.toString (n));
                ++n;
            }
        }
        // Output code
        StringBuilder sb = new StringBuilder ();
        for (RootObject i: code) {
            sb.append (i);
        }
        return sb.toString ();
    }

}
