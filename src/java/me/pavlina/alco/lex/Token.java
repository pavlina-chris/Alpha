// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Token class

package me.pavlina.alco.lex;

import me.pavlina.alco.compiler.ErrorAnnotator;

/**
 * Source code token.
 */
public class Token
{

    /**
     * @name Token types
     * @{
     * These are type flags for identifying which regular expression matched
     * the token.
     */
    
    public static final int STRING = 1;  /**< String. */
    public static final int WORD = 2;    /**< Word (keyword, name, etc). */
    public static final int INT = 3;     /**< Integer. */
    public static final int REAL = 4;    /**< Real number (float). */
    public static final int OPER = 5;    /**< Operator. */
    public static final int EXTRA = 6;   /**< Extrastandard identifier. */
    public static final int NO_MORE = 0; /**< No token (EOF). */
    /** @} */

    /**
     * @name Editable attributes
     * @{
     * Fully editable for rewriting purposes
     */
    public int line;      /**< Zero-based line number */
    public int col;       /**< Zero-based column number */
    public int type;      /**< Token's type flag */
    public String value;  /**< Token's text */
    /** Error message annotator (TokenStream) */
    public ErrorAnnotator annotator;
    /** @} */

    /**
     * Create a new token.
     * @param type Token type (see Token types)
     * @param value Token text
     * @param line Line number (zero-based)
     * @param col Column number (zero-based)
     * @param annotator Error message annotator (TokenStream)
     */
    public Token (int type, String value, int line, int col,
                  ErrorAnnotator annotator)
    {
        this.type = type;
        this.value = value;
        this.line = line;
        this.col = col;
        this.annotator = annotator;
    }

    /**
     * Return whether this token equals another
     * @param other Other object to check
     * @return false if 'other' is not a Token; true if 'other' is a Token and
     * has the same type and value.
     */
    @Override
        public boolean equals (Object other)
    {
        if (!(other instanceof Token)) return false;
        Token t = (Token) other;
        return (t.type == type) && t.value.equals (value);
    }

    /**
     * Return a hash code for this token.
     * @return
     * @code 961 + (31 * type) + value.hashCode ();
     * @endcode
     */
    @Override
        public int hashCode ()
    {
        return 961 + (31 * type) + value.hashCode ();
    }

    /**
     * Return whether this token is of the given type
     * @param type Token type (see Token types)
     * @return Whether this token matches
     */
    public boolean is (int type)
    {
        return type == this.type;
    }

    /**
     * Return whether this token is of the given type and value
     * @param type Token type (see Token types)
     * @param value Token text
     * @return Whether this token matches
     */
    public boolean is (int type, String value)
    {
        return (type == this.type) && value.equals (this.value);
    }

    /**
     * Return whether this token is of the given value
     * @param value Token text
     * @return Whether this token matches
     */
    public boolean is (String value)
    {
        return value.equals (this.value);
    }

    /**
     * Return a descriptive string for this token.
     * @return String which may be printed for debug purposes.
     */
    @Override
        public String toString ()
    {
        String t;
        switch (type) {
        case STRING:  t = "STRING";  break;
        case WORD:    t = "WORD";    break;
        case INT:     t = "INT";     break;
        case REAL:    t = "REAL";    break;
        case OPER:    t = "OPER";    break;
        case EXTRA:   t = "EXTRA";   break;
        case NO_MORE: t = "NO_MORE"; break;
        default:
            throw new RuntimeException ("invalid token type");
        }
        return String.format ("%-7s %3dx%3d %s", t, line + 1, col + 1, value);
    }

}
