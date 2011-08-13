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
 * Logical OR operator.
 */
public class OpLOr extends Expression.Operator {
    Token token;
    Expression[] children;
    Type type;
    String valueString;
    Cast castL, castR;

    public static final Expression.OperatorCreator CREATOR;

    public OpLOr (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.LOG_OR;
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

        type = new Type (env, "bool", null);
        Type.checkCoerce (children[0], type, token);
        Type.checkCoerce (children[1], type, token);
        castL = new Cast (token).type (children[0].getType ()).dest (type);
        castR = new Cast (token).type (children[1].getType ()).dest (type);
        castL.checkTypes (env, resolver);
        castR.checkTypes (env, resolver);
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        // %L = <lhs as i8>
        // br %.Lbegin
        // .Lbegin:
        // switch i8 %L, label %.Lout [ i8 0, label %.LrhsEval ]
        // .LrhsEval:
        // %R = <rhs as i8>
        // br %.Lrhs
        // .Lrhs
        // br label %.Lout
        // .Lout
        // result = phi i8 [ -1, %.Lbegin ], [ %R, %.Lrhs ]

        String Lbegin = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String LrhsEval = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lrhs = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lout = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));

        children[0].genLLVM (env, emitter, function);
        castL.value (children[0].getValueString ());
        castL.genLLVM (env, emitter, function);
        String L = castL.getValueString ();

        new branch (emitter, function).ifTrue ("%" + Lbegin).build ();
        new label (emitter, function).name (Lbegin).build ();
        new _switch (emitter, function)
            .value ("i8", L).dest ("%" + Lout)
            .addDest ("0", "%" + LrhsEval).build ();
        new label (emitter, function).name (LrhsEval).build ();
        
        children[1].genLLVM (env, emitter, function);
        castR.value (children[1].getValueString ());
        castR.genLLVM (env, emitter, function);
        String R = castR.getValueString ();

        new branch (emitter, function).ifTrue ("%" + Lrhs).build ();
        new label (emitter, function).name (Lrhs).build ();
        new branch (emitter, function).ifTrue ("%" + Lout).build ();
        new label (emitter, function).name (Lout).build ();
        valueString = new phi (emitter, function)
            .type ("i8").pairs ("-1", "%" + Lbegin,
                                R, "%" + Lrhs).build ();
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
        out.print ("(log-or");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to logical operation", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpLOr (env, stream, method);
                }
            };
    }
}
