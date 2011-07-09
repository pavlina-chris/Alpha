// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;
import me.pavlina.alco.llvm.FHead.*;

/**
 * Global. */
public class Global extends RootCodeObj
{
    String name;
    String type;
    String value;
    Linkage linkage;
    
    /**
     * Create a global.
     * @param name Global name. Should start with @
     * @param type Global type.
     * @param value Global value. Remember "zeroinitializer"
     * @param linkage Linkage type. */
    public Global (String name, String type, String value, Linkage linkage) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.linkage = linkage;
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_GLOBAL;
    }

    public void write (PrintStream out) {
        out.printf ("%s = %s global %s %s\n", name, linkage, type, value);
    }
}
