// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Assignment operator. This is a basic assignment operator, but also
 * handles the arithmetic assigns by creating the equivalent arithmetic
 * operator. */
public class OpAssign extends Expression.Operator {
    private Token token;
    private Expression[] children;
    List<Expression> srcs, dests;
    List<Type> types;
    String valueString;

    public static Expression.OperatorCreator CREATOR;


    public OpAssign (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpAssign (Token token) {
        this.token = token;
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.ASSIGNMENT;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children[0] = left;
        children[1] = right;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return children[0].getType ().getNotConst ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {

        // Special case: values = call
        if (OpCall.class.isInstance (children[1])) {
            checkTypesCall (env, resolver);
            return;
        }

        dests = new ArrayList<Expression> ();
        srcs = new ArrayList<Expression> ();
        
        // Unpack tuples
        if (OpComma.class.isInstance (children[0])) {
            ((OpComma) children[0]).unpack (dests);
        } else {
            dests.add (children[0]);
        }

        if (OpComma.class.isInstance (children[1])) {
            ((OpComma) children[1]).unpack (srcs);
        } else {
            srcs.add (children[1]);
        }

        // Check types
        for (Expression i: srcs)
            i.checkTypes (env, resolver);
        for (Expression i: dests) {
            i.checkTypes (env, resolver);
            i.checkPointer (true, token);
        }

        // Implicit cast
        int limit = (srcs.size () < dests.size ())
            ? srcs.size ()
            : dests.size ();
        for (int i = 0; i < limit; ++i) {
            srcs.set (i, (Expression) Type.coerce
                      (srcs.get (i), dests.get (i).getType (),
                       OpCast.CASTCREATOR, env));
        }

        // Symmetry
        if (srcs.size () != dests.size ()) {
            env.warning_at ("multiple assign is not symmetric; only matching "
                            + "pairs will be assigned", token);
        }
    }

    // Special checkTypes for values=call special case
    private void checkTypesCall (Env env, Resolver resolver) throws CError {
        dests = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (children[0])) {
            ((OpComma) children[0]).unpack (dests);
        } else {
            dests.add (children[0]);
        }

        // Check types
        types = new ArrayList<Type> (dests.size ());
        for (Expression i: dests) {
            i.checkTypes (env, resolver);
            if (!NullValue.class.isInstance (i))
                i.checkPointer (true, token);
            types.add (i.getType ());
        }

        ((OpCall) children[1]).checkTypesMult (env, resolver, types);
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("assignment has no address", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign ");
        children[0].print (out);
        out.print (" ");
        children[1].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {

        // Special case: values = call
        if (OpCall.class.isInstance (children[1])) {
            genLLVMCall (env, emitter, function);
            return;
        }

        int limit = (srcs.size () < dests.size ())
            ? srcs.size ()
            : dests.size ();
        String[] pointers = new String[limit];
        String[] values = new String[limit];
        for (int i = 0; i < limit; ++i) {
            srcs.get (i).genLLVM (env, emitter, function);
            values[i] = srcs.get (i).getValueString ();
        }
        valueString = values[0];
        for (int i = 0; i < limit; ++i) {
            pointers[i] = dests.get (i).getPointer (env, emitter, function);
        }
        for (int i = 0; i < limit; ++i) {
            new store (emitter, function)
                .pointer (pointers[i])
                .value (LLVMType.getLLVMName (srcs.get (i).getType ()),
                        values[i])
                .build ();
        }
    }

    private void genLLVMCall (Env env, LLVMEmitter emitter, Function function) {

        List<String> pointers = new ArrayList<String> (dests.size ());
        for (Expression i: dests) {
            if (NullValue.class.isInstance (i))
                pointers.add ("");
            else
                pointers.add (i.getPointer (env, emitter, function));
        }
        ((OpCall) children[1]).genLLVMMult (env, emitter, function, types,
                                            pointers);

    }

    @SuppressWarnings("unchecked") // :-( I'm sorry.
    public List<AST> getChildren () {
        // Oh FFS Java, why can't List<Expression> sub in for List<AST>? :-(
        // (I suppose they probably have some important reason, which I should
        //  look into for designing my own subtype assignment semantics)
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAssign (env, stream, method);
                }
            };
    }
}
