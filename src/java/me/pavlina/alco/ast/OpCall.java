// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Call operator. This does not parse, as it is always explicitly created from
 * a known name. */
public class OpCall extends Expression.Operator {
    private Token token;
    private String valueString;
    private AST[] children;
    private List<Expression> args;
    private FunctionLike function;
    Method method;

    public OpCall (Token token, Expression expr, Method method) {
        this.token = token;
        this.method = method;
        children = new AST[] { expr, null };
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.CALL;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public Type getType () {
        Type t = function.getType ();
        if (t == null) return null;
        return t.getNotConst ();
    }

    public void setOperands (Expression op, Expression ignore) {
        children[1] = op;
    }

    // Code shared between checkTypes and checkTypesMult
    private void checkTypesShared (Env env, Resolver resolver) throws CError {
        if (!NameValue.class.isInstance (children[0])) {
            throw Unexpected.at ("name", children[0].getToken ());
        }
        args = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (children[1])) {
            ((OpComma) children[1]).unpack (args);
        } else {
            args.add ((Expression) children[1]);
        }
        for (Expression i: args)
            i.checkTypes (env, resolver);
        function = resolver.getFunction
            (((NameValue) children[0]).getName (), args,
             children[0].getToken ());

        // Check all argument types
        List<Type> destTypes = function.getArgTypes ();
        for (int i = 0; i < args.size (); ++i) {
            Type.checkCoerce (args.get (i), destTypes.get (i), token);
        }
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {

        checkTypesShared (env, resolver);

        // checkTypes() is used when we only want one value, so request a
        // temporary.
        int n = function.getTypes ().size ();
        if (n > 0)
            method.requireTemps (1);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        List<String> valueStrings = new ArrayList<String> ();
        List<Type> destTypes = this.function.getArgTypes ();
        for (int i = 0; i < args.size (); ++i) {
            args.get (i).genLLVM (env, emitter, function);
            String val = args.get (i).getValueString ();
            Cast c = new Cast (token)
                .value (val)
                .type (args.get (i).getType ()).dest (destTypes.get (i));
            c.genLLVM (env, emitter, function);
            valueStrings.add (c.getValueString ());
        }

        // Bitcast the temporary %.T0 (i128*) into the proper pointer types
        List<String> temps = new ArrayList<String> ();
        List<Type> returns = this.function.getTypes ();
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

        call callbuilder = new call
            (emitter, function)
            .type (LLVMType.getLLVMName (this.getType ()))
            .pointer ("@" + this.function.getMangledName ());
        for (int i = 1; i < returns.size (); ++i) {
            callbuilder.arg (LLVMType.getLLVMName (returns.get (i)) + "*",
                             temps.get (i));
        }
        for (int i = 0; i < args.size (); ++i) {
            callbuilder.arg (LLVMType.getLLVMName
                             (destTypes.get (i).getType ()),
                             valueStrings.get (i));
        }
        valueString = callbuilder.build ();
    }

    /**
     * Multiple-value checkTypes.
     *
     * When processing something like (x,y) = call(), OpCall handles the
     * assignment rather than OpAssign. This performs the checking and
     * inserts proper implicit casts.
     *
     * @param types Expected return types */
    public void checkTypesMult (Env env, Resolver resolver, List<Type> types)
        throws CError
    {
        checkTypesShared (env, resolver);

        // Make sure all return types can be coerced to the expected types.
        // If they need to be coerced, declare a temporary. Any ignored values
        // can share one temporary.
        List<Type> returns = this.function.getTypes ();
        int temps = 1;
        Type nullType = Type.getNull ();
        for (int i = 0; i < types.size (); ++i) {
            if (i == returns.size ()) break;
            if (returns.get (i).equals (types.get (i))) continue;
            if (types.get (i).equals (nullType)) {
                // Ignore
            } else {
                Type.checkCoerce (returns.get (i), types.get (i), token);
                if (i > 0)
                    ++temps;
            }
        }
        method.requireTemps (temps);
    }
    
    /**
     * Multiple-value codegen.
     *
     * When processing something like (x,y) = call(), OpCall handles the
     * assignment rather than OpAssign.
     *
     * @param types Expected return types. Should be the same thing passed to
     * the namesake parameter on checkTypesMult().
     * @param pointers LLVM-string pointers which match the types. Values will
     * be assigned to these. For values to be ignored, the given pointer does
     * not matter. */
    public void genLLVMMult (Env env, LLVMEmitter emitter, Function function,
                             List<Type> types, List<String> pointers)
    {
        List<String> valueStrings = new ArrayList<String> ();
        List<Type> destTypes = this.function.getArgTypes ();
        for (int i = 0; i < args.size (); ++i) {
            args.get (i).genLLVM (env, emitter, function);
            String val = args.get (i).getValueString ();
            Cast c = new Cast (token)
                .value (val)
                .type (args.get (i).getType ()).dest (destTypes.get (i));
            c.genLLVM (env, emitter, function);
            valueStrings.add (c.getValueString ());
        }

        // We have allocated a number of temporary variables, %.T0 and so on.
        // %.T0 is shared for all ignored values, and %.T1 ... are used to hold
        // pre-cast temporaries. Bitcast them as necessary. To make this easier,
        // pad the list with empties for arguments which do not require a
        // temporary.
        List<String> temps = new ArrayList<String> ();
        List<Type> returns = this.function.getTypes ();
        Type nullType = Type.getNull ();
        // First return is a real return. Any casting is done inline, and
        // ignoring just by literally ignoring the value.
        temps.add (""); 
        int tempsUsed = 0;
        for (int i = 1; i < returns.size (); ++i) {
            boolean ignore = false;
            boolean cast = false;
            if (i >= types.size ())
                // More returns than expected values. Ignore!
                ignore = true;
            else if (returns.get (i).equals (types.get (i)))
                // Same type. Nothing required - pass directly into pointer
                ;
            else if (types.get (i).equals (nullType))
                // Ignored
                ignore = true;
            else
                cast = true;
            String dest = "";
            if (ignore) dest = "%.T0";
            else if (cast) {
                dest = "%.T" + Integer.toString (tempsUsed + 1);
                ++tempsUsed;
            }
            if (ignore || cast) {
                String temp = new Conversion (emitter, function)
                    .operation (Conversion.ConvOp.BITCAST)
                    .source ("i128*", dest)
                    .dest (LLVMType.getLLVMName (returns.get (i)) + "*")
                    .build ();
                temps.add (temp);
            } else
                temps.add ("");
        }
        
        // Do the actual call
        call callbuilder = new call (emitter, function)
            .type (LLVMType.getLLVMName (this.getType ()))
            .pointer ("@" + this.function.getMangledName ());
        for (int i = 1; i < returns.size (); ++i) {
            if (temps.get (i).equals ("")) {
                // Return directly into pointer
                callbuilder.arg (LLVMType.getLLVMName (returns.get (i)) + "*",
                                 pointers.get (i));
            } else {
                // Return into temporary
                callbuilder.arg (LLVMType.getLLVMName (returns.get (i)) + "*",
                                 temps.get (i));
            }
        }
        for (int i = 0; i < args.size (); ++i) {
            callbuilder.arg (LLVMType.getLLVMName
                             (destTypes.get (i).getType ()),
                             valueStrings.get (i));
        }
        String firstReturn = callbuilder.build ();

        // Cast and assign the first return value
        if (!types.get (0).equals (nullType)) {
            String val;
            if (types.get (0).equals (returns.get (0))) {
                val = firstReturn;
            } else {
                Cast c = new Cast (token)
                    .value (firstReturn).type (returns.get (0))
                    .dest (types.get (0));
                c.genLLVM (env, emitter, function);
                val = c.getValueString ();
            }
            new store (emitter, function)
                .pointer (pointers.get (0))
                .value (LLVMType.getLLVMName (types.get (0)), val)
                .build ();
        }

        // Cast and assign? the subsequent return values
        for (int i = 1; i < returns.size (); ++i) {
            if (i == types.size ()) break;
            if (!temps.get (i).equals ("") && !types.get(i).equals(nullType)) {
                String tempVal = new load (emitter, function)
                    .pointer (LLVMType.getLLVMName (returns.get (i)),
                              temps.get (i))
                    .build ();
                Cast c = new Cast (token)
                    .value (tempVal).type (returns.get (i))
                    .dest (types.get (i));
                c.genLLVM (env, emitter, function);
                new store (emitter, function)
                    .pointer (pointers.get (i))
                    .value (LLVMType.getLLVMName (types.get (i)),
                            c.getValueString ())
                    .build ();
            }
        }
    }

    public String getValueString () {
        return valueString;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(call");
        for (AST i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public List<AST> getChildren () {
        return Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("function call has no address", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

}
