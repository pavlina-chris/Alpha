// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;
import me.pavlina.alco.llvm.FHead.*;

/**
 * Global constant. */
public class Constant extends RootCodeObj
{
    String name;
    String type;
    String value;
    Linkage linkage;

    /**
     * Create a constant.
     * @param name Constant name. Should start with @.
     * @param type Constant type.
     * @param value Constant value.
     * @param linkage Linkage type. */
    public Constant (String name, String type, String value, Linkage linkage) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.linkage = linkage;
    }

    /**
     * Create an unnamed constant.
     * @param counter Counter to get number from
     * @param type Constant type.
     * @param value Constant value.
     * @param linkage Linkage type. */
    public Constant (Counter counter, String type, String value,
                     Linkage linkage)
    {
        this.name = "@" + Integer.toString (counter.getTemporary ("@"));
        this.type = type;
        this.value = value;
        this.linkage = linkage;
    }

    public String getName () {
        return name;
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_GLOBAL;
    }

    public void write (PrintStream out) {
        out.printf ("%s = %s constant %s %s\n", name, linkage, type, value);
    }
}
