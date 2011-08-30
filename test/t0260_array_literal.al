// NAME Full array literal
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT Hello!

executable testout;
extern int puts (u8*);

int main (int argc, u8** argv) nomangle {
    // Hooray! Almost at strings!
    let hello_ = {72, 101, 108, 108, 111, 33, 0};
    let hello = new u8[] (7);
    hello[0] := hello_[0] as unsigned as u8;
    hello[1] := hello_[1] as unsigned as u8;
    hello[2] := hello_[2] as unsigned as u8;
    hello[3] := hello_[3] as unsigned as u8;
    hello[4] := hello_[4] as unsigned as u8;
    hello[5] := hello_[5] as unsigned as u8;
    hello[6] := hello_[6] as unsigned as u8;
    puts (hello as u8*);
}
