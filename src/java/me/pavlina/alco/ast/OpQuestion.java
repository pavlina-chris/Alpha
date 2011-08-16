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
    Instruction instruction;
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
        left.setParent (this);
        right.setParent (this);
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

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function)
    {
        Block LlhsEval = new Block ();
        Block Llhs = new Block ();
        Block LrhsEval = new Block ();
        Block Lrhs = new Block ();
        Block Lout = new Block ();

        children[0].genLLVM (env, emitter, function);
        cast.value (children[0].getInstruction ());
        cast.genLLVM (env, emitter, function);
        Instruction cond = cast.getInstruction ();

        function.add (new SWITCH ().value (cond).dest (LlhsEval)
                  .addDest ("0", LrhsEval));

        function.add (LlhsEval);
        children[1].genLLVM (env, emitter, function);
        Instruction lhs = children[1].getInstruction ();
        if (coerceSide == -1) {
            Cast c = new Cast (token)
                .value (lhs).type (children[1].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            lhs = c.getInstruction ();
        }
        function.add (new BRANCH ().dest (Llhs));

        function.add (Llhs);
        function.add (new BRANCH ().dest (Lout));

        function.add (LrhsEval);
        children[2].genLLVM (env, emitter, function);
        Instruction rhs = children[2].getInstruction ();
        if (coerceSide == 1) {
            Cast c = new Cast (token)
                .value (rhs).type (children[2].getType ()).dest (type);
            c.genLLVM (env, emitter, function);
            rhs = c.getInstruction ();
        }
        function.add (new BRANCH ().dest (Lrhs));
        
        function.add (Lrhs);
        function.add (new BRANCH ().dest (Lout));

        function.add (Lout);
        instruction = new PHI ().type (LLVMType.getLLVMName (type))
            .pairs (lhs, Llhs, rhs, Lrhs);
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

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
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
