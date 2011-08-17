// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina;
import java.io.PrintStream;

/**
 * Indented output stream. All output through this is indented a given number
 * of spaces. */
public class IndentOutputStream extends java.io.OutputStream
{
    private PrintStream out;
    private boolean indent_next;
    private int indent;

    /**
     * Create an IndentOutputStream.
     * @param out PrintStream to direct all output to
     * @param indent Number of spaces to indent by */
    public IndentOutputStream (PrintStream out, int indent) {
        super ();
        this.out = out;
        this.indent = indent;
        indent_next = true;
    }

    /**
     * Write a byte to the PrintStream. Prepends with 'indent' spaces if it
     * is the first byte, or the first byte after a newline. */
    public void write (int b) throws java.io.IOException {
        if (b == '\n')
            indent_next = true;
        else if (indent_next) {
            for (int i = 0; i < indent; ++i)
                out.write (' ');
            indent_next = false;
        }
        out.write (b);
    }
}
