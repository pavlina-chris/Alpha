// NAME String literal (Hello, world!)
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT Hello, world!

executable testout;
extern int puts (u8*);

int main (int argc, u8** argv) nomangle {
    puts ("Hello, world!");
}
