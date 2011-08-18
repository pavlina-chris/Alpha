// NAME Disallow redeclaration of variable in same scope
// COMPILE ["-o", "testout"]
// CERR t0150_redeclare.al:14:5: error: variable 'n' already declared at 13:5
// CERR     let n = 0;
// CERR     ^~~       
// CEXIT 1
/* Note extra spaces above */

package testout;

void f () {

    let n = 0;
    let n = 0;

}
