// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.llvm.*;
import java.util.List;
import java.io.PrintStream;

/**
 * Booleans */
public class BoolValue extends Expression
{
    boolean value;
    Token token;
    Type type;

    /**
     * Create a BoolValue from the stream */
    public BoolValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        if (token.is (Token.WORD, "true"))
            value = true;
        else if (token.is (Token.WORD, "false"))
            value = false;
        else
            assert false: token;
    }

    public boolean getValue () {
        return value;
    }
    
    public Instruction getInstruction () {
        return new Placeholder (value ? "-1" : "0", "i8");
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        type = new Type (env, "bool", null);
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // Generate nothing. We instead emit a constant from getValueString()
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("primitive literal has no address", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    public void print (PrintStream out) {
        out.print (value ? "true" : "false");
    }
}
