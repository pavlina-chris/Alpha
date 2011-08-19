// NAME Delete array
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout

executable testout;

int main (int argc, u8** argv) nomangle {
    let test1 = new u8[] (5);
    delete test1;

    test1 := new u8[] (8);
    let test2 = new size[] (65536);
    delete test1, test2;

    return 0;
}
