// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// This class reads a file and returns tokens.

package me.pavlina.alco.lex;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.pavlina.alco.compiler.ErrorAnnotator;
import me.pavlina.alco.compiler.errors.*;

/**
 * Stream of tokens.
 */
public class TokenStream
{
    Lexer lexer;
    int pos;

    /**
     * Initialise the token stream. */
    public TokenStream (Lexer lexer) {
        this.lexer = lexer;
        pos = 0;
    }

    /**
     * Return the Lexer */
    public Lexer getLexer () {
        return lexer;
    }

    /**
     * Get the next token */
    public Token next () {
        if (pos >= lexer.length ()) {
            return new Token (Token.NO_MORE, "", 0, 0, lexer);
        }
        return lexer.get (pos++);
    }

    /**
     * Get the last token.
     * Example:
     *   next() -&gt; A
     *   next() -&gt; B
     *   last() -&gt; A
     */
    public Token last () {
        if (pos < 2) return null;
        return lexer.get (pos - 2);
    }

    /**
     * Look at the next token without consuming */
    public Token peek () {
        if (pos >= lexer.length ()) {
            return new Token (Token.NO_MORE, "", 0, 0, lexer);
        }
        return lexer.get (pos);
    }

    /**
     * Put back the token. Can rewrite, but be careful. Put back in reverse
     * order.
     */
    public void putback (Token token) {
        lexer.set (--pos, token);
    }
}
