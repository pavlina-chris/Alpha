// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.compiler;

import java.io.PrintStream;
import me.pavlina.alco.lex.Token;

/**
 * Compilation environment. This holds information about compile options, and
 * provides output features.
 */
public class Env
{
    @SuppressWarnings("unused")
    PrintStream out, err;
    int         bits;
    boolean     debug;
    String      malloc_fn, free_fn;

    /**
     * Initialise the compilation environment.
     * @param out Standard output stream (usually System.out)
     * @param err Standard error stream (usually System.err)
     * @param bits Processor word length (32 or 64)
     * @param debug Whether debug information is desired
     */
    public Env (PrintStream out, PrintStream err, int bits, boolean debug)
    {
        this.out = out;
        this.err = err;
        this.bits = bits;
        this.debug = debug;
        // TODO: Custom malloc
        malloc_fn = "GC_malloc";
        free_fn = "GC_free";
    }

    /**
     * Return the architecture
     * @return Processor word length (32 or 64)
     */
    public int getBits ()
    {
        return bits;
    }

    /**
     * Return the malloc function */
    public String getMalloc () {
        return malloc_fn;
    }

    /**
     * Return the free function. */
    public String getFree () {
        return free_fn;
    }

    /**
     * Return whether debug information is desired
     */
    // I refuse to follow with @return Whether debug information is desired :-)
    public boolean isDebug ()
    {
        return debug;
    }

    /**
     * Print a warning message with no associated file position
     * @param message Warning message string
     */
    public void warning (String message)
    {
        err.print ("warning: ");
        err.println (message);
    }

    /**
     * Print a warning message associated with the position just after a token.
     * @param message Warning message string
     * @param tok Token to associate with
     */
    public void warning_after (String message, Token tok)
    {
        err.printf ("%s:%d:%d: warning: ", tok.annotator.filename (),
                    tok.line + 1, tok.col + 1);
        err.println (message);
        tok.annotator.annotate (tok.line, tok.col + tok.value.length (), 0, 0,
                                err);
    }

    /**
     * Print a warning message associated with a token. The token will be
     * underlined in the printout.
     * @param message Warning message string
     * @param tok Token to associate with
     */
    public void warning_at (String message, Token tok)
    {
        err.printf ("%s:%d:%d: warning: ", tok.annotator.filename (),
                    tok.line + 1, tok.col + 1);
        err.println (message);
        tok.annotator.annotate (tok.line, tok.col, tok.col,
                                tok.col + tok.value.length (), err);
    }
}
