// NAME Out of memory
// COMPILE ["-o", "testout", "-malloc", "testMalloc"]
// RUN ./testout
// DELETE testout
// PEXIT -6
// PERR Error: Out of memory.

executable testout;

u8* testMalloc (size n) nomangle {
    return null;
}

int main (int argc, u8** argv) nomangle {

    let arr = new int[] (4);

}
