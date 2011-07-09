// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Unexpected token error.

package me.pavlina.alco.compiler.errors;

import me.pavlina.alco.compiler.ErrorAnnotator;
import me.pavlina.alco.lex.Token;

/**
 * Unexpected-item error. This is used when a different token was expected. For
 * example, if the file starts with "package 1234;", the 1234 will be unexpected
 * (a Token.WORD was expected, not Token.INT).
 */
public class Unexpected extends CError
{

    /**
     * Return an Unexpected initialised after (marking the position directly
     * following) a token.
     * @param expected Description of what was expected instead
     * @param token Token with which to associate
     * @return Unexpected object
     */
    public static Unexpected after (String expected, Token token)
    {
        // Create an Unexpected error after the token.
        int line = token.line;
        int col = token.col + token.value.length ();
        return new Unexpected (expected, line, col, 0, 0, token.annotator);
    }

    /**
     * Return an Unexpected initialised at (underlining) a token.
     * @param expected Description of what was expected instead
     * @param token Token with which to associate
     * @return Unexpected object
     */
    public static Unexpected at (String expected, Token token)
    {
        // Create an Unexpected error at the token.
        int line = token.line;
        int col = token.col;
        int start = token.col;
        int stop = token.col + token.value.length ();
        return new Unexpected (expected, line, col, start, stop,
                token.annotator);
    }

    /**
     * Initialise an Unexpected error.
     * @param expected Description of what was expected instead
     * @param line Line of code which triggered the error (zero-based)
     * @param col Column under which to show an error (zero-based)
     * @param start Column under which to start an underline
     * @param stop Column before which to stop the underline
     * @param annotator ErrorAnnotator object to format the code line
     */
    public Unexpected (String expected, int line, int col, int start, int stop,
            ErrorAnnotator annotator)
    {
        super ("expected " + expected, line, col, start, stop, annotator);
    }

}
