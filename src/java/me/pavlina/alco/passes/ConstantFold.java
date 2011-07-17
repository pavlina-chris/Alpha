// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.passes;
import me.pavlina.alco.compiler.errors.CError;
import me.pavlina.alco.ast.*;
import java.util.List;

/**
 * Constant arithmetic folding pass. This operates on an AST, performing
 * constant arithmetic folding (e.g., rewriting 2*3 as 5).
 */
public class ConstantFold {
    private ConstantFold () {}

    /**
     * Run the pass. */
    public static AST run (AST item) throws CError {
        // Differentiate based on object types
        if (Expression.class.isInstance (item))
            return run ((Expression) item);

        else {
            List<AST> children = item.getChildren ();
            if (children == null) return item;
            for (int i = 0; i < children.size (); ++i) {
                children.set (i, run (children.get (i)));
            }
            return item;
        }
    }

    private static Expression run (Expression item) throws CError {
        if (OpDiv.class.isInstance (item))
            return run ((OpDiv) item);

        else if (OpMinus.class.isInstance (item))
            return run ((OpMinus) item);

        else if (OpMul.class.isInstance (item))
            return run ((OpMul) item);
        
        else if (OpPlus.class.isInstance (item))
            return run ((OpPlus) item);

        else if (OpNeg.class.isInstance (item))
            return run ((OpNeg) item);

        else {
            List<AST> children = item.getChildren ();
            if (children == null) return item;
            // All children of expressions are expressions
            for (int i = 0; i < children.size (); ++i) {
                children.set (i, run ((Expression) children.get (i)));
            }
            return item;
        }
    }

    /**
     * Division */
    private static Expression run (OpDiv item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        children.set (1, run (children.get (1)));
        Expression lhs = (Expression) children.get (0);
        Expression rhs = (Expression) children.get (1);
            children.set (0, run (lhs));
            children.set (1, run (rhs));

        if (IntValue.class.isInstance (lhs) &&
            IntValue.class.isInstance (rhs)) {
            IntValue lhsI = (IntValue) lhs;
            IntValue rhsI = (IntValue) rhs;
            try {
                lhsI.setValue (lhsI.getValue ().divide (rhsI.getValue ()));
            } catch (ArithmeticException e) {
                throw CError.at ("integer division by zero", item.getToken ());
            }
            return lhsI;

        } else if (RealValue.class.isInstance (lhs) &&
                   RealValue.class.isInstance (rhs)) {
            RealValue lhsR = (RealValue) lhs;
            RealValue rhsR = (RealValue) rhs;
            lhsR.setValue (lhsR.getValue () / rhsR.getValue ());
            return lhsR;

        } else {
            return item;
        }
    }

    /**
     * Multiplication */
    private static Expression run (OpMul item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        children.set (1, run (children.get (1)));
        Expression lhs = (Expression) children.get (0);
        Expression rhs = (Expression) children.get (1);

        if (IntValue.class.isInstance (lhs) &&
            IntValue.class.isInstance (rhs)) {
            IntValue lhsI = (IntValue) lhs;
            IntValue rhsI = (IntValue) rhs;
            lhsI.setValue (lhsI.getValue ().multiply (rhsI.getValue ()));
            return lhsI;

        } else if (RealValue.class.isInstance (lhs) &&
                   RealValue.class.isInstance (rhs)) {
            RealValue lhsR = (RealValue) lhs;
            RealValue rhsR = (RealValue) rhs;
            lhsR.setValue (lhsR.getValue () * rhsR.getValue ());
            return lhsR;

        } else {
            return item;
        }
    }

    /**
     * Addition */
    private static Expression run (OpPlus item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        children.set (1, run (children.get (1)));
        Expression lhs = (Expression) children.get (0);
        Expression rhs = (Expression) children.get (1);

        if (IntValue.class.isInstance (lhs) &&
            IntValue.class.isInstance (rhs)) {
            IntValue lhsI = (IntValue) lhs;
            IntValue rhsI = (IntValue) rhs;
            lhsI.setValue (lhsI.getValue ().add (rhsI.getValue ()));
            return lhsI;

        } else if (RealValue.class.isInstance (lhs) &&
                   RealValue.class.isInstance (rhs)) {
            RealValue lhsR = (RealValue) lhs;
            RealValue rhsR = (RealValue) rhs;
            lhsR.setValue (lhsR.getValue () + rhsR.getValue ());
            return lhsR;

        } else {
            return item;
        }
    }

    /**
     * Subtraction */
    private static Expression run (OpMinus item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        children.set (1, run (children.get (1)));
        Expression lhs = (Expression) children.get (0);
        Expression rhs = (Expression) children.get (1);

        if (IntValue.class.isInstance (lhs) &&
            IntValue.class.isInstance (rhs)) {
            IntValue lhsI = (IntValue) lhs;
            IntValue rhsI = (IntValue) rhs;
            lhsI.setValue (lhsI.getValue ().subtract (rhsI.getValue ()));
            return lhsI;

        } else if (RealValue.class.isInstance (lhs) &&
                   RealValue.class.isInstance (rhs)) {
            RealValue lhsR = (RealValue) lhs;
            RealValue rhsR = (RealValue) rhs;
            lhsR.setValue (lhsR.getValue () - rhsR.getValue ());
            return lhsR;

        } else {
            return item;
        }
    }

    /**
     * Negation */
    private static Expression run (OpNeg item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        Expression op = (Expression) children.get (0);

        if (IntValue.class.isInstance (op)) {
            IntValue opI = (IntValue) op;
            opI.setValue (opI.getValue ().negate ());
            return opI;

        } else if (RealValue.class.isInstance (op)) {
            RealValue opR = (RealValue) op;
            opR.setValue (-opR.getValue ());
            return opR;

        } else {
            return item;
        } 
    }
}
