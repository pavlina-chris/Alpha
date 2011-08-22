// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.codegen.IndexArray;
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
    IndexArray indexarray;

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
        children[0].setParent (this);
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
        if (indexarray != null) {
            children[0].genLLVM (env, emitter, function);
            children[1].genLLVM (env, emitter, function);
            indexarray.linst (children[0].getInstruction ());
            indexarray.rinst (children[1].getInstruction ());
            return indexarray.getPointer (env, emitter, function);
        }
        return null;
    }

    public Type getType () {
        return children[0].getType ().getSubtype ();
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (children[0].getType ().getEncoding () == Type.Encoding.ARRAY) {
            if (OpComma.class.isInstance (children[1]))
                throw Unexpected.at ("single argument", token);
            children[1].checkTypes (env, resolver);
            indexarray = new IndexArray (token);
            indexarray.ltype (children[0].getType ());
            indexarray.rtype (children[1].getType ());
            indexarray.checkTypes (env, resolver);
        } else
            throw Unexpected.at ("array", token);
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (indexarray != null) {
            children[0].genLLVM (env, emitter, function);
            children[1].genLLVM (env, emitter, function);
            indexarray.linst (children[0].getInstruction ());
            indexarray.rinst (children[1].getInstruction ());
            indexarray.genLLVM (env, emitter, function);
            instruction = indexarray.getInstruction ();
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
        out.print ("(index");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }
}
