// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.Arrays;

/**
 * Dereference operator. */
public class OpDeref extends Expression.Operator {
    Token token;
    Expression[] children;
    Instruction instruction;

    public static final Expression.OperatorCreator CREATOR;

    public OpDeref (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[1];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.DEREF;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children[0] = op;
        op.setParent (this);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return children[0].getType ().getSubtype ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (children[0].getType ().getEncoding () != Type.Encoding.POINTER) {
            throw CError.at ("cannot dereference non-pointer", token);
        }
    }

    public void checkPointer (boolean write, Token token) throws CError {
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        children[0].genLLVM (env, emitter, function);
        return children[0].getInstruction ();
    }

    public void print (java.io.PrintStream out) {
        out.print ("(deref ");
        children[0].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        Instruction ptr = children[0].getInstruction ();
        instruction = new LOAD ()
            .type (LLVMType.getLLVMName (getType ()))
            .pointer (ptr)
            ._volatile (getType ().isVolatile ());
        function.add (instruction);
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
                    return new OpDeref (env, stream, method);
                }
            };
    }
}
