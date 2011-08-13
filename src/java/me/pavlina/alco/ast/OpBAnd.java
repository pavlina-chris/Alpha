// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.Arrays;

/**
 * Bitwise AND operator.
 */
public class OpBAnd extends Expression.Operator {
    Token token;
    Method method;
    Expression[] children;
    Type type;
    String valueString;
    Overload overload;
    Coerce coerce;
    BitAnd bitand;

    public static final Expression.OperatorCreator CREATOR;


    public OpBAnd (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.BIT_AND;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children[0] = left;
        children[1] = right;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[1].checkTypes (env, resolver);

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            bitand = null;
            overload = new Overload (token, method);
            overload.operator ("&").children (children);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        coerce = new Coerce (token)
            .lhsT (children[0].getType ())
            .rhsT (children[1].getType ());
        coerce.checkTypes (env, resolver);
        type = coerce.getType ();
        bitand = new BitAnd (token)
            .type (type);
        bitand.checkTypes (env, resolver);
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function)
    {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);

        if (bitand != null) {
            coerce.lhsV (children[0].getValueString ());
            coerce.rhsV (children[1].getValueString ());
            coerce.genLLVM (env, emitter, function);
            bitand.lhs (coerce.getValueStringL ());
            bitand.rhs (coerce.getValueStringR ());
            bitand.genLLVM (env, emitter, function);
            valueString = bitand.getValueString ();

        } else {
            overload.genLLVM (env, emitter, function);
            valueString = overload.getValueString ();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        if (children == null)
            return null;
        else
            return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(bitw-and");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to bitwise operation", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpBAnd (env, stream, method);
                }
            };
    }
}
