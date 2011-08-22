// NAME Out-of-bounds error handler
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// PEXIT -6
// POUT 20
// POUT 5
// PERR Error: Invalid array access.

executable testout;
extern void putint (int);

void @bounds (unsigned line, unsigned col) {
    putint (line as int);
    putint (col as int);
}

int main (int argc, u8** argv) nomangle {
    let arr = new int[] (2);
    arr[2] := 1;
}
