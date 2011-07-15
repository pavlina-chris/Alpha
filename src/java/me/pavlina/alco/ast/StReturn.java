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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Return (duh). Syntax:
 * return ;
 * return {expression} ;
 */
public class StReturn extends Statement
{
    private Token token;
    private Expression value;
    private Method method;

    public StReturn (Env env, TokenStream stream, Method method) throws CError {
        this.method = method;
        token = stream.next ();
        if (!token.is (Token.WORD, "return"))
            throw new RuntimeException ("StReturn instantiated without kwd");

        value = Expression.parse (env, stream, ";"); // Allow null

        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";"))
            throw Unexpected.after (";", stream.last ());
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (value);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        value.checkTypes (env, resolver);
        value = (Expression) Type.coerce (value, method.getType (),
                                          OpCast.CASTCREATOR, env);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (value == null)
            new ret (emitter, function).build ();
        else {
            value.genLLVM (env, emitter, function);
            String valueString = value.getValueString ();
            new ret (emitter, function)
                .value (LLVMType.getLLVMName (value.getType ()), valueString)
                .build ();
        }
    }

    public void print (java.io.PrintStream out) {
        if (value == null)
            out.println ("Return void");
        else {
            out.println ("Return");
            value.print (out, 2);

        }
    }

    public static Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method)
                    throws CError
                {
                    return new StReturn (env, stream, method);
                }
            };
    }
}
