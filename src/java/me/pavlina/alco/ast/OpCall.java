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
        return function.getType ().getNormalised ();
    }

    public void setOperands (Expression op, Expression ignore) {
        children[1] = op;
    }

    /**
     * This checkTypes does not allocate temporary variables. It is used
     * by codegen.AssignCall. */
    public void checkTypes_ (Env env, Resolver resolver) throws CError {
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

        checkTypes_ (env, resolver);

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
            .type (LLVMType.getLLVMNameV (this.getType ()))
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
        if (valueString == null) {
            valueString = new load (emitter, function)
                .pointer ("%.nonprim", "@.null")
                .build ();
        }
    }

    /**
     * Get the parent method of this call. Used by codegen.AssignCall. */
    public Method getMethod () {
        return method;
    }

    /**
     * Get the FunctionLike to which this call resolved. Used by
     * codegen.AssignCall. */
    public FunctionLike getFunction () {
        return function;
    }

    /**
     * Get the arguments list. Used by codegen.AssignCall. */
    public List<Expression> getArgs () {
        return args;
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
