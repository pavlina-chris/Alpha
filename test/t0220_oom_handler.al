// NAME Out-of-memory error handler
// COMPILE ["-o", "testout", "-malloc", "testMalloc"]
// RUN ./testout
// DELETE testout
// PEXIT -6
// POUT 32
// POUT 26
// POUT 15
// PERR Error: Out of memory.

executable testout;
extern int putint (int);

u8* testMalloc (size n) nomangle {
    return null;
}

bool @oom (size sz, unsigned line, unsigned col) {
    putint (sz as ssize as int);
    putint (line as int);
    putint (col as int);
    return true;
}

int main (int argc, u8** argv) nomangle {
    let arr = new int[] (4);
}
