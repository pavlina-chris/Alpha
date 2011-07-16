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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Arithmetic assignment operators. These are assignments like +=, *= etc. */
public class OpAssignArith extends Expression.Operator {
    Token token;
    Expression[] children;
    String valueString;

    public static Expression.OperatorCreator CREATOR;

    public OpAssignArith (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        children = new Expression[1];
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
        // Our one child will be an OpAssign. From dest, value, we construct
        // something like (assign dest (op dest value)).
        
        Expression.Operator assign = new OpAssign (token);
        Expression.Operator oper;
        if (token.is (Token.OPER, "+="))
            oper = new OpPlus (token);
        else if (token.is (Token.OPER, "-="))
            oper = new OpMinus (token);
        else if (token.is (Token.OPER, "*="))
            oper = new OpMul (token);
        else if (token.is (Token.OPER, "/="))
            oper = new OpDiv (token);
        else
            throw new RuntimeException
                ("OpAssignArith for bad operator");
        oper.setOperands (dest, value);
        assign.setOperands (dest, oper);
        children[0] = assign;
    }

    public String getValueString () {
        return children[0].getValueString ();
    }

    public Type getType () {
        return children[0].getType ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        children[0].checkPointer (write, token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return children[0].getPointer (env, emitter, function);
    }

    public void print (java.io.PrintStream out) {
        children[0].print (out);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
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
                    return new OpAssignArith (env, stream);
                }
            };
    }
}
