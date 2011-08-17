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
import me.pavlina.alco.parse.StatementParser;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

/**
 * AST scope. This holds variables and parses code. */
public class Scope extends AST
{
    Token token;
    List<AST> children;
    List<Boolean> gencode;

    /**
     * Initialise the scope without parsing */
    public Scope (Token token) {
        this.token = token;
        children = new ArrayList<AST> ();
        gencode = new ArrayList<Boolean> ();
    }

    /**
     * Parse and initialise the scope.
     */
    public Scope (Env env, TokenStream stream, Method method)
        throws CError
    {
        this.token = stream.peek ();
        children = new ArrayList<AST> ();
        gencode = new ArrayList<Boolean> ();
        parse (env, stream, method);
    }

    /**
     * Parse the scope */
    public void parse (Env env, TokenStream stream, Method method)
        throws CError
    {
        Token token = stream.peek ();
        if (token.is (Token.OPER, "{")) {
            // Block scope
            stream.next ();
            while (true) {
                token = stream.next ();
                if (token.is (Token.OPER, "}"))
                    break;
                else if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after ("}", stream.last ());
                stream.putback (token);

                if (token.is (Token.OPER, "{")) {
                    // Nested scope
                    Scope scope = new Scope (env, stream, method);
                    children.add (scope);
                    gencode.add (true);
                    scope.setParent (this);
                    continue;
                }

                // Try a statement
                Statement statement = StatementParser.parse
                    (env, stream, method);
                if (statement != null) {
                    children.add (statement);
                    gencode.add (true);
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
                    gencode.add (true);
                    expression.setParent (this);
                    continue;
                }

                // Nothing good
                throw Unexpected.at ("statement or expression", token);
            }
        } else {
            // Try a statement
            Statement statement = StatementParser.parse (env, stream, method);
            if (statement != null) {
                children.add (statement);
                gencode.add (true);
                statement.setParent (this);
                return;
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
                gencode.add (true);
                expression.setParent (this);
                return;
            }

            // Nothing good
            throw Unexpected.at ("statement or expression", token);
        }
    }

    /**
     * Add an item to the scope.
     * @param item Item to add
     * @param gencode Whether code should be generated for this item */
    public void add (AST item, boolean gencode) {
        children.add (item);
        this.gencode.add (gencode);
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
        for (int i = 0; i < children.size (); ++i) {
            if (gencode.get (i).booleanValue ())
                children.get (i).genLLVM (env, emitter, function);
        }
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
