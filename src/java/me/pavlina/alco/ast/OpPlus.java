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
 * Addition operator.
 */
public class OpPlus extends Expression.Operator {
    Token token;
    Expression[] children;
    Expression pointer;
    Expression integer;
    boolean pointerAdd;
    Type type;
    String valueString;
    int castSide; // -1, 0, 1

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

        // Try checking for addition of (pointer + int) first
        boolean foundPointer = false;
        int foundInt = -1; // Index at which int was found
        for (int i = 0; i < 2; ++i) {
            if (children[i].getType ().getEncoding () ==
                Type.Encoding.POINTER) {
                foundPointer = true;
                pointer = children[i];

            } else if (children[i].getType ().getEncoding () ==
                     Type.Encoding.UINT) {
                if (children[i].getType ().getSize () > (env.getBits () / 8))
                    throw CError.at
                        ("cannot add pointer to wider integer", token);
                integer = children[i];
                foundInt = i;

            } else if (children[i].getType ().getEncoding () ==
                       Type.Encoding.SINT) {
                if (children[i].getType ().getSize () > (env.getBits () / 8))
                    throw CError.at
                        ("cannot add pointer to wider integer", token);
                integer = children[i];
                foundInt = i;
            }
        }
        pointerAdd = (foundPointer && (foundInt != 0));
        if (pointerAdd) {
            String ty =
                (integer.getType ().getEncoding () == Type.Encoding.UINT)
                ? "size" : "ssize";
            Type.checkCoerce (children[foundInt], new Type (env, ty, null),
                              token);
            type = pointer.getType ().getNotConst ();
            return;
        }

        castSide = Type.arithCoerce (children[0], children[1], token);
        type = children[castSide == 1 ? 0 : 1].getType ().getNotConst ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (pointerAdd) genLLVM_pointerAdd (env, emitter, function);
        else genLLVM_normal (env, emitter, function);
    }

    private void genLLVM_pointerAdd (Env env, LLVMEmitter emitter,
                                     Function function)
    {
        // Multiply the integer by the pointer width, then add it
        pointer.genLLVM (env, emitter, function);
        integer.genLLVM (env, emitter, function);
        String ptrV = pointer.getValueString ();
        String t_ =
            (integer.getType ().getEncoding () == Type.Encoding.UINT)
            ? "size" : "ssize";
        Type t = new Type (env, t_, null);
        Cast c = new Cast (token)
            .value (integer.getValueString ()).type (integer.getType ())
            .dest (t);
        c.genLLVM (env, emitter, function);
        String intV = c.getValueString ();

        String ptrAsInt = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.PTRTOINT)
            .source (LLVMType.getLLVMName (pointer.getType ()), ptrV)
            .dest (LLVMType.getLLVMName (t))
            .build ();
        String intByWidth = new Binary (emitter, function)
            .operation (Binary.BinOp.MUL)
            .type (LLVMType.getLLVMName (t))
            .operands (intV,
                       Integer.toString
                       (pointer.getType ().getSubtype ().getSize ()))
            .build ();
        String newPtrInt = new Binary (emitter, function)
            .operation (Binary.BinOp.ADD)
            .type (LLVMType.getLLVMName (t))
            .operands (intByWidth, ptrAsInt)
            .build ();
        valueString = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.INTTOPTR)
            .source (LLVMType.getLLVMName (t), newPtrInt)
            .dest (LLVMType.getLLVMName (pointer.getType ()))
            .build ();
    }

    private void genLLVM_normal (Env env, LLVMEmitter emitter,
                                 Function function)
    {
        Binary.BinOp operation;
        Type.Encoding enc = children[0].getType ().getEncoding ();
        if (enc == Type.Encoding.FLOAT)
            operation = Binary.BinOp.FADD;
        else if (enc == Type.Encoding.SINT || enc == Type.Encoding.UINT)
            operation = Binary.BinOp.ADD;
        else
            throw new RuntimeException ("Adding unsupported items");

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
