// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.compiler;
import me.pavlina.alco.ast.AST;
import me.pavlina.alco.ast.FunctionLike;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.llvm.LLVMEmitter;
import me.pavlina.alco.llvm.Function;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class PackageReader
{
    private PackageReader () {}

    /**
     * Read the methods and classes from a .so file into the resolver. */
    public static void readPackage (File path, Env env, Resolver resolver,
                                    Token token) throws CError
    {
        // Run 'nm' to list all the symbols. Get anything starting with
        // _M and add it to the resolver.
        
        try {
            Process nm = new ProcessBuilder ("nm", path.getPath ())
                .redirectErrorStream (false)
                .start ();
            InputStream inp = nm.getInputStream ();
            Scanner sc = new Scanner (inp);
            
            while (sc.hasNextLine ()) {
                String line = sc.nextLine ();
                String[] split = line.split ("\\s+");
                if (split.length != 3)
                    throw new CError ("invalid output from nm");
                String symbol = split[2];
                if (!symbol.startsWith ("_M")) continue;
                
                String[] name_and_types = symbol.split ("\\.");
                if (name_and_types.length < 2)
                    throw new CError ("invalid mangled method name " + symbol);

                // Remove _M from name
                String name = name_and_types[0].substring (2);

                // Return type
                Type type;
                if (name_and_types[1].equals ("")) {
                    type = null;
                } else {
                    type = Type.fromEncodedName (env, name_and_types[1]);
                    if (type == null)
                        throw new CError ("invalid mangled method name "
                                          + symbol);
                }

                // Argument types
                List<Type> argtypes = new ArrayList<Type> ();
                for (int i = 2; i < name_and_types.length; ++i) {
                    Type t = Type.fromEncodedName (env, name_and_types[i]);
                    if (t == null)
                        throw new CError ("invalid mangled method name "
                                          + symbol);
                    argtypes.add (t);
                }

                resolver.addFunction
                    (new ImportedFunction (name, type, argtypes), token);
            }
            
            int rc;
            try {
                rc = nm.waitFor ();
            } catch (InterruptedException e) {
                throw new CError ("package reader thread interrupted");
            } finally {
                Thread.interrupted ();
            }
            if (rc != 0)
                throw new CError ("nm exited" + rc);
        } catch (IOException e) {
            throw new CError ("error reading from nm");
        }
    }
}

class ImportedFunction extends FunctionLike
{
    public ImportedFunction (String name, Type type, List<Type> argtypes) {
        super ();
        this.name = name;
        this.type = type;
        this.argtypes = argtypes;
        this.argnames = new ArrayList<String> ();
        for (int i = 0; i < argtypes.size (); ++i)
            this.argnames.add ("");
    }
    
    public void print (java.io.PrintStream out) {
        out.print ("Imported function " + this.toString ());
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        throw new RuntimeException ("ImportedFunction.genLLVM ()");
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
    }

    public List<AST> getChildren () {
        return null;
    }

    public Token getToken () {
        return null;
    }
}
