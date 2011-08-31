// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import java.util.List;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.parse.StringParser;

/**
 * String literals */
public class StringValue extends Expression
{
    Token token;
    Type type;
    Instruction instruction;
    List<Byte> value;

    public StringValue (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.peek ();
        assert token.is (Token.STRING);
        value = StringParser.parse (env, stream);
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
        // Yay! Simple checkTypes ()!
        type = new Type (env, "u8", null).getArray (env);
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        StringBuilder sb = new StringBuilder ();
        sb.append ("c\"");
        for (byte i: value) {
            // Printable ASCII
            if (i >= ' ' && i <= '~' && i != '"' && i != '\\')
                sb.append ((char) i);
            else
                sb.append (String.format ("\\%02x", i));
        }
        sb.append ("\\00\"");
        String arrTy = "[" + (value.size () + 1) + " x i8]";
        String size_t = "i" + env.getBits ();
        String strTy = "{" + size_t + ", " + arrTy + "*}";
        Constant c = new Constant (arrTy, sb.toString (), "internal");
        emitter.add (c);
        Instruction arrStruct = new ALLOCA ().type (strTy);
        Instruction arrStructSz = new GETELEMENTPTR ()
            .type (strTy + "*").rtype (size_t).value (arrStruct)
            .addIndex (0).addIndex (0);
        function.add (arrStruct);
        function.add (arrStructSz);
        function.add (new STORE ().pointer (arrStructSz)
                .type (size_t)
                .value (Integer.toString (value.size ())));
        Instruction arrStructPtr = new GETELEMENTPTR ()
            .type (strTy + "*").rtype (arrTy + "*").value (arrStruct)
            .addIndex (0).addIndex (1);
        function.add (arrStructPtr);
        function.add (new STORE ().pointer (arrStructPtr).value (c));
        instruction = new CONVERT ()
            .op ("bitcast").stype (strTy + "*").dtype ("%.nonprim")
            .value (arrStruct);
        function.add (instruction);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("string literal has no address; " +
                "assign to variable first", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }

    public void print (java.io.PrintStream out) {
        // Laziness!!!!!
        out.print (token.value);
    }
}
