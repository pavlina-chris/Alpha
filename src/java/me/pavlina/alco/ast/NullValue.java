// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

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

/**
 * Null */
public class NullValue extends Expression
{
    Token token;
    Type type;
    Instruction instruction;

    /**
     * Create a NullValue from the stream */
    public NullValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        if (!token.is (Token.WORD, "null"))
            throw new RuntimeException ("NullValue created for non-null");
    }

    /**
     * Create a NullValue */
    public NullValue () {
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        type = Type.getNull ();
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        instruction = new LOAD ()
            .type ("%.nonprim")
            .pointer ("@.null");
        function.add (instruction);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("primitive literal has no address", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }
    
    public void print (PrintStream out) {
        out.print ("null");
    }

}
