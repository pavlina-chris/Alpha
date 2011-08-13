// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import java.util.Map;
import java.util.HashMap;

/**
 * Statement base class and parser. When adding statements, look at the static
 * initialiser for most; they should be simple to add. */
public abstract class Statement extends AST
{

    /**
     * Statement parser. This is very simple, as it just has to recognise the
     * keyword and call the individual statement's parser. Note that it can
     * return null, in which case you should try the expression parser
     * instead. */
    public static Statement parse (Env env, TokenStream stream,
                                   Method method) throws CError
    {
        Token token = stream.peek ();
        if (!token.is (Token.WORD)) return null;

        StatementCreator sc = STATEMENTS.get (token.value);
        if (sc == null) return null;
        return sc.create (env, stream, method);
    }

    /**
     * Statement creator interface */
    protected static interface StatementCreator {
        /**
         * Create and return a statement, given Env and TokenStream */
        public Statement create (Env env, TokenStream stream,
                                 Method method) throws CError;
    }

    /**
     * Map of all keywords to their statement creators. */
    private static Map<String, StatementCreator> STATEMENTS;

    /**
     * Statement-loading static intiailiser. This loads all statements into the
     * map. */
    static {
        STATEMENTS = new HashMap<String, StatementCreator> ();
        
        STATEMENTS.put ("let", StLet.CREATOR);
        STATEMENTS.put ("const", StConst.CREATOR);
        STATEMENTS.put ("static", StStatic.CREATOR);
        STATEMENTS.put ("return", StReturn.CREATOR);
        STATEMENTS.put ("if", StIf.CREATOR);
        STATEMENTS.put ("while", StWhile.CREATOR);
    }

}
