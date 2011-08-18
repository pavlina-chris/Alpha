// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Index operator. This does not parse, as it is always explicitly created from
 * a known name. */
public class OpIndex extends Expression.Operator {
    Token token;
    Expression[] children;
    List<Expression> args;
    Instruction instruction;

    public OpIndex (Token token, Expression expr, Method method) {
        this.token = token;
        children = new Expression[] { expr, null };
        expr.setParent (this);
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.INDEX;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public void setOperands (Expression op, Expression ignore) {
        children[1] = op;
        op.setParent (this);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public void checkPointer (boolean write, Token token) throws CError {
        if ((children[0].getType ().getSubtype ().isConst () ||
             children[0].getType ().isConst ()) && write)
            throw CError.at ("cannot assign to constant", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        // Array:
        // +--------+---------+      +---+---+---+---+---+
        // | LENGTH | POINTER |  ->  |   |   |   |   |   |
        // +--------+---------+      +---+---+---+---+---+
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        String size = "i" + env.getBits ();
        String sizep = size + "*";
        String elemp = LLVMType.getLLVMName
            (children[0].getType ().getSubtype ()) + "*";
        Instruction arr = children[0].getInstruction ();
        Instruction bc1 = new CONVERT ()
            .op ("bitcast").stype ("i8*").dtype (sizep).value (arr);
        Instruction ptrField = new GETELEMENTPTR ()
            .type (sizep).rtype (sizep).addIndex (1).value (bc1);
        Instruction ptr_ = new LOAD ()
            .type (size).pointer (ptrField);
        Instruction ptr = new CONVERT ()
            .op ("inttoptr").stype (size).dtype (elemp).value (ptr_);
        Instruction n = new GETELEMENTPTR ()
            .type (elemp).rtype (elemp).addIndex (children[1].getInstruction ())
            .value (ptr);
        function.add (bc1);
        function.add (ptrField);
        function.add (ptr_);
        function.add (ptr);
        function.add (n);
        return n;
    }

    public Type getType () {
        return children[0].getType ().getSubtype ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (children[0].getType ().getEncoding () != Type.Encoding.ARRAY) {
            throw Unexpected.at ("array", children[0].getToken ());
        }
        args = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (children[1])) {
            ((OpComma) children[1]).unpack (args);
        } else {
            args.add ((Expression) children[1]);
        }
        for (Expression i: args)
            i.checkTypes (env, resolver);
        if (args.size () != 1) {
            throw Unexpected.at ("single argument", token);
        }
        Type.checkCoerce (args.get (0), new Type (env, "size", null), token);
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        Instruction ptr = this.getPointer (env, emitter, function);
        Instruction val = new LOAD ()
            .type (LLVMType.getLLVMName (children[0].getType ().getSubtype ()))
            .pointer (ptr);
        function.add (val);
        instruction = val;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(index");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }
}
