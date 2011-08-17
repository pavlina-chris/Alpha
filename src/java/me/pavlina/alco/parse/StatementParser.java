// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.parse;
import me.pavlina.alco.ast.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import java.util.Map;
import java.util.HashMap;

// This class is ridiculously simple because all the statements have their own
// parsers.
public class StatementParser {

    private StatementParser () {}

    /**
     * Statement parser. This is very simple, as it just has to recognise the
     * keyword and call the individual statement's parser. Note that it can
     * return null, in which case you should try the expression parser
     * instead. */
    public static Statement parse (Env env, TokenStream stream, Method method)
        throws CError
    {
        Token token = stream.peek ();
        if (!token.is (Token.WORD)) return null;

        Statement.StatementCreator sc = STATEMENTS.get (token.value);
        if (sc == null) return null;
        return sc.create (env, stream, method);
    }

    /**
     * Map of all keywords to their statement creators. */
    private static Map<String, Statement.StatementCreator> STATEMENTS;

    /**
     * Statement-loading static intiailiser. This loads all statements into the
     * map. */
    static {
        STATEMENTS = new HashMap<String, Statement.StatementCreator> ();
        
        STATEMENTS.put ("let", StLet.CREATOR);
        STATEMENTS.put ("const", StConst.CREATOR);
        STATEMENTS.put ("static", StStatic.CREATOR);
        STATEMENTS.put ("return", StReturn.CREATOR);
        STATEMENTS.put ("if", StIf.CREATOR);
        STATEMENTS.put ("while", StWhile.CREATOR);
        STATEMENTS.put ("do", StDoWhile.CREATOR);
        STATEMENTS.put ("for", StFor.CREATOR);
        STATEMENTS.put ("break", StBreak.CREATOR);
    }
}
