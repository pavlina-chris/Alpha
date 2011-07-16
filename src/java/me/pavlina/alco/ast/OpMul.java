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
import java.util.List;
import java.util.Arrays;

/**
 * Multiplication operator.
 */
public class OpMul extends Expression.Operator {
    Token token;
    Expression[] children;
    Type type;
    String valueString;

    public static Expression.OperatorCreator CREATOR;


    public OpMul (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpMul (Token token) {
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

        // Coercion: rank types by this list (see
        // Standard:Types:Casting:Coercion)
        int[] ranks = new int[2];
        for (int i = 0; i < 2; ++i) {
            Type t = children[i].getType ();
            Type.Encoding enc = t.getEncoding ();
            int size = t.getSize ();
            if (enc == Type.Encoding.FLOAT) {
                if (size == 4)      ranks[i] = 1;
                else                ranks[i] = 2;
            } else if (enc == Type.Encoding.UINT) {
                if (size == 8)      ranks[i] = 3;
                else if (size == 4) ranks[i] = 4;
                else if (size == 2) ranks[i] = 5;
                else                ranks[i] = 6;
            } else if (enc == Type.Encoding.SINT) {
                if (size == 8)      ranks[i] = 7;
                else if (size == 4) ranks[i] = 8;
                else if (size == 2) ranks[i] = 9;
                else                ranks[i] = 10;
            } else {
                throw CError.at ("invalid type for multiplication",
                                 children[i].getToken ());
            }
        }

        if (ranks[0] < ranks[1]) {
            // Coerce rhs to lhs
            children[1] = (Expression) Type.coerce
                (children[1], children[0].getType (),
                 OpCast.CASTCREATOR, env);

        } else if (ranks[1] < ranks[0]) {
            // Coerce lhs to rhs
            children[0] = (Expression) Type.coerce
                (children[0], children[1].getType (),
                 OpCast.CASTCREATOR, env);

        }
        // else: no coercion required

        type = children[0].getType ().getNotConst ();
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
            operation = Binary.BinOp.FMUL;
        else if (enc == Type.Encoding.SINT || enc == Type.Encoding.UINT)
            operation = Binary.BinOp.MUL;
        else
            throw new RuntimeException ("Multiplying unsupported items");

        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String lhs = children[0].getValueString ();
        String rhs = children[1].getValueString ();

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
        out.print ("(mult");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to multiplication", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream)
                    throws CError {
                    return new OpMul (env, stream);
                }
            };
    }
}
