// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.llvm.LLVMEmitter;
import me.pavlina.alco.llvm.Function;
import java.util.List;
import java.util.Arrays;

/**
 * Colon operator. This groups expressions for the ?: ternary operator.
 * It should never be left at the genLLVM stage.
 */
public class OpColon extends Expression.Operator {
    private Token token;
    private Expression[] children;

    public static Expression.OperatorCreator CREATOR;


    public OpColon (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.COLON;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        if (children == null) return;
        children[0] = left;
        children[1] = right;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        throw Unexpected.at ("?", token);
    }

    public String getValueString () {
        throw new RuntimeException ("OpColon still exists at codegen");
    }

    public Type getType () {
        throw new RuntimeException ("OpColon still exists at codegen");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        throw new RuntimeException ("OpColon still exists at codegen");
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
        if (children == null) {
            out.print ("'()");
        } else {
            out.print ("'(");
            this.printNoParens (out);
            out.print (")");
        }
    }
    
    private void printNoParens (java.io.PrintStream out) {
        if (OpColon.class.isInstance (children[0])) {
            ((OpColon) children[0]).printNoParens (out);
        } else {
            children[0].print (out);
        }
        out.print (" ");
        children[1].print (out);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw new RuntimeException ("OpColon still exists at codegen");
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpColon (env, stream, method);
                }
            };
    }
}
