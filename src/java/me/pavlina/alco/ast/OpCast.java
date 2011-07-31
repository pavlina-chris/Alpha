// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.Arrays;
import java.math.BigInteger;

/**
 * Cast operator. */
public class OpCast extends Expression.Operator {
    private Token token;
    private Expression[] children;
    private String valueString;
    Cast cast;

    public static Expression.OperatorCreator CREATOR;

    public OpCast (Env env, TokenStream stream, Method method) throws CError {
        children = new Expression[2];
        token = stream.next ();
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.CAST;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public Type getType () {
        return children[1].getType ();
    }

    public void setOperands (Expression value, Expression type) {
        children[0] = value;
        children[1] = type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (!TypeValue.class.isInstance (children[1])) {
            throw Unexpected.at ("type", children[1].getToken ());
        }
        cast = new Cast (token)
            .type (children[0].getType ())
            .dest (children[1].getType ());
        cast.checkTypes (env, resolver);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        cast.value (children[0].getValueString ());
        cast.genLLVM (env, emitter, function);
    }

    public String getValueString () {
        return cast.getValueString ();
    }

    public void checkPointer (boolean write, Token token) throws CError {
        // If we are casting off const, lie to checkPointer about intent to
        // read
        if (!children[1].getType ().isConst () &&
            children[0].getType ().isConst ()) {
            children[0].checkPointer (false, token);
        } else {
            children[0].checkPointer (write, token);
        }
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        // Casting a pointer is just a bit-cast.
        String chPtr = children[0].getPointer (env, emitter, function);
        String ptr = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.BITCAST)
            .source (LLVMType.getLLVMName (children[0].getType ()) + "*",
                     chPtr)
            .dest (LLVMType.getLLVMName (children[1].getType ()) + "*")
            .build ();
        return ptr;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(cast ");
        out.print (children[1].getType ().toString ());
        out.print (" ");
        children[0].print (out);
        out.print (")");
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpCast (env, stream, method);
                }
            };
    }
}
