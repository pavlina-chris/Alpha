#include <stdio.h>
#include <unistd.h>

void putint (int i) {
  printf ("%d\n", i);
}

void putssize (ssize_t i) {
  printf ("%zd\n", i);
}

void putdbl (double d) {
  printf ("%f\n", d);
}

