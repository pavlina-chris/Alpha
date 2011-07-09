// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.Map;
import java.util.HashMap;

/**
 * Temporary counter. Returns a sequential temporary for the given prefix.
 * Every prefix gets its own count. */
public class Counter {

    private Map<String, Integer> counts;

    public Counter () {
        counts = new HashMap<String, Integer> ();
    }

    /**
     * Get a new temporary for the prefix. */
    public int getTemporary (String prefix) {
        Integer count_ = counts.get (prefix);
        int count;
        if (count_ == null)
            count = 2;
        else
            count = count_.intValue () + 1;
        counts.put (prefix, count);
        return count - 1;
    }

    /**
     * Reset the count for a prefix. Returns the old counter token as an
     * opaque object; this can be put back in with setCount. */
    public Object resetCount (String prefix) {
        Integer count_ = counts.get (prefix);
        counts.remove (prefix);
        if (count_ != null)
            return count_;
        else
            return new Integer (1);
    }

    /**
     * Set the count, from a value returned by resetCount().
     * @throws ClassCastException if 'count' is of the wrong type. This should
     *  never happen, if you always treat counts as opaque objects.
     * @throws NullPointerExceptoin if 'count' is null
     */
    public void setCount (String prefix, Object count)
        throws ClassCastException, NullPointerException
    {
        if (count == null) throw new NullPointerException ();
        counts.put (prefix, (Integer) count);
    }

}
