// NAME Tokenisation
// COMPILE ["-tokens"]
// COUT REAL     71x  1 0.0
// COUT REAL     72x  1 0.0e1
// COUT REAL     73x  1 0.0E2
// COUT REAL     74x  1 0.1e-2
// COUT REAL     75x  1 0.4e+3
// COUT INT      76x  1 0x0123456789abcdefABCDEF
// COUT INT      77x  1 0X0123456789abcdefABCDEF
// COUT INT      78x  1 123456789
// COUT INT      79x  1 0D0123456789
// COUT INT      80x  1 0d0123456789
// COUT INT      81x  1 0O01234567
// COUT INT      82x  1 0o01234567
// COUT INT      83x  1 0B01
// COUT INT      84x  1 0b01
// COUT WORD     85x  1 abcd
// COUT WORD     86x  1 @abcd
// COUT OPER     87x  1 ++
// COUT OPER     87x  4 --
// COUT OPER     87x  7 ~
// COUT OPER     87x  9 -
// COUT OPER     87x 11 *
// COUT OPER     87x 13 /
// COUT OPER     87x 15 %
// COUT OPER     87x 17 %%
// COUT OPER     87x 20 +
// COUT OPER     87x 22 -
// COUT OPER     87x 24 <<
// COUT OPER     87x 27 >>
// COUT OPER     87x 30 &
// COUT OPER     87x 32 ^
// COUT OPER     87x 34 |
// COUT OPER     87x 36 !
// COUT OPER     87x 38 &&
// COUT OPER     87x 41 ||
// COUT OPER     87x 44 <
// COUT OPER     87x 46 <=
// COUT OPER     87x 49 >
// COUT OPER     87x 51 >=
// COUT OPER     87x 54 ==
// COUT OPER     87x 57 !=
// COUT OPER     87x 60 ===
// COUT OPER     87x 64 !==
// COUT OPER     88x  1 *=
// COUT OPER     88x  4 /=
// COUT OPER     88x  7 %=
// COUT OPER     88x 10 %%=
// COUT OPER     88x 14 +=
// COUT OPER     88x 17 -=
// COUT OPER     88x 20 <<=
// COUT OPER     88x 24 >>=
// COUT OPER     88x 28 &=
// COUT OPER     88x 31 ^=
// COUT OPER     88x 34 |
// COUT OPER     88x 36 :=
// COUT OPER     89x  1 (
// COUT OPER     89x  3 )
// COUT OPER     89x  5 [
// COUT OPER     89x  7 ]
// COUT OPER     89x  9 {
// COUT OPER     89x 11 }
// COUT OPER     89x 13 ,
// COUT OPER     89x 15 .
// COUT OPER     89x 17 =
// COUT EXTRA    90x  1 $$abcd
// COUT WORD     91x  1 a
// COUT OPER     91x  2 +
// COUT WORD     91x  3 b

0.0
0.0e1
0.0E2
0.1e-2
0.4e+3
0x0123456789abcdefABCDEF
0X0123456789abcdefABCDEF
123456789
0D0123456789
0d0123456789
0O01234567
0o01234567
0B01
0b01
abcd
@abcd
++ -- ~ - * / % %% + - << >> & ^ | ! && || < <= > >= == != === !==
*= /= %= %%= += -= <<= >>= &= ^= | :=
( ) [ ] { } , . =
$$abcd
a+b
