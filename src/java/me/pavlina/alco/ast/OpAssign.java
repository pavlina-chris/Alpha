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
import me.pavlina.alco.codegen.Assign;
import me.pavlina.alco.codegen.AssignCall;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Assignment operator. This is a basic assignment operator, but also
 * handles the arithmetic assigns by creating the equivalent arithmetic
 * operator. */
public class OpAssign extends Expression.Operator {
    private Token token;
    private Expression[] children;
    List<Expression> srcs, dests;
    List<Type> types;
    String valueString;
    Assign assign;
    AssignCall assigncall;

    public static Expression.OperatorCreator CREATOR;


    public OpAssign (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpAssign (Token token) {
        this.token = token;
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

    public void setOperands (Expression left, Expression right) {
        children[0] = left;
        children[1] = right;
    }

    public String getValueString () {
        if (assign == null)
            return valueString;
        else
            return assign.getValueString ();
    }

    public Type getType () {
        return children[0].getType ().getNotConst ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {

        // Special case: values = call
        if (OpCall.class.isInstance (children[1])) {
            assigncall = new AssignCall (token)
                .dests (children[0])
                .source ((OpCall) children[1]);
            assigncall.checkTypes (env, resolver);

        } else {
            assign = new Assign (token)
                .dests (children[0]).sources (children[1]);
            assign.checkTypes (env, resolver);
        }
    }

    // Special checkTypes for values=call special case
    private void checkTypesCall (Env env, Resolver resolver) throws CError {
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("assignment has no address", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign ");
        children[0].print (out);
        out.print (" ");
        children[1].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (OpCall.class.isInstance (children[1])) {
            assigncall.genLLVM (env, emitter, function);
        } else {
            assign.genLLVM (env, emitter, function);
        }
    }

    @SuppressWarnings("unchecked") // :-( I'm sorry.
    public List<AST> getChildren () {
        // Oh FFS Java, why can't List<Expression> sub in for List<AST>? :-(
        // (I suppose they probably have some important reason, which I should
        //  look into for designing my own subtype assignment semantics)
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAssign (env, stream, method);
                }
            };
    }
}
