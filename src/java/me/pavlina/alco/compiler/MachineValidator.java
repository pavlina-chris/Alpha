// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// MachineValidator - whine if not -m32 or -m64

package me.pavlina.alco.compiler;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Validator for the -m command line argument
 */
public class MachineValidator implements IParameterValidator
{

    /**
     * Whine if -m is not 32, 64, or 0 (which is the default)
     * @param name Argument name (-m)
     * @param value Argument value ("32", "64", "0")
     * @throws ParameterException if invalid
     */
    @Override
    public void validate (String name, String value) throws ParameterException
    {
        int n = Integer.parseInt (value);
        if ((n != 32) && (n != 64) && (n != 0))
            throw new ParameterException ("Argument for " + name
                                          + " must be 32 or 64 (found " + value
                                          + ")");
    }
}
