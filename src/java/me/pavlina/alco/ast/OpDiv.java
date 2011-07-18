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
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.Arrays;

/**
 * Division operator.
 */
public class OpDiv extends Expression.Operator {
    Token token;
    Expression[] children;
    Type type;
    String valueString;
    int castSide; // -1, 0, 1

    public static Expression.OperatorCreator CREATOR;


    public OpDiv (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpDiv (Token token) {
        this.token = token;
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.MUL;
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

        castSide = Type.arithCoerce (children[0], children[1], token);
        if (castSide == 1)
            type = children[0].getType ().getNotConst ();
        else
            type = children[1].getType ().getNotConst ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function)
    {
        Binary.BinOp operation;
        Type.Encoding enc = children[0].getType ().getEncoding ();
        if (enc == Type.Encoding.FLOAT)
            operation = Binary.BinOp.FDIV;
        else if (enc == Type.Encoding.SINT)
            operation = Binary.BinOp.SDIV;
        else if (enc == Type.Encoding.UINT)
            operation = Binary.BinOp.UDIV;
        else
            throw new RuntimeException ("Dividing unsupported items");

        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String lhs = children[0].getValueString ();
        String rhs = children[1].getValueString ();

        if (castSide == 1) {
            Cast c = new Cast (token)
                .value (rhs).type (children[1].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            rhs = c.getValueString ();
        } else if (castSide == -1) {
            Cast c = new Cast (token)
                .value (lhs).type (children[0].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            lhs = c.getValueString ();
        }

        valueString = new Binary (emitter, function)
            .operation (operation)
            .type (LLVMType.getLLVMName (children[0].getType ()))
            .operands (lhs, rhs)
            .build ();
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
        out.print ("(div ");
        children[0].print (out);
        out.print (" ");
        children[1].print (out);
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to division", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpDiv (env, stream, method);
                }
            };
    }
}
