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
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Multiplication assignment operator (*=). */
public class OpAssignMul extends Expression.Operator {
    Token token;
    Method method;
    Type type;
    Expression[] children;
    Expression pointer, integer;
    String valueString;
    Overload overload;
    Cast cast;
    MulNum mulnum;

    public static Expression.OperatorCreator CREATOR;

    public OpAssignMul (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
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

    public void setOperands (Expression dest, Expression value) {
        children[0] = dest;
        children[1] = value;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (true, token);
        children[1].checkTypes (env, resolver);

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            mulnum = null;
            overload = new Overload (token, method);
            OpAddress addrOf = new OpAddress (token, children[0]);
            addrOf.checkTypes (env, resolver);
            overload.operator ("*=").children (addrOf, children[1]);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        Type.checkCoerce (children[1], children[0], token);
        cast = new Cast (token)
            .type (children[1].getType ())
            .dest (children[0].getType ());
        cast.checkTypes (env, resolver);
        mulnum = new MulNum (token)
            .type (children[0].getType ());
        mulnum.checkTypes (env, resolver);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);

        if (mulnum != null) {
            String ptr = children[0].getPointer (env, emitter, function);
        
            cast.value (children[1].getValueString ());
            cast.genLLVM (env, emitter, function);
            mulnum.lhs (children[0].getValueString ());
            mulnum.rhs (cast.getValueString ());
            mulnum.genLLVM (env, emitter, function);
            valueString = mulnum.getValueString ();
            new store (emitter, function)
                .pointer (ptr)
                .value (LLVMType.getLLVMName (children[0].getType ()),
                        valueString)
                .build ();

        } else {
            overload.genLLVM (env, emitter, function);
            valueString = overload.getValueString ();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign-mult");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to assignment", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAssignMul (env, stream, method);
                }
            };
    }
}
