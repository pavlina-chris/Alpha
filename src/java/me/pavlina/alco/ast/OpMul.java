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
 * Multiplication operator.
 */
public class OpMul extends Expression.Operator {
    Token token;
    Method method;
    Expression[] children;
    Type type;
    String valueString;
    Overload overload;
    Coerce coerce;
    MulNum mulnum;

    public static Expression.OperatorCreator CREATOR;


    public OpMul (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.MUL;
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
            mulnum = null;
            overload = new Overload (token, method);
            overload.operator ("*").children (children);
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
        mulnum = new MulNum (token)
            .type (type);
        mulnum.checkTypes (env, resolver);
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

        if (mulnum != null) {
            coerce.lhsV (children[0].getValueString ());
            coerce.rhsV (children[1].getValueString ());
            coerce.genLLVM (env, emitter, function);
            mulnum.lhs (coerce.getValueStringL ());
            mulnum.rhs (coerce.getValueStringR ());
            mulnum.genLLVM (env, emitter, function);
            valueString = mulnum.getValueString ();

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
        out.print ("(mult");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to multiplication", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpMul (env, stream, method);
                }
            };
    }
}
