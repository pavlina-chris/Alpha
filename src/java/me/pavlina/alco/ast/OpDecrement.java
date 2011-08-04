// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Decrement operator (--) */
public class OpDecrement extends Expression.Operator {
    Token token;
    Method method;
    Type type;
    Expression[] children;
    String valueString;
    Overload overload;
    boolean ptrSub;

    public static Expression.OperatorCreator CREATOR;

    public OpDecrement (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.INCR;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children = new Expression[] {op};
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (true, token);

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            overload = new Overload (token, method);
            OpAddress addrOf = new OpAddress (token, children[0]);
            addrOf.checkTypes (env, resolver);
            overload.operator ("--").children (addrOf);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        Type.Encoding lhsE = children[0].getType ().getEncoding ();

        // Try --pointer first
        if (lhsE == Type.Encoding.POINTER) {
            ptrSub = true;
        } else {
            Type.checkCoerce (new Type (env, "int", null), children[0], token);
            ptrSub = false;
        }
        type = children[0].getType ().getNormalised ();
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        String ptr = children[0].getPointer (env, emitter, function);

        if ((overload == null) && ptrSub) {
            valueString = new getelementptr (emitter, function)
                .type (LLVMType.getLLVMName (children[0].getType ()))
                .pointer (children[0].getValueString ())
                .addIndex (-1)
                .build ();
            new store (emitter, function)
                .pointer (ptr)
                .value (LLVMType.getLLVMName (children[0].getType ()),
                        valueString)
                ._volatile (children[0].getType ().isVolatile ())
                .build ();
        } else if ((overload == null) && !ptrSub) {
            valueString = new Binary (emitter, function)
                .operation (Binary.BinOp.SUB)
                .type (LLVMType.getLLVMName (type))
                .operands (children[0].getValueString (), "1")
                .build ();
            new store (emitter, function)
                .pointer (ptr)
                .value (LLVMType.getLLVMName (children[0].getType ()),
                        valueString)
                ._volatile (children[0].getType ().isVolatile ())
                .build ();
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

    public void print (java.io.PrintStream out) {
        out.print ("(decrement");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to increment", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpDecrement (env, stream, method);
                }
            };
    }
}
