// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// MachineValidator - whine if not -O0,1,2,3

package me.pavlina.alco.compiler;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Validator for the -O command line argument
 */
public class OptimisationValidator implements IParameterValidator
{

    /**
     * Whine if -O is not 0, 1, 2, 3
     * @param name Argument name (-m)
     * @param value Argument value ("0", "1", "2", "3")
     * @throws ParameterException if invalid
     */
    @Override
    public void validate (String name, String value) throws ParameterException
    {
        int n = Integer.parseInt (value);
        switch (n) {
        case 0:
        case 1:
        case 2:
        case 3:
            break;
        default:
            throw new ParameterException ("Argument for " + name
                                          + " must be 0, 1, 2, or 3 (found "
                                          + value + ")");
        }
    }
}
