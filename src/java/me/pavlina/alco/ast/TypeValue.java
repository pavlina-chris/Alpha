// Copyright (c) 2011, Christoper Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.IntLimits;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.llvm.*;
import java.util.List;
import java.io.PrintStream;

// This is used to hold the cast operator's type. I was having problems with
// the cast operator looking like a binary op, but really being unary.
/**
 * Type */
public class TypeValue extends Expression
{
    Token token;
    Type type;

    /**
     * Create a TypeValue from the stream */
    public TypeValue (Env env, TokenStream stream) throws CError {
        token = stream.peek ();
        type = TypeParser.parse (stream, env);
    }

    /**
     * Create a TypeValue from a type */
    public TypeValue (Type type) {
        this.type = type;
        token = null;
    }

    /**
     * @return null */
    public Instruction getInstruction () {
        return null;
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        throw CError.at ("using type as value", token);
    }

    public Type getType () {
        return type;
    }

    /**
     * no-op */
    public void genLLVM (Env env, Emitter emitter, Function function) {}

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("assigning to type", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    public void print (PrintStream out) {
        // Just a bit of commentary, since I feel like ranting: I like
        // using .toString(). The argument against that is that println() is
        // null-safe. I DON'T WANT NULL-SAFETY!!! I want to KNOW if 'type' is
        // null. IT SHOULDN'T BE. Back when I was trying to write this in
        // Python, I had quite a few errors from LLC itself, perplexed at the
        // "None" in the generated code. I'd much rather an exception than
        // trying to track down the line which dumped a given string into the
        // file.
        out.println (type.toString ());
    }
}
