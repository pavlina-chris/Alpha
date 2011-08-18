// NAME Variable resolution in nested scopes
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT 2
// POUT 3
// POUT 1

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let n = 1;
    putint (n);

    {
        let n = 2;
        putint (n);
        n := 3;
        putint (n);
    }

    putint (n);

}
