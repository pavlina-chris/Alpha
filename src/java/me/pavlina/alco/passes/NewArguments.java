// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.passes;
import me.pavlina.alco.compiler.errors.CError;
import me.pavlina.alco.ast.*;
import java.util.List;
import java.util.ArrayList;

/**
 * "New" argument replacement pass. Replaces (call (new A) B) by
 * (new A B). */
public class NewArguments {

    private NewArguments () {}

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
                if (children.get (i) == null) continue;
                children.set (i, run (children.get (i)));
            }
            return item;
        }
    }

    private static Expression run (Expression item) throws CError {
        if (OpCall.class.isInstance (item))
            return run ((OpCall) item);
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
     * Call */
    private static Expression run (OpCall item) throws CError {
        List<AST> children = item.getChildren ();
        children.set (0, run (children.get (0)));
        children.set (1, run (children.get (1)));
        if (NewValue.class.isInstance (children.get (0))) {
            // (call (new))
            List<Expression> args = new ArrayList<Expression> ();
            assert Expression.class.isInstance (children.get (1));
            if (OpComma.class.isInstance (children.get (1))) {
                ((OpComma) children.get (1)).unpack (args);
            } else
                args.add ((Expression) children.get (1));
            ((NewValue) children.get (0)).setArguments (args);
            return (Expression) children.get (0);
        } else {
            return item;
        }
    }
}
