// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * AST extern declaration */
public class Extern extends FunctionLike
{
    Token token;
    AST parent;

    /**
     * Parse and initialise. */
    public Extern (TokenStream stream, Env env) throws CError
    {
        super ();
        token = stream.next ();
        if (!token.is (Token.WORD, "extern"))
            throw new RuntimeException ("Extern instantiated with no kwd");

        this.parse (stream, env, /* allowStatic*/ false,
                    /* allowNomangle */ true, /* allowAllowconflict */ false,
                    /* allowGlobal */ false, /* allowMultRet */ false,
                    /* nomangleRedundant */ true, /* allowUnnamed */ true,
                    /* allowOperator */ false);

        nomangle = true;
        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";")) {
            System.out.println (temp);
            throw Unexpected.after (";", stream.last ());
        }
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return null;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        // Do nothing
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // Declaration at top of file
        FDeclare decl = new FDeclare
            ("@" + this.getMangledName (),
             LLVMType.getLLVMNameV (this.getType ()));
        for (Type i: argtypes) {
            if (i.getEncoding () == Type.Encoding.OBJECT ||
                i.getEncoding () == Type.Encoding.ARRAY) {
                decl.addParameter ("i64");
                decl.addParameter ("i64");
            } else {
                decl.addParameter (LLVMType.getLLVMName (i));
            }
        }
        emitter.add (decl);
    }

    public void print (PrintStream out) {
        out.println (this.toString ());
    }
}
