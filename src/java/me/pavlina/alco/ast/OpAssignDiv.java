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
 * Multiplication assignment operator (*=). */
public class OpAssignDiv extends Expression.Operator {
    Token token;
    Expression[] children;
    Expression pointer, integer;
    String valueString;
    Cast cast;
    DivNum divnum;

    public static Expression.OperatorCreator CREATOR;

    public OpAssignDiv (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.ASSIGNMENT;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression dest, Expression value) {
        children[0] = dest;
        children[1] = value;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return children[0].getType ().getNormalised ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (true, token);
        children[1].checkTypes (env, resolver);

        Type.checkCoerce (children[1], children[0], token);
        cast = new Cast (token)
            .type (children[1].getType ())
            .dest (children[0].getType ());
        cast.checkTypes (env, resolver);
        divnum = new DivNum (token)
            .type (children[0].getType ());
        divnum.checkTypes (env, resolver);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String ptr = children[0].getPointer (env, emitter, function);
        
        cast.value (children[1].getValueString ());
        cast.genLLVM (env, emitter, function);
        divnum.lhs (children[0].getValueString ());
        divnum.rhs (cast.getValueString ());
        divnum.genLLVM (env, emitter, function);
        valueString = divnum.getValueString ();
        new store (emitter, function)
            .pointer (ptr)
            .value (LLVMType.getLLVMName (children[0].getType ()),
                    valueString)
            .build ();
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign-mult");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to assignment", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAssignDiv (env, stream, method);
                }
            };
    }
}
