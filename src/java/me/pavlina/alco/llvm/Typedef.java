// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;

/**
 * Typedef. There is no object-oriented representation of a type itself, so you
 * must specify this as a string. */
public class Typedef extends RootCodeObj
{
    String name;
    String text;

    /**
     * Make a typedef.
     * @param name Typedef name. Should start with %
     * @param value Typedef value. Should be valid LLVM. Omit the "type"
     *  prefix. */
    public Typedef (String name, String value) {
        this.name = name;
        this.text = value;
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_TYPE;
    }

    public void write (PrintStream out) {
        out.printf ("%s = type %s\n", name, text);
    }

    /**
     * Get the globally accessible name of the type */
    public String getName () {
        return name;
    }

}
