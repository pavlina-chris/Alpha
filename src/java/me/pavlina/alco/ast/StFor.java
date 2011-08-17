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
 * For loop. Syntax:
 * for (expression; expression; expression) scope
 * for (StLet expression; expression) scope
 */
public class StFor extends Loop
{
    Token token;
    Block contLabel, botLabel;
    AST[] values; // {initialiser, condition, incrementer, body}

    public StFor (Env env, TokenStream stream, Method method) throws CError
    {
        token = stream.next ();
        if (!token.is (Token.WORD, "for"))
            throw new RuntimeException ("StFor instantiated without kwd");
        
        values = new AST[4];
        
        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after ("(", stream.last ());
        else if (!temp.is (Token.OPER, "("))
            throw Unexpected.after ("(", stream.last ());
        
        Scope scope = new Scope (token);
        values[3] = scope;
        scope.setParent (this);

        temp = stream.peek ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after ("expression or let", stream.last ());
        else if (temp.is (Token.WORD, "let")) {
            values[0] = new StLet (env, stream, method);
        } else {
            // Allow null
            values[0] = ExpressionParser.parse (env, stream, method, ";");
            temp = stream.next ();
            if (temp.is (Token.NO_MORE))
                throw UnexpectedEOF.after (";", stream.last ());
            else if (!temp.is (Token.OPER, ";"))
                throw Unexpected.after (";", stream.last ());
        }
        if (values[0] != null) {
            scope.add (values[0], false);
            values[0].setParent (scope);
        }
        

        values[1] = ExpressionParser.parse (env, stream, method, ";");        
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";"))
            throw Unexpected.after (";", stream.last ());
        if (values[1] != null) {
            scope.add (values[1], false);
            values[1].setParent (scope);
        }

        values[2] = ExpressionParser.parse (env, stream, method, ")");
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (")", stream.last ());
        else if (!temp.is (Token.OPER, ")"))
            throw UnexpectedEOF.after (")", stream.last ());
        if (values[2] != null) {
            scope.add (values[2], false);
            values[2].setParent (scope);
        }

        scope.parse (env, stream, method);
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return Arrays.asList (values);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        values[3].checkTypes (env, resolver);
        if (values[1] != null)
            Type.checkCoerce ((Expression) values[1],
                              new Type (env, "bool", null), token);
    }

    public Block getContinueLabel () {
        return contLabel;
    }

    public Block getBottomLabel () {
        return botLabel;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // --initialiser--
        // br label %.Lcond
        // .Lcond:
        // %.cond = <cond>
        // %cond = icmp ne i8 %.cond, 0
        // br i1 %cond, label %.Lbody, label %.Lbot
        // .Lbody:
        // --body--
        // br label %.Lcont
        // .Lcont:
        // --increment--
        // br label %.Lcond
        // .Lbot

        Block Lcond = new Block ();
        Block Lbody = new Block ();
        Block Lcont = new Block ();
        Block Lbot = new Block ();
        contLabel = Lcont;
        botLabel = Lbot;

        function.add (new COMMENT ().text ("FOR, line " + (1+token.line)));
        if (values[0] != null)
            values[0].genLLVM (env, emitter, function);
        function.add (new BRANCH ().dest (Lcond));

        function.add (Lcond);
        if (values[1] != null) {
            values[1].genLLVM (env, emitter, function);
            Instruction bCond = ((Expression) values[1]).getInstruction ();
            Cast c = new Cast (token)
                .value (bCond).type (((Expression) values[1]).getType ())
                .dest (new Type (env, "bool", null));
            c.genLLVM (env, emitter, function);
            Instruction cond = new BINARY ()
                .op ("icmp ne").type ("i8").lhs (c.getInstruction ()).rhs ("0");
            function.add (cond);
            function.add (new BRANCH ().cond (cond).T (Lbody).F (Lbot));
        } else
            function.add (new BRANCH ().dest (Lbody));

        function.add (Lbody);
        values[3].genLLVM (env, emitter, function);
        function.add (new BRANCH ().dest (Lcont));

        function.add (Lcont);
        if (values[2] != null)
            values[2].genLLVM (env, emitter, function);
        function.add (new BRANCH ().dest (Lcond));

        function.add (Lbot);
    }

    public void print (java.io.PrintStream out) {
        out.print ("(for ");
        for (AST i: values) {
            if (i != null)
                i.print (out);
            else
                out.print ("()");
            out.print (" ");
        }
        out.println (")");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method) throws CError
                {
                    return new StFor (env, stream, method);
                }
            };
    }
}
