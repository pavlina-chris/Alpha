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
 * AST method. This represents (and parses) just one method. */
public class Method extends FunctionLike
{
    private Token token;
    private List<AST> children;

    /**
     * Parse and initialise the method. */
    public Method (TokenStream stream, Env env, boolean allowStatic,
                   Package pkg)
        throws CError
    {
        super ();
        token = stream.peek ();
        this.parse (stream, env, allowStatic, /* allowNomangle */ true,
                    /* alloowAllowconflict */ true, /* allowGlobal */ true,
                    /* nomangleRedundant */ false, /* allowUnnamed */ false);
        this.pkg = pkg;

        children = new ArrayList<AST> ();

        // Create a Scope for this method. It will parse the code.
        Scope scope = new Scope (env, stream, this);
        children.add (scope);
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return children;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        resolver.addFunction (this, this.getToken ());
        Resolver newResolver = new Resolver (resolver);
        for (int i = 0; i < argtypes.size (); ++i)
            newResolver.addVariable (argnames.get (i), argtypes.get (i));
        for (AST i: children)
            i.checkTypes (env, newResolver);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        // Declaration at top of file
        FDeclare decl = new FDeclare
            (this.getMangledName (), LLVMType.getLLVMName (this.getType ()));
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

        // Method
        Function func = new Function
            (this.getMangledName (), LLVMType.getLLVMName (this.getType ()));
        for (int i = 0; i < argtypes.size (); ++i) {
            Type.Encoding enc = argtypes.get (i).getEncoding ();
            if (enc == Type.Encoding.OBJECT ||
                enc == Type.Encoding.ARRAY) {
                func.addParameter ("i64", "%." + Integer.toString (i) + ".T");
                func.addParameter ("i64", "%." + Integer.toString (i) + ".V");
            } else {
                func.addParameter
                    (LLVMType.getLLVMName (argtypes.get (i)),
                     "%." + Integer.toString (i));
            }
        }
        emitter.add (func);

        // Save the counter
        Object countSave1 = emitter.resetCount ("%");
        Object countSave2 = emitter.resetCount ("%.L");
        
        // Copy arguments into local variables
        for (int i = 0; i < argtypes.size (); ++i) {
            Type.Encoding enc = argtypes.get (i).getEncoding ();
            new alloca (emitter, func)
                .type (LLVMType.getLLVMName (argtypes.get (i)))
                .result ("%" + argnames.get (i))
                .build ();
            if (enc == Type.Encoding.OBJECT ||
                enc == Type.Encoding.ARRAY) {
                String pdest1 = new getelementptr (emitter, func)
                    .type ("%.nonprim")
                    .pointer ("%" + argnames.get (i))
                    .inbounds (true)
                    .addIndex (0).addIndex (0).build ();
                String pdest2 = new getelementptr (emitter, func)
                    .type ("%.nonprim")
                    .pointer ("%" + argnames.get (i))
                    .inbounds (true)
                    .addIndex (0).addIndex (1).build ();
                new store (emitter, func)
                    .pointer (pdest1)
                    .value ("i64", "%." + Integer.toString (i) + ".T")
                    .build ();
                new store (emitter, func)
                    .pointer (pdest2)
                    .value ("i64", "%." + Integer.toString (i) + ".V")
                    .build ();
            } else {
                new store (emitter, func)
                    .pointer ("%" + argnames.get (i))
                    .value (LLVMType.getLLVMName (argtypes.get (i)),
                            "%." + Integer.toString (i))
                    .build ();
            }
        }

        // Write the function code
        for (AST i: children) {
            i.genLLVM (env, emitter, func);
        }

        // Restore the counter
        emitter.setCount ("%", countSave1);
        emitter.setCount ("%.L", countSave2);
    }

    public void print (PrintStream out) {
        out.print (this.toString ());
        for (AST i: children) {
            out.print ("\n");
            i.print (out, 2);
        }
        out.println ();
    }
}
