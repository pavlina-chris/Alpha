// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.LLVMEmitter;
import me.pavlina.alco.llvm.Function;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.Arrays;

/**
 * Index operator. This does not parse, as it is always explicitly created from
 * a known name. */
public class OpIndex extends Expression.Operator {
    private Token token;
    private Expression[] children;

    public OpIndex (Token token, Expression expr) {
        token = token;
        children = new Expression[] { expr, null };
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.INDEX;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children[1] = op;
    }

    public String getValueString () {
        throw new RuntimeException ("indices not implemented yet");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw new RuntimeException ("indices not implemented yet");
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        throw new RuntimeException ("indices not implemented yet");
    }

    public Type getType () {
        throw new RuntimeException ("indices not implemented yet");
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        throw new RuntimeException ("indices not implemented yet");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        throw new RuntimeException ("indices not implemented yet");
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(index");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }
}
