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
import me.pavlina.alco.parse.ExpressionParser;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Return (duh). Syntax:
 * return ;
 * return {expression} ;
 */
public class StReturn extends Statement
{
    Token token;
    Expression[] value;
    List<Expression> values;
    Method method;

    public StReturn (Env env, TokenStream stream, Method method) throws CError {
        this.method = method;
        token = stream.next ();
        if (!token.is (Token.WORD, "return"))
            throw new RuntimeException ("StReturn instantiated without kwd");

        value = new Expression[]
            {ExpressionParser.parse (env, stream, method, ";")}; // Allow null

        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";"))
            throw Unexpected.after (";", stream.last ());
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (value);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        if (value[0] == null) {
            if (method.getType () != null) {
                throw CError.at ("length of return list does not match " +
                                 "function declaration", token);
            }
            return;
        }

        // Unpack tuples
        values = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (value[0])) {
            ((OpComma) value[0]).unpack (values);
        } else {
            values.add (value[0]);
        }

        for (Expression i: values) {
            i.setParent (this);
            i.checkTypes (env, resolver);
        }

        List<Type> methodTypes = method.getTypes ();

        if (values.size () != methodTypes.size ())
            throw CError.at ("length of return list does not match function" +
                             " declaration", token);

        for (int i = 0; i < values.size (); ++i) {
            Type.checkCoerce (values.get (i), methodTypes.get (i), token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (value[0] == null) {
            function.add (new RET ());
        } else {
            List<Type> methodTypes = method.getTypes ();
            for (int i = 1; i < values.size (); ++i) {
                values.get (i).genLLVM (env, emitter, function);
                Instruction val = values.get (i).getInstruction ();
                Cast c = new Cast (token)
                    .value (val).type (values.get (i).getType ())
                    .dest (methodTypes.get (i));
                c.genLLVM (env, emitter, function);

                function.add (new STORE ()
                              .pointer ("%.R" + Integer.toString (i))
                              .type (LLVMType.getLLVMName (methodTypes.get (i)))
                              .value (c.getInstruction ()));
            }
            values.get (0).genLLVM (env, emitter, function);
            Instruction val = values.get (0).getInstruction ();
            Cast c = new Cast (token)
                .value (val).type (values.get (0).getType ())
                .dest (methodTypes.get (0));
            c.genLLVM (env, emitter, function);

            function.add (new RET ()
                          .value (c.getInstruction ()));
        }
    }

    public void print (java.io.PrintStream out) {
        if (value[0] == null)
            out.print ("(return)");
        else {
            out.print ("(return ");
            value[0].print (out);
            out.print (")");
        }
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method)
                    throws CError
                {
                    return new StReturn (env, stream, method);
                }
            };
    }
}
