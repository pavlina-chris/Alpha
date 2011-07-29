// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.*;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.lex.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Overloaded operator. */
public class Overload {
    Token token;
    String valueString;
    String operator;
    FunctionLike fun;
    Method method;
    Expression[]  children;

    public Overload (Token token, Method method) {
        this.token = token;
        this.method = method;
    }

    /**
     * Required: Set operator */
    public Overload operator (String operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Required: Set children
     * @param children Either one or two children */
    public Overload children (Expression... children) {
       this.children = children;
       return this;
    }

    /**
     * Check if there is a matching overload. If a resolver error occurs, the
     * error is printed and false is returned. */
    public boolean find (Env env, Resolver resolver) {
        try {
            fun = resolver.getFunction (operator, Arrays.asList (children),
                                        token);
        } catch (CError e) {
            System.err.println
                ("########################################################");
            System.err.println
                ("###     Cannot resolve operator as overload call     ###");
            e.print (System.err);
            System.err.println ();
            System.err.println
                ("########################################################");
            System.err.println
                ("###         Cannot resolve standard operator         ###");
            return false;
        }
        return true;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        List<Type> destTypes = fun.getArgTypes ();
        for (int i = 0; i < children.length; ++i) {
            Type.checkCoerce (children[i], destTypes.get (i), token);
        }
        if (fun.getTypes ().size () > 0)
            method.requireTemps (1);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        List<String> valueStrings = new ArrayList<String> ();
        List<Type> destTypes = fun.getArgTypes ();
        for (int i = 0; i < children.length; ++i) {
            children[i].genLLVM (env, emitter, function);
            String val = children[i].getValueString ();
            Cast c = new Cast (token)
                .value (val)
                .type (children[i].getType ()).dest (destTypes.get (i));
            c.genLLVM (env, emitter, function);
            valueStrings.add (c.getValueString ());
        }

        // Bitcast the temporary %.T0 (i128*) into the proper pointer types
        List<String> temps = new ArrayList<String> ();
        List<Type> returns = fun.getTypes ();
        // Make temps[i] equiv. returns[i]
        temps.add ("");
        for (int i = 1; i < returns.size (); ++i) {
            String temp = new Conversion (emitter, function)
                .operation (Conversion.ConvOp.BITCAST)
                .source ("i128*", "%.T0")
                .dest (LLVMType.getLLVMName (returns.get (i)) + "*")
                .build ();
            temps.add (temp);
        }

        call callbuilder = new call (emitter, function)
            .type (LLVMType.getLLVMName (this.getType ()))
            .pointer ("@" + fun.getMangledName ());
        for (int i = 1; i < returns.size (); ++i) {
            callbuilder.arg (LLVMType.getLLVMName (returns.get (i)) + "*",
                             temps.get (i));
        }
        for (int i = 0; i < children.length; ++i) {
            callbuilder.arg (LLVMType.getLLVMName
                             (destTypes.get (i).getType ()),
                             valueStrings.get (i));
        }
        valueString = callbuilder.build ();
    }

    public Type getType () {
        Type t = fun.getType ();
        if (t == null) return null;
        return t.getNormalised ();
    }

    public String getValueString () {
        return valueString;
    }
}
