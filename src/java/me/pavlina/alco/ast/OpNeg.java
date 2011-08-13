// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.Arrays;

/**
 * Negation operator. */
public class OpNeg extends Expression.Operator {
    Token token;
    Method method;
    Type type;
    Expression[] children;
    String valueString;
    NegNum negnum;
    Overload overload;

    public static final Expression.OperatorCreator CREATOR;

    public OpNeg (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[1];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.NEG;
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

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            negnum = null;
            overload = new Overload (token, method);
            overload.operator ("-").children (children[0]);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        negnum = new NegNum (token).type (children[0].getType ());
        negnum.checkTypes (env, resolver);
        type = children[0].getType ().getNormalised ();
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to negation", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(negate ");
        children[0].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);

        if (negnum != null) {
            negnum.operand (children[0].getValueString ());
            negnum.genLLVM (env, emitter, function);
            valueString = negnum.getValueString ();

        } else {
            overload.genLLVM (env, emitter, function);
            valueString = overload.getValueString ();
        }
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
                    return new OpNeg (env, stream, method);
                }
            };
    }
}
