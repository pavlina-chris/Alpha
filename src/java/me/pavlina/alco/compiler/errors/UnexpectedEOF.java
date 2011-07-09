// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Unexpected EOF error.

package me.pavlina.alco.compiler.errors;

import me.pavlina.alco.compiler.ErrorAnnotator;
import me.pavlina.alco.lex.Token;

/**
 * Unexpected-end-of-file error. This is used when a block or sequence was being
 * parsed, and the end of the file came before the end marker of the block or
 * sequence.
 */
public class UnexpectedEOF extends CError
{

    /**
     * Return an UnexpectedEOF initialised after (marking the position directly
     * following) a token.
     * @param expected Description of what was expected, but never found
     * @param token Token with which to associate
     * @return UnexpectedEOF object
     */
    public static UnexpectedEOF after (String expected, Token token)
    {
        // Create an UnexpectedEOF error after the token.
        int line = token.line;
        int col = token.col + token.value.length ();
        return new UnexpectedEOF (expected, line, col, 0, 0, token.annotator);
    }

    /**
     * Return an UnexpectedEOF initialised at (underlining) a token.
     * @param expected Description of what was expected, but never found
     * @param token Token with which to associate
     * @return UnexpectedEOF object
     */
    public static UnexpectedEOF at (String expected, Token token)
    {
        // Create an UnexpectedEOF error at the token.
        int line = token.line;
        int col = token.col;
        int start = token.col;
        int stop = token.col + token.value.length ();
        return new UnexpectedEOF (expected, line, col, start, stop,
                token.annotator);
    }

    /**
     * Initialise an UnexpectedEOF error.
     * @param expected Description of what was expected, but never found
     * @param line Line of code which triggered the error (zero-based)
     * @param col Column under which to show an error (zero-based)
     * @param start Column under which to start an underline
     * @param stop Column before which to stop the underline
     * @param annotator ErrorAnnotator object to format the code line
     */
    public UnexpectedEOF (String expected, int line, int col, int start,
            int stop, ErrorAnnotator annotator)
    {
        super ("unexpected EOF; wanted " + expected, line, col, start, stop,
               annotator);
    }
}
