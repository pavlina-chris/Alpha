// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.parse;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.ast.*;
import static me.pavlina.alco.ast.Expression.Operator;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.llvm.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;

/**
 * Expression parser. When adding operators, look at the static initialiser for
 * most (only a select few, like function calls, needs special wiring). */
public class ExpressionParser {

    // This expression parser implements a basic shunting-yard algorithm. This
    // is a very simple, easy-to-maintain algorithm with which I've found very
    // few shortcomings.
    
    Stack<Operator> stack;
    Stack<Expression> output;
    int nest;
    boolean callPossible, unaryPossible;
    Env env;
    TokenStream stream;
    Method method;
    String end;

    /**
     * Quickly parse and return an expression.
     * @param end End markers. This is a string of single-character Token.OPERs
     * which end the expression. The parser will not stop on these while nested.
     * @return Expression or null */
    public static Expression parse (Env env, TokenStream stream,
                                    Method method, String end) throws CError {
        return new ExpressionParser (env, stream, method, end).parse ();
    }

    /**
     * Create an expression parser.
     * @param end End markers. This is a string of single-character Token.OPERs
     * which end the expression. The parser will not stop on these while nested.
     */
    public ExpressionParser (Env env, TokenStream stream, Method method,
                             String end) {
        this.env = env;
        this.stream = stream;
        this.method = method;
        this.end = end;
        stack = new Stack<Operator> ();
        output = new Stack<Expression> ();
        unaryPossible = true;
    }

    /**
     * Run the expression parser, returning the expression.
     * @return Expression or null */
    public Expression parse () throws CError {
        // Read everything
        while (readOne ()) {}

        // Empty the stack
        emptyStack ();

        // Get the item
        if (output.empty ())
            return null;
        Expression expr = output.pop ();
        if (!output.empty ()) {
            // We have a split expression. Try to find the leftmost item to
            // whine at. This may not always be accurate, but we want to give
            // the best error messages possible.
            List<AST> children = expr.getChildren ();
            AST pos = expr;
            while (children != null && children.size () > 0) {
                pos = children.get (0);
                children = pos.getChildren ();
            }
            throw CError.at ("split expression", pos.getToken ());
        }
        return expr;
    }

    /**
     * Read one item and handle it.
     * @return Whether an item could be found */
    private boolean readOne () throws CError {
        Token token = stream.peek ();

        // Check for EOF.
        if (token.is (Token.NO_MORE)) return false;

        // Check for an end character.
        if (token.is (Token.OPER)
            && token.value.length () == 1
            && nest == 0
            && end.indexOf (token.value) >= 0)
            return false;
        
        // Check for all possible items
        if (readValue (token)) return true;

        if (readOpenParen (token)) return true;

        if (readCloseParen (token)) return true;

        if (readOpenSquare (token)) return true;

        if (readCloseSquare (token)) return true;

        if (readOperator (token)) return true;

        // Anything else? It's invalid.
        throw CError.at ("invalid in expression", token);
    }

    /**
     * Get all remaining items off the stack */
    private void emptyStack () throws CError {
        while (!stack.empty ()) {
            Operator oper = stack.pop ();
            if (OpeningParen.class.isInstance (oper))
                throw CError.at ("mismatched parentheses", oper.getToken ());
            else if (OpeningSquare.class.isInstance (oper))
                throw CError.at ("mismatched brackets", oper.getToken ());
            else
                moveOper (oper);
        }
    }

    /**
     * Check for single values
     * @return Whether an item could be found */
    private boolean readValue (Token token) throws CError {
        if (token.is (Token.INT)) {
            output.push (new IntValue (env, stream));
            callPossible = unaryPossible = false;

        } else if (token.is (Token.REAL)) {
            output.push (new RealValue (env, stream));
            callPossible = unaryPossible = false;

        } else if (token.is (Token.OPER, "{")) {
            output.push (new ArrayValue (env, stream, method));
            callPossible = unaryPossible = false;

        } else if (token.is (Token.WORD, "true") ||
                   token.is (Token.WORD, "false")) {
            output.push (new BoolValue (env, stream));
            callPossible = unaryPossible = false;

        } else if (token.is (Token.WORD, "null")) {
            output.push (new NullValue (env, stream));
            callPossible = unaryPossible = false;

        } else if (token.is (Token.WORD, "new")) {
            output.push (new NewValue (env, stream));
            callPossible = true;
            unaryPossible = false;

        } else if (token.is (Token.EXTRA, "$$name")) {
            output.push (new NameValue (env, stream));
            callPossible = true;
            unaryPossible = false;

        } else if (token.is (Token.WORD) &&
                   !Keywords.isKeyword (token.value)) {
            output.push (new NameValue (env, stream));
            callPossible = true;
            unaryPossible = false;

        } else {
            return false;
        }
        return true;
    }

    /**
     * Check for and handle opening parentheses
     * @return Whether an item was found */
    private boolean readOpenParen (Token token) throws CError {
        if (token.is (Token.OPER, "(")) {
            // There are two things that can happen here.
            // 1. If callPossible is true, then this paren opens a function
            // call. The function name itself was just pushed to the output
            // stack; replace it with an OpCall on the operator stack.
            // 2. If callPossible is false, then this paren is just for
            // grouping. Push a special OpeningParen object to the
            // operator stack.

            if (callPossible) {
                Expression expr = output.pop ();
                stack.push (new OpCall (expr.getToken (), expr, method));
                stack.push (new OpeningParen (token));
                // If the next token is ), we have an empty function call.
                // This confuses shuntOper(), which sees a call as a unary
                // operator, so we push a placeholder.
                stream.next ();
                Token peek = stream.peek ();
                if (peek.is (Token.OPER, ")"))
                    output.push (new OpComma (peek));
            } else {
                stack.push (new OpeningParen (token));
                stream.next ();
            }
            ++nest;
            callPossible = false;
            unaryPossible = true;
            return true;
        } else
            return false;
    }

    /**
     * Check for and handle closing parentheses
     * @return Whether an item was found */
    private boolean readCloseParen (Token token) throws CError {
        if (token.is (Token.OPER, ")")) {
            // Find the matching open parenthesis
            boolean foundOpen = false;
            while (! stack.empty ()) {
                Operator oper = stack.pop ();
                if (OpeningParen.class.isInstance (oper)) {
                    foundOpen = true;
                    break;
                } else {
                    moveOper (oper);
                }
            }
            if (foundOpen) {
                if (!stack.empty () &&
                    OpCall.class.isInstance (stack.peek ())) {
                    moveOper (stack.pop ());
                }
            } else
                throw CError.at ("mismatched parentheses", token);
            --nest;
            callPossible = true;
            unaryPossible = false;
            stream.next ();
            return true;
        } else
            return false;
    }

    /**
     * Check for and handle opening square bracket
     * @return Whether an item was found */
    public boolean readOpenSquare (Token token) throws CError {
        if (token.is (Token.OPER, "[")) {
            // This signifies an index, which is like a function call. For
            // this to show up, we need "callPossible".
            if (!callPossible)
                throw Unexpected.at ("indexable before [", token);

            Expression expr = output.pop ();
            stack.push (new OpIndex (expr.getToken (), expr, method));
            stack.push (new OpeningSquare (token));
            callPossible = false;
            unaryPossible = true;
            stream.next ();
            ++nest;
            return true;
        } else
            return false;
    }

    /**
     * Check for and handle closing square bracket
     * @return Whether an item was found */
    public boolean readCloseSquare (Token token) throws CError {
        if (token.is (Token.OPER, "]")) {
            boolean foundOpen = false;
            while (! stack.empty ()) {
                Operator oper = stack.pop ();
                if (OpeningSquare.class.isInstance (oper)) {
                    foundOpen = true;
                    break;
                } else
                    moveOper (oper);
            }
            if (foundOpen) {
                if (OpIndex.class.isInstance (stack.peek ())) {
                    moveOper (stack.pop ());
                } else
                    assert false: stack.peek ();
            } else
                throw CError.at ("mismatched brackets", token);
            callPossible = true;
            unaryPossible = false;
            stream.next ();
            --nest;
            return true;
        } else
            return false;
    }

    /**
     * Check for and handle operator
     * @return Whether an item was found */
    public boolean readOperator (Token token) throws CError {
        if (token.is (Token.OPER) ||
            (token.is (Token.WORD) && Keywords.isKeyword (token.value))) {
            Expression.OperatorCreator creator;
            String message;
            if (unaryPossible) {
                creator = UNOPS.get (token.value);
                message = "unary operator";
            } else {
                creator = BINOPS.get (token.value);
                message = "binary operator";
            }
            callPossible = false;
            unaryPossible = true;

            if (creator == null)
                throw Unexpected.at (message, token);

            Operator oper = creator.create (env, stream, method);
            shuntOper (oper);

            // The 'as' operator expects a TypeValue
            if (OpCast.class.isInstance (oper)) {
                output.push (new TypeValue (env, stream));
                callPossible = true;
                unaryPossible = false;
            }

            return true;
        } else
            return false;
    }

    /**
     * Move an operator onto the output, giving it any operands it needs. */
    private void moveOper (Operator oper) throws CError {
        if (oper.getArity () == Expression.Arity.UNARY) {
            // Give it one operand
            if (output.empty ())
                throw CError.at ("requires one operand", oper.getToken ());
            Expression operand = output.pop ();
            oper.setOperands (operand, null);
        } else {
            // Give it two operands
            if (output.empty ())
                throw CError.at ("requires two operands", oper.getToken ());
            Expression right = output.pop ();
            if (output.empty ())
                throw CError.at ("requires two operands", oper.getToken ());
            Expression left = output.pop ();
            oper.setOperands (left, right);
        }
        output.push (oper);
    }

    /**
     * "Shunt" the operator through the shunting yard. This is the most
     * important part of the algorithm, even if it's very small. This is what
     * makes associativity and precedence work. */
    private void shuntOper (Operator oper) throws CError {
        while (! stack.empty ()) {
            Operator top = stack.peek ();
            // This is a big conditional. Break it up for readability. No need
            // to worry about the loss of short circuit here; no side effects
            // and very speedy operations.
            boolean p = oper.getAssociativity () == Expression.Associativity.LEFT;
            boolean q = oper.getPrecedence () <= top.getPrecedence ();
            boolean r = oper.getPrecedence () <  top.getPrecedence ();
            if ((p && q) || (!p && r)) {
                top = stack.pop ();
                moveOper (top);
            } else break;
        }
        stack.push (oper);
    }

    /**
     * Map of all binary operators. This maps the operator text to an
     * OperatorCreator. */
    private static final Map<String, Expression.OperatorCreator> BINOPS;

    /**
     * Map of all unary operators. This maps the operator text to an
     * OperatorCreator. */
    private static final Map<String, Expression.OperatorCreator> UNOPS;

    /**
     * Operator-loading static initialiser. This loads all operators into the
     * map. */
    static {
        BINOPS = new HashMap<String, Expression.OperatorCreator> ();
        UNOPS = new HashMap<String, Expression.OperatorCreator> ();

        BINOPS.put (":=", OpAssign.CREATOR);
        BINOPS.put ("=", OpEqError.CREATOR);
        BINOPS.put (",", OpComma.CREATOR);
        BINOPS.put ("as", OpCast.CREATOR);
        BINOPS.put (".", OpMember.CREATOR);
        BINOPS.put ("+", OpPlus.CREATOR);
        BINOPS.put ("-", OpMinus.CREATOR);
        BINOPS.put ("*", OpMul.CREATOR);
        BINOPS.put ("/", OpDiv.CREATOR);
        BINOPS.put ("%", OpMod.CREATOR);
        BINOPS.put ("%%", OpRem.CREATOR);
        BINOPS.put ("&", OpBAnd.CREATOR);
        BINOPS.put ("|", OpBOr.CREATOR);
        BINOPS.put ("^", OpBXor.CREATOR);
        BINOPS.put ("&&", OpLAnd.CREATOR);
        BINOPS.put ("||", OpLOr.CREATOR);
        BINOPS.put ("+=", OpAssignPlus.CREATOR);
        BINOPS.put ("-=", OpAssignMinus.CREATOR);
        BINOPS.put ("*=", OpAssignMul.CREATOR);
        BINOPS.put ("/=", OpAssignDiv.CREATOR);
        BINOPS.put ("%=", OpAssignMod.CREATOR);
        BINOPS.put ("%%=", OpAssignRem.CREATOR);
        BINOPS.put ("&=", OpAssignBAnd.CREATOR);
        BINOPS.put ("|=", OpAssignBOr.CREATOR);
        BINOPS.put ("^=", OpAssignBXor.CREATOR);
        BINOPS.put ("<", OpLt.CREATOR);
        BINOPS.put (">", OpGt.CREATOR);
        BINOPS.put ("<=", OpLe.CREATOR);
        BINOPS.put (">=", OpGe.CREATOR);
        BINOPS.put ("==", OpVEq.CREATOR);
        BINOPS.put ("!=", OpVNe.CREATOR);
        BINOPS.put ("===", OpREq.CREATOR);
        BINOPS.put ("!==", OpRNe.CREATOR);
        BINOPS.put ("?", OpQuestion.CREATOR);
        BINOPS.put (":", OpColon.CREATOR);
        
        UNOPS.put ("*", OpDeref.CREATOR);
        UNOPS.put ("&", OpAddress.CREATOR);
        UNOPS.put ("-", OpNeg.CREATOR);
        UNOPS.put ("!", OpLNot.CREATOR);
        UNOPS.put ("~", OpBNot.CREATOR);
        UNOPS.put ("++", OpIncrement.CREATOR);
        UNOPS.put ("--", OpDecrement.CREATOR);
    }
}

/**
 * Class representing an opening parenthesis. This is a sentinel value, and
 * very un-Operator-like and un-OOP-like. Bad. Very bad. :-) */
class OpeningParen extends Expression.Operator {
    private Token token;
    public OpeningParen (Token token) { this.token = token; }
    public int getPrecedence () { return 0; }
    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT; }
    public Expression.Arity getArity () { return Expression.Arity.UNARY; }
    public void setOperands (Expression left, Expression right) {}
    public Instruction getInstruction () { return null; }
    public Token getToken () { return token; }
    public List<AST> getChildren () { return null; }
    public void checkTypes (Env env, Resolver r) throws CError {}
    public Type getType () { return null; }
    public void genLLVM (Env env, Emitter emitter, Function f) {}
    public void checkPointer (boolean w, Token t) throws CError {}
    public Instruction getPointer (Env e, Emitter em, Function f) {
        return null; }
    public void print (java.io.PrintStream out) {}
}

/**
 * Class representing an opening bracket. This is a sentinel value, and
 * very un-Operator-like and un-OOP-like. Bad. Very bad. :-) */
class OpeningSquare extends Expression.Operator {
    private Token token;
    public OpeningSquare (Token token) { this.token = token; }
    public int getPrecedence () { return 0; }
    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT; }
    public Expression.Arity getArity () { return Expression.Arity.UNARY; }
    public void setOperands (Expression left, Expression right) {}
    public Instruction getInstruction () { return null; }
    public Token getToken () { return token; }
    public List<AST> getChildren () { return null; }
    public void checkTypes (Env env, Resolver r) throws CError {}
    public Type getType () { return null; }
    public void genLLVM (Env env, Emitter emitter, Function f) {}
    public void checkPointer (boolean w, Token t) throws CError {}
    public Instruction getPointer (Env e, Emitter em, Function f) {
        return null; }
    public void print (java.io.PrintStream out) {}
}
