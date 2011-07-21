// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.Arrays;

/**
 * Negation operator. */
public class OpNeg extends Expression.Operator {
    private Token token;
    private Expression[] children;
    private String valueString;

    public static Expression.OperatorCreator CREATOR;

    public OpNeg (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[1];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.NEG;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children[0] = op;
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return children[0].getType ().getNormalised ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        Type.Encoding e = children[0].getType ().getEncoding ();
        if (e == Type.Encoding.UINT)
            throw CError.at ("negation of unsigned integer", token);
        if (e != Type.Encoding.SINT && e != Type.Encoding.UINT &&
            e != Type.Encoding.FLOAT) {
            throw CError.at ("invalid type for negation", token);
        }
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to negation", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(negate ");
        children[0].print (out);
        out.print (")");
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        String val = children[0].getValueString ();
        Type.Encoding e = children[0].getType ().getEncoding ();
        if (e == Type.Encoding.FLOAT) {
            valueString = new Binary (emitter, function)
                .operation (Binary.BinOp.FSUB)
                .type (LLVMType.getLLVMName (getType ()))
                .operands ("0.0", val)
                .build ();
        } else {
            valueString = new Binary (emitter, function)
                .operation (Binary.BinOp.SUB)
                .type (LLVMType.getLLVMName (getType ()))
                .operands ("0", val)
                .build ();
        }
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
                    return new OpNeg (env, stream, method);
                }
            };
    }
}
