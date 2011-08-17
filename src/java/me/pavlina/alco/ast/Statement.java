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
     * Statement creator interface */
    public static interface StatementCreator {
        /**
         * Create and return a statement, given Env and TokenStream */
        public Statement create (Env env, TokenStream stream,
                                 Method method) throws CError;
    }

}
