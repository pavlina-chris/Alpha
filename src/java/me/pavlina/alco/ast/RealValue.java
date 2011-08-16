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
 * Floats */
public class RealValue extends Expression
{
    double value;
    Token token;
    Type type;

    /**
     * Create a RealValue from the stream */
    public RealValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        if (!token.is (Token.REAL))
            throw new RuntimeException ("RealValue created for non-real");

        try {
            value = Double.parseDouble (token.value);
        } catch (NumberFormatException e) {
            throw CError.at ("invalid double", token);
        }
    }

    /**
     * Create a Double from a number */
    public RealValue (double value) {
        this.value = value;
    }

    public void setValue (double value) {
        this.value = value;
    }

    public double getValue () {
        return this.value;
    }

    public Instruction getInstruction () {
        return new Placeholder
            (String.format ("0x%016x", Double.doubleToRawLongBits (value)),
             "double");
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        type = new Type (env, "double", null);
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
        out.print (value);
    }

}
