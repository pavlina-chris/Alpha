// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.IntLimits;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.llvm.LLVMEmitter;
import me.pavlina.alco.llvm.Function;
import me.pavlina.alco.llvm.load;
import me.pavlina.alco.llvm.LLVMType;
import java.util.List;
import java.io.PrintStream;

/**
 * Variable */
public class NameValue extends Expression
{
    private Token token;
    private Type type;
    private String name;
    private String realName;
    private String valueString;

    /**
     * Create a NameValue from the stream */
    public NameValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        if (token.is (Token.EXTRA, "$$name")) {
            // Extrastandard id $$name allows using anything as a name
            Token nextToken = stream.next ();
            if (nextToken.is (Token.NO_MORE))
                throw UnexpectedEOF.after ("name", token);
            name = nextToken.value;

        } else {
            if (!token.is (Token.WORD))
                throw new RuntimeException ("NameValue created for non-name");
            if (Keywords.isKeyword (token.value, true))
                throw Unexpected.at ("name", token);
            name = token.value;
        }
    }

    /**
     * Create a NameValue from a name */
    public NameValue (String name) {
        this.name = name;
    }

    public String getValueString () {
        return valueString;
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Resolver.Variable var = resolver.getVariable (name, token);
        realName = var.getName ();
        type = var.getType ();
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        valueString = new
            load (emitter, function)
            .pointer (LLVMType.getLLVMName (type), realName)
            .build ();
    }
    
    public void print (PrintStream out) {
        out.print (name);
    }

    public String getName () {
        return name;
    }

    public String getRealName () {
        return realName;
    }

    public void checkPointer (boolean write, Token token) throws CError {
        if (type.isConst () && write)
            throw CError.at ("cannot assign to constant", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return realName;
    }
}
