// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.Arrays;

/**
 * Address-of operator. */
public class OpAddress extends Expression.Operator {
    Token token;
    Expression[] children;
    String valueString;
    Type type;

    public static Expression.OperatorCreator CREATOR;

    public OpAddress (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
        children = new Expression[1];
    }

    public OpAddress (Token token, Expression child) {
        this.token = token;
        children = new Expression[] {child};
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.DEREF;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children[0] = op;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (false, token);
        type = children[0].getType ().getPointer (env);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to address-of", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(address ");
        children[0].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        valueString = children[0].getPointer (env, emitter, function);
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAddress (env, stream, method);
                }
            };
    }
}