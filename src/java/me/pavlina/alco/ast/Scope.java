// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.LLVMEmitter;
import me.pavlina.alco.llvm.Function;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

/**
 * AST scope. This holds variables and parses code. */
public class Scope extends AST
{
    private Token token;
    private List<AST> children;
    private Resolver resolver;

    /**
     * Parse and initialise the scope. Because resolution is done with the help
     * of a special Resolver, the scope does not need to know its parent. */
    public Scope (Env env, TokenStream stream, Method method)
        throws CError
    {
        this.token = stream.next ();
        if (!this.token.is (Token.OPER, "{")) {
            throw new RuntimeException ("Scope instantiated without {");
        }
        children = new ArrayList<AST> ();
        while (true) {
            Token token = stream.next ();
            if (token.is (Token.OPER, "}"))
                break;
            else if (token.is (Token.NO_MORE))
                throw UnexpectedEOF.after ("}", stream.last ());
            stream.putback (token);
            
            // Try a statement
            Statement statement = Statement.parse (env, stream, method);
            if (statement != null) {
                children.add (statement);
                continue;
            }

            // Try an expression
            Expression expression = Expression.parse (env, stream, method, ";");
            if (expression != null) {
                token = stream.next ();
                if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after (";", stream.last ());
                else if (!token.is (Token.OPER, ";"))
                    throw Unexpected.after (";", stream.last ());
                children.add (expression);
                continue;
            }

            // Nothing good
            throw Unexpected.at ("statement or expression", token);
        }
    }

    public Token getToken ()
    {
        return token;
    }

    public List<AST> getChildren ()
    {
        return children;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError
    {
        // Because this is a scope, we need to create a new Resolver to descend.
        Resolver newResolver = new Resolver (resolver);
        for (AST i: children)
            i.checkTypes (env, newResolver);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        for (AST i: children)
            i.genLLVM (env, emitter, function);
    }

    public void print (PrintStream out)
    {
        out.print ("(");
        boolean first = true;
        for (AST i: children) {
            if (first) first = false;
            else out.print (" ");
            i.print (out, 2);
            out.println ();
        }
        out.print ("   )");
    }
}
