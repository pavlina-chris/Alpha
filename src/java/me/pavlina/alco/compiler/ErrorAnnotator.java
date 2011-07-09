// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// This represents any class that can print out an annotated line of code.

package me.pavlina.alco.compiler;

import java.io.PrintStream;

/**
 * Anything that can print out an error message with a line annotated. This is
 * really just a cheap hack to avoid a circular import between Lex.Token and
 * Lex.TokenStream...
 */
public interface ErrorAnnotator
{

    /**
     * Print out a line of code, annotated with an error location. An arrow of
     * some sort should be placed at 'col', and an underline should run from
     * 'start' inclusive to 'stop' exclusive.
     * 
     * If 'start' and 'stop' are equal, no underline should be produced; if
     * 'col' is outside the limits of the line, no arrow should be produced.
     * 
     * The current implementation, like clang, uses a caret (^) for the arrow
     * and tildes (~~~~) for the underline.
     * 
     * @param line Line to print out (zero-based)
     * @param col Column to place the arrow under (zero-based)
     * @param start Column to start underline at
     * @param stop Column to stop underline at
     * @param out PrintStream to print to
     */
    public void annotate (int line, int col, int start, int stop,
                          PrintStream out);

    /**
     * Return the file name that the code came from. This should be the short
     * name, not including the directory. It is used for formatting error
     * message, which include this.
     * @return Short file name
     */
    public String filename ();

}
