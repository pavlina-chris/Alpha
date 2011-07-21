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
 * Subtraction operator.
 */
public class OpMinus extends Expression.Operator {
    Token token;
    Expression[] children;
    boolean pointerSub;
    Type type;
    String valueString;
    int castSide;

    public static Expression.OperatorCreator CREATOR;

    public OpMinus (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public OpMinus (Token token) {
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

        // Check for pointer subtraction
        if (children[0].getType ().getEncoding () == Type.Encoding.POINTER
            && children[1].getType ().getEncoding () == Type.Encoding.POINTER) {
            if (!children[0].getType ().getSubtype ().equalsNoConst
                (children[1].getType ().getSubtype ())) {
                throw CError.at ("subtraction of pointers: must be same type",
                                 token);
            }
            type = new Type (env, "ssize", null);
            pointerSub = true;
            return;
        }

        castSide = Type.arithCoerce (children[0], children[1], token);
        type = children[castSide == 1 ? 0 : 1].getType ().getNormalised ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (pointerSub) genLLVM_pointerSub (env, emitter, function);
        else genLLVM_normal (env, emitter, function);
    }

    private void genLLVM_pointerSub (Env env, LLVMEmitter emitter,
                                     Function function)
    {
        // Subtract the pointers, then divide by the pointer width
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String lhsV = children[0].getValueString ();
        String rhsV = children[1].getValueString ();
        Type lhsT = children[0].getType ();
        Type rhsT = children[1].getType ();
        String intermedT = "i" + Integer.toString (env.getBits ());

        String lhsAsInt = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.PTRTOINT)
            .source (LLVMType.getLLVMName (lhsT), lhsV)
            .dest (intermedT)
            .build ();

        String rhsAsInt = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.PTRTOINT)
            .source (LLVMType.getLLVMName (rhsT), rhsV)
            .dest (intermedT)
            .build ();

        String difference = new Binary (emitter, function)
            .operation (Binary.BinOp.SUB)
            .type (intermedT)
            .operands (lhsAsInt, rhsAsInt)
            .build ();

        valueString = new Binary (emitter, function)
            .operation (Binary.BinOp.SDIV)
            .type (intermedT)
            .operands (difference,
                       Integer.toString
                       (children[0].getType ().getSubtype ().getSize ()))
            .build ();
    }

    private void genLLVM_normal (Env env, LLVMEmitter emitter,
                                 Function function)
    {
        Binary.BinOp operation;
        Type.Encoding enc = children[0].getType ().getEncoding ();
        if (enc == Type.Encoding.FLOAT)
            operation = Binary.BinOp.FSUB;
        else if (enc == Type.Encoding.SINT || enc == Type.Encoding.UINT)
            operation = Binary.BinOp.SUB;
        else
            throw new RuntimeException ("Subtracting unsupported items");

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
        out.print ("(subt");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to subtraction", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpMinus (env, stream, method);
                }
            };
    }
}
