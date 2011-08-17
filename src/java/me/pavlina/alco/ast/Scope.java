// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.Emitter;
import me.pavlina.alco.llvm.Function;
import me.pavlina.alco.parse.ExpressionParser;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

/**
 * AST scope. This holds variables and parses code. */
public class Scope extends AST
{
    Token token;
    List<AST> children;

    /**
     * Parse and initialise the scope. Because resolution is done with the help
     * of a special Resolver, the scope does not need to know its parent. */
    public Scope (Env env, TokenStream stream, Method method)
        throws CError
    {
        this.token = stream.peek ();
        children = new ArrayList<AST> ();

        if (this.token.is (Token.OPER, "{")) {
            // Block scope
            stream.next ();
            while (true) {
                Token token = stream.next ();
                if (token.is (Token.OPER, "}"))
                    break;
                else if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after ("}", stream.last ());
                stream.putback (token);

                if (token.is (Token.OPER, "{")) {
                    // Nested scope
                    Scope scope = new Scope (env, stream, method);
                    children.add (scope);
                    scope.setParent (this);
                    continue;
                }

                // Try a statement
                Statement statement = Statement.parse (env, stream, method);
                if (statement != null) {
                    children.add (statement);
                    statement.setParent (this);
                    continue;
                }

                // Try an expression
                Expression expression = ExpressionParser.parse
                    (env, stream, method, ";");
                if (expression != null) {
                    token = stream.next ();
                    if (token.is (Token.NO_MORE))
                        throw UnexpectedEOF.after (";", stream.last ());
                    else if (!token.is (Token.OPER, ";"))
                        throw Unexpected.after (";", stream.last ());
                    children.add (expression);
                    expression.setParent (this);
                    continue;
                }

                // Nothing good
                throw Unexpected.at ("statement or expression", token);
            }
        } else {
            // Try a statement
            Statement statement = Statement.parse (env, stream, method);
            if (statement != null) {
                children.add (statement);
                statement.setParent (this);
                return;
            }

            // Try an expression
            Expression expression = ExpressionParser.parse
                (env, stream, method, ";");
            if (expression != null) {
                Token token = stream.next ();
                if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after (";", stream.last ());
                else if (!token.is (Token.OPER, ";"))
                    throw Unexpected.after (";", stream.last ());
                children.add (expression);
                expression.setParent (this);
                return;
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

    public void genLLVM (Env env, Emitter emitter, Function function) {
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
