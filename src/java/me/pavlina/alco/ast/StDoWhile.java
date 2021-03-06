// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import me.pavlina.alco.parse.ExpressionParser;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Do/while loop. Syntax:
 * do scope while (expression) ;
 */
public class StDoWhile extends Loop
{
    Token token;
    Block condLabel, botLabel;
    AST[] values; // {condition, body}

    public StDoWhile (Env env, TokenStream stream, Method method) throws CError
    {
        token = stream.next ();
        assert token.is (Token.WORD, "do");
        
        values = new AST[2];
        
        // Body
        if (stream.peek ().is (Token.NO_MORE))
            throw UnexpectedEOF.after ("body", stream.last ());
        values[1] = new Scope (env, stream, method);
        values[1].setParent (this);

        // Condition
        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after ("while", stream.last ());
        else if (!temp.is (Token.WORD, "while"))
            throw Unexpected.after ("while", stream.last ());
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after ("(", stream.last ());
        else if (!temp.is (Token.OPER, "("))
            throw Unexpected.after ("(", stream.last ());
        values[0] = (AST) ExpressionParser.parse (env, stream, method, ")");
        if (values[0] == null)
            throw Unexpected.after ("expression", temp);
        values[0].setParent (this);
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (")", stream.last ());
        else if (!temp.is (Token.OPER, ")"))
            throw Unexpected.after (")", stream.last ());

        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";"))
            throw Unexpected.after (";", stream.last ());
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return Arrays.asList (values);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        for (AST i: values)
            i.checkTypes (env, resolver);
        Type.checkCoerce ((Expression) values[0],
                          new Type (env, "bool", null), token);
    }

    public Block getContinueLabel () {
        return condLabel;
    }

    public Block getBottomLabel () {
        return botLabel;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // br label %.Lbody
        // .Lbody
        // --body--
        // br label %.Lcond
        // .Lcond:
        // %.cond = <cond>
        // %cond = icmp ne i8 %.cond, 0
        // br i1 %cond, label %.Lbody, label %.Lbot
        // .Lbot:

        Block Lbody = new Block ();
        Block Lcond = new Block ();
        Block Lbot = new Block ();
        condLabel = Lcond;
        botLabel = Lbot;

        function.add (new COMMENT ().text ("DO-WHILE, line " + (1+token.line)));

        function.add (new BRANCH ().dest (Lbody));
        function.add (Lbody);
        values[1].genLLVM (env, emitter, function);
        function.add (new BRANCH ().dest (Lcond));
        function.add (Lcond);
        values[0].genLLVM (env, emitter, function);
        Instruction bCond = ((Expression) values[0]).getInstruction ();
        Cast c = new Cast (token)
            .value (bCond).type (((Expression) values[0]).getType ())
            .dest (new Type (env, "bool", null));
        c.genLLVM (env, emitter, function);

        Instruction cond = new BINARY ()
            .op ("icmp ne").type ("i8").lhs (c.getInstruction ()).rhs ("0");
        function.add (cond);

        function.add (new BRANCH ().cond (cond).T (Lbody).F (Lbot));
        function.add (Lbot);
    }

    public void print (java.io.PrintStream out) {
        out.print ("(do-while ");
        values[0].print (out);
        values[1].print (out);
        out.println (")");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method) throws CError
                {
                    return new StDoWhile (env, stream, method);
                }
            };
    }
}
