// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.llvm.*;
import java.util.List;
import java.util.Arrays;

/**
 * Tuple operator. This joins expressions for use in calls and multiple
 * assigns. It should never be left at the genLLVM() stage.
 */
public class OpComma extends Expression.Operator {
    Token token;
    Expression[] children;

    public static final Expression.OperatorCreator CREATOR;


    public OpComma (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    /**
     * Placeholder empty tuple (for void function calls) */
    public OpComma (Token token) {
        this.token = token;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.COMMA;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        if (children == null) return;
        children[0] = left;
        children[1] = right;
        left.setParent (this);
        right.setParent (this);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        throw CError.at ("performing operation on a tuple", token);
    }

    public Instruction getInstruction () {
        throw new RuntimeException ("OpComma still exists at codegen");
    }

    public Type getType () {
        throw new RuntimeException ("OpComma still exists at codegen");
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        throw new RuntimeException ("OpComma still exists at codegen");
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
        if (OpComma.class.isInstance (children[0])) {
            ((OpComma) children[0]).printNoParens (out);
        } else {
            children[0].print (out);
        }
        out.print (" ");
        children[1].print (out);
    }

    public void unpack (List<Expression> into) {
        if (children == null) return;
        if (OpComma.class.isInstance (children[0]))
            ((OpComma) children[0]).unpack (into);
        else
            into.add (children[0]);
        into.add (children[1]);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw new RuntimeException ("OpComma still exists at codegen");
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpComma (env, stream, method);
                }
            };
    }
}
