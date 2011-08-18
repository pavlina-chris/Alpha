/* It's a freakin' launcher. No copyright claimed here. */

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <alloca.h>
#include <stdlib.h>

#define JAVA "java"
#define ALCOJAR_INSTALLED "/usr/lib/alco.jar"
#define ALCOJAR_LOCAL     "./alco.jar"

int main (int argc, char **argv) {
    
    struct stat sbuf;
    char const *alco;
    char **new_argv;
    size_t i, j;

    /* Find alco */
    alco = stat (ALCOJAR_LOCAL, &sbuf) ? ALCOJAR_INSTALLED : ALCOJAR_LOCAL;

    /* Make a new argv. We need to add two arguments, plus maybe '-ea',
     * plus a NULL, so make it argc+4 */
    new_argv = alloca ((argc + 4) * sizeof (*new_argv));
    i = 0;
    new_argv[i++] = JAVA;
    if (getenv ("ASSERT"))
      new_argv[i++] = "-ea";
    new_argv[i++] = "-jar";
    new_argv[i++] = (char *) alco;
    for (j = 1; j < argc; ++j) {
        new_argv[j + i - 1] = argv[j];
    }
    new_argv[j + i - 1] = NULL;

    /* Execute alco */
    execvp (JAVA, new_argv);

    /* We failed if we made it here. */
    perror ("execvp");
    return 1;
}
