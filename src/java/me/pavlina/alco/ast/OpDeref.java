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
 * Dereference operator. */
public class OpDeref extends Expression.Operator {
    private Token token;
    private Expression[] children;
    private String valueString;

    public static Expression.OperatorCreator CREATOR;

    public OpDeref (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        children = new Expression[1];
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
        return children[0].getType ().getSubtype ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (children[0].getType ().getEncoding () != Type.Encoding.POINTER) {
            throw CError.at ("cannot dereference non-pointer", token);
        }
    }

    public void checkPointer (boolean write, Token token) throws CError {
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        return children[0].getValueString ();
    }

    public void print (java.io.PrintStream out) {
        out.println ("Dereference");
        children[0].print (out, 2);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        String ptr = children[0].getValueString ();
        valueString = new load (emitter, function)
            .pointer (LLVMType.getLLVMName (getType ()), ptr)
            .build ();
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
                public Operator create (Env env, TokenStream stream)
                    throws CError {
                    return new OpDeref (env, stream);
                }
            };
    }
}
