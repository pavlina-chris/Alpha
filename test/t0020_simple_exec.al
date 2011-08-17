// NAME Simple executable compilation and run
// COMPILE ["-o", "testout"]
// RUN ./testout
// PEXIT 3
// DELETE testout

executable testout;

int main (int argc, u8** argv) nomangle {
    return 3;
}
