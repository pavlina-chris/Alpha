// Copyright (c) 2011, Chris Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Type;
import java.util.List;
import java.util.ArrayList;

/**
 * Type parser. */
public class TypeParser {

    /**
     * Parse a type name and return a me.pavlina.alco.language.Type. */
    public static Type parse (TokenStream stream, Env env) throws CError {

        Token token, argsToken;
        String name;
        List<Type> args = null;
        List<Type.Modifier> mods = null;

        // Base name
        token = stream.next ();
        if (token.is (Token.NO_MORE)) {
            throw UnexpectedEOF.after ("type name", stream.last ());
        } else if (!token.is (Token.WORD)) {
            throw Unexpected.at ("type name", token);
        }
        name = token.value;

        // Arguments
        argsToken = stream.next ();
        if (argsToken.is (Token.OPER, "<")) {
            // There are arguments
            args = new ArrayList<Type> ();
            while (true) {
                Type arg = TypeParser.parse (stream, env);
                token = stream.next ();
                if (token.is (Token.OPER, ">")) {
                    stream.next ();
                    break;
                } else if (token.is (Token.OPER, ">>")) {
                    // Rewrite >> to >
                    stream.next ();
                    token.value = ">";
                    ++token.col;
                    stream.putback (token);
                    break;
                } else if (token.is (Token.NO_MORE)) {
                    throw UnexpectedEOF.after (", or >", stream.last ());
                } else if (!token.is (Token.OPER, ",")) {
                    throw Unexpected.after (",", stream.last ());
                }
            }
        } else {
            stream.putback (argsToken);
        }

        // Modifiers
        mods = new ArrayList<Type.Modifier> ();
        while (true) {
            token = stream.next ();
            if (token.is (Token.OPER, "*")) {
                mods.add (Type.Modifier.POINTER);
            } else if (token.is (Token.OPER, "[")) {
                token = stream.next ();
                if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after ("]", stream.last ());
                else if (!token.is (Token.OPER, "]"))
                    throw Unexpected.after ("]", stream.last ());
                mods.add (Type.Modifier.ARRAY);
            } else if (token.is (Token.WORD, "const")) {
                mods.add (Type.Modifier.CONST);
            } else {
                stream.putback (token);
                break;
            }
        }
        Type.Modifier[] modarray = new Type.Modifier[mods.size ()];
        modarray = mods.toArray (modarray);
        
        Type type = new Type (env, name, args, modarray);

        if (type.getEncoding () != Type.Encoding.OBJECT && args != null) {
            throw CError.at ("non-object cannot have type arguments", token);
        }

        return type;
    }

}
