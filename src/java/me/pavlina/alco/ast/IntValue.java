// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

// This is a METRIC SHITTON of imports for an "IntValue"...
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
import java.math.BigInteger;
import java.io.PrintStream;

/**
 * Integers */
public class IntValue extends Expression
{
    BigInteger value;
    Token token;
    Type type;

    /**
     * Create an IntValue from the stream */
    public IntValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        assert token.is (Token.INT);

        String number;
        int radix;
        if (token.value.startsWith ("0x") || token.value.startsWith ("0X")) {
            number = token.value.substring (2);
            radix = 16;
        } else if (token.value.startsWith ("0D") ||
                   token.value.startsWith ("0d")) {
            number = token.value.substring (2);
            radix = 10;
        } else if (token.value.startsWith ("0O") ||
                   token.value.startsWith ("0o")) {
            number = token.value.substring (2);
            radix = 8;
        } else if (token.value.startsWith ("0B") ||
                   token.value.startsWith ("0b")) {
            number = token.value.substring (2);
            radix = 2;
        } else {
            number = token.value;
            radix = 10;
        }
        try {
            value = new BigInteger (number, radix);
        } catch (NumberFormatException e) {
            throw CError.at ("invalid integer", token);
        }
    }

    /**
     * Create an IntValue from a number */
    public IntValue (BigInteger value) {
        this.value = value;
    }

    /**
     * Set the value from BigInteger */
    public void setValue (BigInteger value) {
        this.value = value;
    }

    /**
     * Set the value from string */
    public void setValue (String value, int radix) {
        this.value = new BigInteger (value, radix);
    }

    /**
     * Get the value */
    public BigInteger getValue () {
        return this.value;
    }

    public Instruction getInstruction () {
        return new Placeholder (this.value.toString (),
                                LLVMType.getLLVMName (type));
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        // The default type for an integer is the first of (int, i64, u64)
        // which can hold the value. Note that while an IntValue created from
        // the token stream cannot be negative, this is run after arithmetic
        // folding.
        if (value.compareTo (IntLimits.INT_MIN) >= 0 &&
            value.compareTo (IntLimits.INT_MAX) <= 0)
            type = new Type (env, "int", null);
        else if (value.compareTo (IntLimits.I64_MIN) >= 0 &&
                 value.compareTo (IntLimits.I64_MAX) <= 0)
            type = new Type (env, "i64", null);
        else if (value.compareTo (IntLimits.U64_MIN) >= 0 &&
                 value.compareTo (IntLimits.U64_MAX) <= 0)
            type = new Type (env, "u64", null);
        else
            throw CError.at ("integer magnitude too high", token);
        type.setValue (value);
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

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }

    public void print (PrintStream out) {
        out.print (value);
    }

}
