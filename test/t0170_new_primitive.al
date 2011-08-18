// NAME Instantiation/allocation of primitives
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 2
// POUT 0
// POUT 1
// POUT 2

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let ppint = new int*;
    *ppint := new int;
    **ppint := 2;
    putint (**ppint);

    let pints = new int (3);
    *(pints) := 0;
    *(pints + 1) := 1;
    *(pints + 2) := 2;
    putint (*pints);
    putint (*(pints + 1));
    putint (*(pints + 2));

}
