// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.CError;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.Emitter;
import me.pavlina.alco.llvm.Function;
import me.pavlina.IndentOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * AST base class. All AST objects extend this. It provides basic tree
 * analysis functions for optimisation passes, and printing for debug
 * purposes. */
public abstract class AST
{

    AST parent;

    /**
     * Get the token that represents this AST object. */
    public abstract Token getToken ();

    /**
     * Get a List of all AST children. This is used for various compile passes,
     * such as the name resolver and the constant arithmetic folder. The List
     * will never be changed in length, but elements may be changed in place.
     * May return null for no children.
     */
    public abstract List<AST> getChildren ();

    /**
     * Get the parent. Will return null for the root. */
    public AST getParent () {
        return parent;
    }

    /**
     * Set the parent. */
    public void setParent (AST parent) {
        this.parent = parent;
    }

    /**
     * Check types and resolve names. All items which create names, of course,
     * should register them. */
    public abstract void checkTypes (Env env, Resolver resolver) throws CError;

    /**
     * Generate the LLVM code for the item. It is assumed that once this
     * method is called, all checking and rewriting passes have been finished,
     * so it must never throw an exception.
     * @param env Compile environment
     * @param emitter LLVM emitter object
     * @param function Current LLVM function object; may be null outside of
     * a function.
     */
    public abstract void genLLVM (Env env, Emitter emitter, Function function);

    /**
     * Print a description of the AST object to the PrintStream. */
    public abstract void print (PrintStream out);

    /**
     * Print an indented description of the AST object.
     * @param indent Number of spaces to indent by */
    public void print (PrintStream out, int indent) {
        this.print (new PrintStream (new IndentOutputStream (out, indent)));
    }
}
