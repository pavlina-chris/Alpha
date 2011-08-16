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
    Instruction instruction;
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
        left.setParent (this);
        right.setParent (this);
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

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
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

        Block Lbegin = new Block ();
        Block LrhsEval = new Block ();
        Block Lrhs = new Block ();
        Block Lout = new Block ();

        children[0].genLLVM (env, emitter, function);
        castL.value (children[0].getInstruction ());
        castL.genLLVM (env, emitter, function);
        Instruction L = castL.getInstruction ();

        function.add (new BRANCH ().dest (Lbegin));
        function.add (Lbegin);
        function.add (new SWITCH ().value (L).dest (Lout)
                      .addDest ("0", LrhsEval));
        function.add (LrhsEval);
        
        children[1].genLLVM (env, emitter, function);
        castR.value (children[1].getInstruction ());
        castR.genLLVM (env, emitter, function);
        Instruction R = castR.getInstruction ();

        function.add (new BRANCH ().dest (Lrhs));
        function.add (Lrhs);
        function.add (new BRANCH ().dest (Lout));
        function.add (Lout);
        instruction = new PHI ().type ("i8")
            .pairs ("-1", Lbegin, R, Lrhs);
        function.add (instruction);
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

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
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
