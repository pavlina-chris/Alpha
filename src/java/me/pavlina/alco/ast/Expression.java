// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.Instruction;
import me.pavlina.alco.llvm.Function;
import me.pavlina.alco.llvm.Emitter;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;

/**
 * Expression base class and parser. All actual Expression objects should be
 * a subclass of one of the specialised Expression subclasses.
 * When adding operators, look at the static initialiser for most (only a
 * select few, like function calls, need special wiring). */
public abstract class Expression extends AST implements HasType
{

    /**
     * Get the LLVM instruction that represents the value of this expression.
     * It is assumed that AST#genLLVM() has been run before calling this. */
    public abstract Instruction getInstruction ();

    /**
     * Get the type of the expression. */
    public abstract Type getType ();

    /**
     * Check whether getting a pointer to the value is possible. This should
     * throw a proper exception if not.
     * @param write Whether write access to the pointer is required
     * @param token Token to throw an exception on if not possible
     */
    public abstract void checkPointer (boolean write, Token token)
        throws CError;

    /**
     * Get a pointer to the value, if possible. This is used IN PLACE OF
     * genLLVM() and getInstruction(), and the behavior when calling both
     * is undefined. This can write code if it needs to (for example, to access
     * an array element). The behavior when calling this on an object for which
     * checkPointer() throws is undefined (though if you're a good programmer,
     * you'll make this function empty in that case...)
     */
    // I just know that after writing "if you're a good programmer", I'll think
    // of some reason to break my own rule :-)
    // -- Yep, I did. See OpCast.
    public abstract Instruction getPointer (Env env, Emitter emitter,
                                            Function function);

    public enum Associativity {LEFT, RIGHT}
    public enum Arity {UNARY, BINARY}

    /**
     * Operator. All operators must extend this. */
    public static abstract class Operator extends Expression
    {
        /**
         * Return the operator precedence. The semantics of this number are
         * unspecified, other than that operators which return higher numbers
         * have higher precedence. */
        public abstract int getPrecedence ();

        /**
         * Return the associativity. Left-associative operators are as if read
         * from left to right, and vice versa. */
        public abstract Associativity getAssociativity ();

        /**
         * Return the arity. */
        public abstract Arity getArity ();

        /**
         * Set the operands. For binary operators, the semantics are obvious;
         * for unary operators, the operands will be in 'left', and 'right'
         * will be null. */
        public abstract void setOperands (Expression left, Expression right);
    }

    /**
     * Operator creator interface */
    public static interface OperatorCreator {
        /**
         * Create and return an operator, given Env and TokenStream. */
        public Operator create (Env env, TokenStream stream,
                                Method method) throws CError;
    }
}
