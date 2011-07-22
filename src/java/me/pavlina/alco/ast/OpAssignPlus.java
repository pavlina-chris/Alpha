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
 * Addition assignment operator (+=). */
public class OpAssignPlus extends Expression.Operator {
    Token token;
    Expression[] children;
    Expression pointer, integer;
    String valueString;
    Cast cast;
    AddNum addnum;
    AddPtr addptr;

    public static Expression.OperatorCreator CREATOR;

    public OpAssignPlus (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
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

    public void setOperands (Expression dest, Expression value) {
        children[0] = dest;
        children[1] = value;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return children[0].getType ().getNormalised ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (true, token);
        children[1].checkTypes (env, resolver);

        Type.Encoding lhsE = children[0].getType ().getEncoding ();
        Type.Encoding rhsE = children[1].getType ().getEncoding ();

        // Try pointer+int first
        if (lhsE == Type.Encoding.POINTER) {
            pointer = children[0];
            integer = children[1];
            if (rhsE != Type.Encoding.SINT && rhsE != Type.Encoding.UINT)
                throw CError.at ("invalid types for addition", token);
        } else if (rhsE == Type.Encoding.POINTER) {
            throw CError.at ("cannot assign-add int+ptr (changes type)", token);
        }

        if (pointer != null) {
            addptr = new AddPtr (token)
                .pointerT (pointer.getType ())
                .integerT (integer.getType ());
            addptr.checkTypes (env, resolver);
        } else {
            Type.checkCoerce (children[1], children[0], token);
            cast = new Cast (token)
                .type (children[1].getType ())
                .dest (children[0].getType ());
            cast.checkTypes (env, resolver);
            addnum = new AddNum (token)
                .type (children[0].getType ());
            addnum.checkTypes (env, resolver);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String ptr = children[0].getPointer (env, emitter, function);
        
        if (addptr != null) {
            addptr.pointerV (children[0].getValueString ());
            addptr.integerV (children[1].getValueString ());
            addptr.genLLVM (env, emitter, function);
            valueString = addptr.getValueString ();
        } else {
            cast.value (children[1].getValueString ());
            cast.genLLVM (env, emitter, function);
            addnum.lhs (children[0].getValueString ());
            addnum.rhs (cast.getValueString ());
            addnum.genLLVM (env, emitter, function);
            valueString = addnum.getValueString ();
        }
        new store (emitter, function)
            .pointer (ptr)
            .value (LLVMType.getLLVMName (children[0].getType ()),
                    valueString)
            .build ();
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign-add");
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
                    return new OpAssignPlus (env, stream, method);
                }
            };
    }
}
