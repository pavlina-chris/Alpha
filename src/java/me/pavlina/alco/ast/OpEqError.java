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
 * Special-case = error operator. Created for use of a single =, which is
 * invalid in Alpha expressions, and displays an error message.
 */
public class OpEqError extends Expression.Operator {

    Token token;

    public static final Expression.OperatorCreator CREATOR;

    public OpEqError (Env env, TokenStream stream, Method method) throws CError
    {
        token = stream.next ();
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.REL_EQ;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        throw Unexpected.at (":= or ==", token);
    }

    public Instruction getInstruction () {
        return null;
    }

    public Type getType () {
        return null;
    }

    public void genLLVM (Env env, Emitter emitter, Function function)
    {
    }

    public List<AST> getChildren () {
        // Must not throw: passes.ConstantFold
        return null;
    }

    public Token getToken () {
        throw new RuntimeException ("OpEqError.getToken()");
    }

    public void print (java.io.PrintStream out) {
        out.print ("(=<error>)");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw new RuntimeException ("OpEqError.checkPointer()");
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpEqError (env, stream, method);
                }
            };
    }

}
