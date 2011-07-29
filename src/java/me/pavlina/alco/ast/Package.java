// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Keywords;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

/**
 * AST package. This represents (and parses) an entire file. */
public class Package extends AST
{
    private String name;
    private boolean executable;
    private List<AST> children;
    private Token token;

    /**
     * Read a package from the token stream. */
    public Package (TokenStream stream, Env env) throws CError {
        children = new ArrayList<AST> ();

        // Get the first token
        token = stream.peek ();

        // Read the info line
        executable = Package.readExecPackage (stream, env);
        name = Package.readName (stream, env);

        // After this come the children
        while (true) {
            AST child = readChild (stream, env);
            if (child == null) break;
            children.add (child);
        }
    }

    /**
     * Read the "executable" or "package" declaration.
     * @return Whether the declaration was "executable" */
    private static boolean readExecPackage (TokenStream stream,
                                            Env env) throws CError {
        Token token = stream.next ();
        if (token.is (Token.NO_MORE)) {
            throw new CError ("no code in source file " +
                              stream.getLexer ().filename ());
        } else if (token.is (Token.WORD, "executable")) {
            return true;
        } else if (token.is (Token.WORD, "package")) {
            return false;
        } else {
            throw Unexpected.at ("'package' or 'executable'", token);
        }
    }

    /**
     * Read the package name and semicolon. */
    private static String readName (TokenStream stream, Env env) throws CError {
        String name;
        Token token = stream.next ();
        if (token.is (Token.NO_MORE)) {
            throw UnexpectedEOF.after ("name", stream.last ());
        } else if (!token.is (Token.WORD)) {
            throw Unexpected.at ("name", token);
        } else if (Keywords.isKeyword (token.value, true)) {
            throw Unexpected.at ("name", token);
        }
        name = token.value;

        token = stream.next ();
        if (token.is (Token.NO_MORE)) {
            throw UnexpectedEOF.after (";", stream.last ());
        } else if (!token.is (Token.OPER, ";")) {
            throw Unexpected.after (";", stream.last ());
        }
        return name;
    }

    /**
     * Read one of the items in the file.
     * @return Item, or null on EOF. */
    private AST readChild (TokenStream stream, Env env) throws CError {

        Token token = stream.peek ();
        if (token.is (Token.NO_MORE)) return null;
        
        else if (token.is (Token.WORD, "extern"))
            return new Extern (stream, env);

        // No keyword: must be a method
        else
            return new Method (stream, env, /* allowStatic */ false, this);
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return children;
    }

    public String getName () {
        return name;
    }

    public boolean isExecutable () {
        return executable;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        for (AST i: children) {
            if (Method.class.isInstance (i)) {
                resolver.addFunction ((Method) i, i.getToken ());
            }
            else if (Extern.class.isInstance (i)) {
                resolver.addFunction ((Extern) i, i.getToken ());
            }
        }
        for (AST i: children) {
            i.checkTypes (env, resolver);
        }
    }

    public void merge (Package other, Env env) throws CError {
        if (! name.equals (other.name)) {
            throw CError.at ("split package has multiple names",
                             other.getToken ());
        }
        if (executable != other.executable) {
            env.warning_at ("executability of split package does not match",
                            other.getToken ());
            executable = true;
        }
        children.addAll (other.children);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        StringConstant pkgname = StringConstant.getPointerConst
            ("@AL_PKG_NAME", name);
        emitter.add (pkgname);
        for (AST i: children) {
            i.genLLVM (env, emitter, function);
        }
    }

    public void print (PrintStream out) {
        if (executable) {
            out.printf ("Executable %s\n", name);
        } else {
            out.printf ("Package %s\n", name);
        }
        for (AST item: children) {
            item.print (out, 2);
        }
    }
}
