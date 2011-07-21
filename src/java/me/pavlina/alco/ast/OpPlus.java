// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.Arrays;

/**
 * Addition operator.
 */
public class OpPlus extends Expression.Operator {
    Token token;
    Expression[] children;
    Expression pointer;
    Expression integer;
    AddNum addnum;
    AddPtr addptr;
    Coerce coerce;
    Type type;
    String valueString;

    public static Expression.OperatorCreator CREATOR;


    public OpPlus (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpPlus (Token token) {
        this.token = token;
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.ADD;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children[0] = left;
        children[1] = right;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
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
            pointer = children[1];
            integer = children[0];
            if (lhsE != Type.Encoding.SINT && lhsE != Type.Encoding.UINT)
                throw CError.at ("invalid types for addition", token);
        }


        if (pointer != null) {
            addptr = new AddPtr (token)
                .pointerT (pointer.getType ())
                .integerT (integer.getType ());
            addptr.checkTypes (env, resolver);
            type = addptr.getType ();
        } else {
            coerce = new Coerce (token)
                .lhsT (children[0].getType ())
                .rhsT (children[1].getType ());
            coerce.checkTypes (env, resolver);
            type = coerce.getType ();
            addnum = new AddNum (token)
                .type (type);
            addnum.checkTypes (env, resolver);
        }
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);

        if (addptr != null) {
            addptr.pointerV (pointer.getValueString ());
            addptr.integerV (integer.getValueString ());
            addptr.genLLVM (env, emitter, function);
            valueString = addptr.getValueString ();

        } else {
            coerce.lhsV (children[0].getValueString ());
            coerce.rhsV (children[1].getValueString ());
            coerce.genLLVM (env, emitter, function);
            addnum.lhs (coerce.getValueStringL ());
            addnum.rhs (coerce.getValueStringR ());
            addnum.genLLVM (env, emitter, function);
            valueString = addnum.getValueString ();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        if (children == null)
            return null;
        else
            return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(add");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to addition", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpPlus (env, stream, method);
                }
            };
    }
}
