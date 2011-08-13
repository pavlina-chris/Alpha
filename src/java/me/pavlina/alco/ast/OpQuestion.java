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
 * Question operator. This is the left part of the ?: ternary operator. */
public class OpQuestion extends Expression.Operator {
    Token token;
    Expression[] children;
    Type type;
    String valueString;
    Cast cast;
    int coerceSide;

    public static final Expression.OperatorCreator CREATOR;

    public OpQuestion (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.QUESTION;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children = new Expression[] {left, right};
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        if (!OpColon.class.isInstance (children[1])) {
            throw Unexpected.at (":", token);
        }
        List<AST> results = children[1].getChildren ();
        children = new Expression[] {
            children[0],
            (Expression) results.get (0),
            (Expression) results.get (1) };

        for (Expression i: children)
            i.checkTypes (env, resolver);

        cast = new Cast (token)
            .type (children[0].getType ())
            .dest (new Type (env, "bool", null));
        cast.checkTypes (env, resolver);

        coerceSide = Type.arithCoerce
            (children[1].getType (), children[2].getType (), token);
        type = children[coerceSide == 1 ? 1 : 2].getType ().getNormalised ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function)
    {
        String LlhsEval = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Llhs = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String LrhsEval = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lrhs = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lout = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));

        children[0].genLLVM (env, emitter, function);
        cast.value (children[0].getValueString ());
        cast.genLLVM (env, emitter, function);
        String cond = cast.getValueString ();

        new _switch (emitter, function).value ("i8", cond)
            .dest ("%" + LlhsEval).addDest ("0", "%" + LrhsEval)
            .build ();

        new label (emitter, function).name (LlhsEval).build ();
        children[1].genLLVM (env, emitter, function);
        String lhs = children[1].getValueString ();
        if (coerceSide == -1) {
            Cast c = new Cast (token)
                .value (lhs).type (children[1].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            lhs = c.getValueString ();
        }
        new branch (emitter, function).ifTrue ("%" + Llhs).build ();

        new label (emitter, function).name (Llhs).build ();
        new branch (emitter, function).ifTrue ("%" + Lout).build ();

        new label (emitter, function).name (LrhsEval).build ();
        children[2].genLLVM (env, emitter, function);
        String rhs = children[2].getValueString ();
        if (coerceSide == 1) {
            Cast c = new Cast (token)
                .value (rhs).type (children[2].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            rhs = c.getValueString ();
        }
        new branch (emitter, function).ifTrue ("%" + Lrhs).build ();
        
        new label (emitter, function).name (Lrhs).build ();
        new branch (emitter, function).ifTrue ("%" + Lout).build ();

        new label (emitter, function).name (Lout).build ();
        valueString = new phi (emitter, function)
            .type (LLVMType.getLLVMName (type))
            .pairs (lhs, "%" + Llhs, rhs, "%" + Lrhs).build ();
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
        out.print ("(conditional ");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to conditional", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpQuestion (env, stream, method);
                }
            };
    }
}
