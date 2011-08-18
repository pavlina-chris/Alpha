// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Continue statement. Syntax:
 * continue ;
 * continue Number ;
 */
public class StContinue extends Statement
{
    Token token, tNum;
    int n;
    Loop loop;

    public StContinue (Env env, TokenStream stream, Method method) throws CError
    {
        token = stream.next ();
        assert token.is (Token.WORD, "continue");

        Token temp = stream.next ();
        if (temp.is (Token.OPER, ";")) {
            n = 1;
            return;
        } else if (temp.is (Token.INT)) {
            try {
                n = Integer.parseInt (temp.value);
            } catch (NumberFormatException e) {
                throw Unexpected.at ("valid loop count", temp);
            }
            tNum = temp;
            temp = stream.next ();
            if (!temp.is (Token.OPER, ";"))
                throw Unexpected.after (";", tNum);
        } else {
            throw Unexpected.after ("; or number", token);
        }
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        assert n >= 0; // There are no negative integer literals
        if (n == 0) {
            throw Unexpected.at ("positive number", tNum);
        }
        // Trace up N loops
        int m = 0;
        AST temp = this.getParent ();
        while (temp != null) {
            if (Loop.class.isInstance (temp)) {
                m += 1;
                if (m == n) {
                    loop = (Loop) temp;
                    break;
                }
            }
            temp = temp.getParent ();
        }
        if (loop == null) {
            throw CError.at ("cannot find " + n + " level" + (n == 1 ? "" : "s")
                             + " to continue from", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        function.add (new BRANCH ().dest (loop.getContinueLabel ()));
    }

    public void print (java.io.PrintStream out) {
        out.print ("(continue)");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method) throws CError
                {
                    return new StContinue (env, stream, method);
                }
            };
    }
}
