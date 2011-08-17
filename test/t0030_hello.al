// NAME Simple output program
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1337

executable testout;

extern void putint (int);

int main (int argc, u8** argv) nomangle {
    putint (1337);
    return 0;
}
