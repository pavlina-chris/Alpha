// NAME Method overloading
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 0.000000

executable testout;
extern void putint (int);
extern void putdbl (double);

void print (int i) {
    putint (i);
}

void print (double d) {
    putdbl (d);
}

int main (int argc, u8** argv) nomangle {

    print (0);
    print (0.0);

    return 0;

}
