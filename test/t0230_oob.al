// NAME Out of bounds
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// PEXIT -6
// PERR Error: Invalid array access.

executable testout;

int main (int argc, u8** argv) nomangle {
    let arr = new int[] (2);
    arr[2] := 1;
}
