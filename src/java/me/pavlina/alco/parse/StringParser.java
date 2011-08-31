// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.parse;
import java.util.ArrayList;
import java.util.List;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;

/**
 * String parser. */
public class StringParser
{
    private StringParser () {}

    /**
     * Parse a string, returning a list of bytes. Note that an Alpha string may
     * only directly contain ASCII printables (0x20 through 0x7e), in addition
     * to any byte, using escapes. */
    public static List<Byte> parse (Env env, TokenStream stream) throws CError {
        // Read through the string one character at a time, decoding escapes if
        // necessary. The list is initialised to the length of the string
        // literal, since no string value will be longer than its literal.

        Token token = stream.next ();
        if (!token.is (Token.STRING)) {
            throw Unexpected.at ("string", token);
            // I would have kept with the de facto standard of using the
            // literal punctuation as the argument to Unexpected.at(), but I
            // don't want shoddy display environments to be confused by a
            // single quotation mark.
        }
        List<Byte> value = new ArrayList<Byte> (token.value.length () - 2);
        boolean insideEscape = false;
        for (int i = 1; i < token.value.length () - 1; ++i) {
            char c = token.value.charAt (i);
            if (insideEscape) {
                switch (c) {
                case 'a':
                    value.add ((byte)7); break;
                case 'b':
                    value.add ((byte)8); break;
                case 'f':
                    value.add ((byte)12); break;
                case 'n':
                    value.add ((byte)10); break;
                case 'r':
                    value.add ((byte)13); break;
                case 't':
                    value.add ((byte)9); break;
                case 'v':
                    value.add ((byte)11); break;
                case '\'':
                    value.add ((byte)'\''); break;
                case '"':
                    value.add ((byte)'"'); break;
                case '\\':
                    value.add ((byte)'\\'); break;
                case 'x':
                    // This is a hex escape. Make sure there are two more
                    // characters, then decode them
                    if (i > token.value.length () - 3)
                        throw Unexpected.at ("hex code after \\x", token);
                    String hexCode = token.value.substring (i + 1, i + 3);
                    int code;
                    try {
                        code = Integer.parseInt (hexCode, 16);
                    } catch (NumberFormatException e) {
                        throw Unexpected.at ("valid hex code after \\x",
                                token);
                    }
                    value.add ((byte) Integer.parseInt (hexCode, 16));
                    break;
                }
            } else {
                if (c == '\\') {
                    insideEscape = true;
                    continue;
                }
                if (c == '"') {
                    // This should never happen. If it does, there is a lexer
                    // bug, since the lexer should terminate a string at "
                    throw new RuntimeException ();
                }
                if (c < 0x20 || c > 0x7e) {
                    throw CError.at (String.format
                            ("invalid character code 0x%02x", c), token);
                }
                value.add ((byte) c);
            }
        }

        return value;
    }
}
