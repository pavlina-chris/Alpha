#!/usr/bin/env python
# Alco compatible with Python 3
# Copyright (c) 2011, Christopher Pavlina. All rights reserved.

# This is the tester for Alco. It runs all tests in test/ in order.

import os, sys, subprocess, json
ALCO_C_HELPERS = ["test/helpers.c"]
ALCO_C_HELPERS_O = ["test/helpers.o"]
ALCO_STD_CMDLINE = ["./alco", "-path=runtime-32:Doxyfile",
                    "-path=runtime-64:Doxyfile"] + ALCO_C_HELPERS_O
CC = "clang"

def start (name):
    """
    Output a message about starting the given test.
    """
    msg = "%-70s" % name
    sys.stderr.write (msg)
    sys.stderr.flush ()

def Pass ():
    """
    Output a message that the test passed.
    """
    sys.stderr.write (" [ PASS ]\n")
    sys.stderr.flush ()

def Fail (text):
    """
    Output a message that the test failed, then give the test's output, then
    quit.
    """
    sys.stderr.write (" [ FAIL ]\n")
    sys.stderr.flush ()
    sys.stderr.write (text)
    sys.stderr.flush ()
    sys.exit (1)

def compile_helpers ():
    for helper in ALCO_C_HELPERS:
        output = helper[:-2] + ".o"
        print (output)
        p = subprocess.Popen (["clang", "-fPIC", "-c", helper, "-o", output])
        if p.wait ():
            sys.exit (1)

def run_alpha_test (filename):
    """
    Run a test in a .al file. The first lines must start with // and describe
    the test.
    """
    name = ""
    cmdline = []
    delete = []
    run = ""
    cout = ""
    cerr = ""
    cexit = 0
    pout = ""
    perr = ""
    pexit = 0
    with open (filename) as f:
        for line in f:
            line = line.strip ()
            if not line.startswith ("// "): break
            line = line[3:]
            tag, sep, rest = line.partition (" ")
            if tag == "NAME":
                name = rest
            elif tag == "COMPILE":
                cmdline = json.loads (rest)
            elif tag == "DELETE":
                delete.append (rest)
            elif tag == "RUN":
                run = rest
            elif tag == "COUT":
                cout += rest + "\n"
            elif tag == "CERR":
                cerr += rest + "\n"
            elif tag == "CEXIT":
                cexit = int (rest)
            elif tag == "POUT":
                pout += rest + "\n"
            elif tag == "PERR":
                perr += rest + "\n"
            elif tag == "PEXIT":
                pexit = int (rest)

    start (name)
    # Compile
    cmdline = ALCO_STD_CMDLINE + [filename] + cmdline
    compiler = subprocess.Popen (cmdline,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
    exitcode = compiler.wait ()
    got_cout = compiler.stdout.read ()
    got_cerr = compiler.stderr.read ()

    if exitcode != cexit:
        msg = "GOT EXIT CODE %d (WANTED %d)\n" % (exitcode, cexit)
        msg += got_cout
        msg += got_cerr
        for i in delete:
            try:
                os.unlink (i)
            except OSError:
                pass
        Fail (msg)
    if got_cout != cout or got_cerr != cerr:
        msg = "INCORRECT OUTPUT\n"
        msg += got_cout
        msg += got_cerr
        for i in delete:
            try:
                os.unlink (i)
            except OSError:
                pass
        Fail (msg)

    if run:
        prog = subprocess.Popen ([run],
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        exitcode = prog.wait ()
        got_pout = prog.stdout.read ()
        got_perr = prog.stderr.read ()

        if exitcode != pexit:
            msg = "GOT EXIT CODE %d (WANTED %d)\n" % (exitcode, pexit)
            msg += got_pout
            msg += got_perr
            for i in delete:
                try:
                    os.unlink (i)
                except OSError:
                    pass
            Fail (msg)
        if got_pout != pout or got_perr != perr:
            msg = "INCORRECT OUTPUT\n"
            msg += got_pout
            msg += got_perr
            for i in delete:
                try:
                    os.unlink (i)
                except OSError:
                    pass
            Fail (msg)

    for i in delete:
        os.unlink (i)
    Pass ()

def main ():
    if "--help" in sys.argv:
        print ("Tests AlCo. AlCo must be fully compiled already.")

    compile_helpers ()
    for fn in sorted (os.listdir ("test")):
        if fn.endswith (".al"):
            run_alpha_test ("./test/" + fn)

if __name__ == '__main__':
    main ()
