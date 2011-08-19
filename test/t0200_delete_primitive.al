// NAME Delete primitive
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let ppint = new int*;
    *ppint := new int;
    delete *ppint;

    let pints = new int (3);
    delete pints;

}
