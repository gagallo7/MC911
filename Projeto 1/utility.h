/* =============================================================================== */ 
/* INCLUDES                                                                        */ 
/* =============================================================================== */ 
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

/* =============================================================================== */ 
/* MACROS                                                                          */ 
/* =============================================================================== */ 

// Abaixo eu declaro uma lista de "separadores"
#define S1 "[;.1.;]"            // Separador mais externo. E' o que separa os fields.
#define S2 "[;.2.;]"            // Separador mediano. Pode ser entendido como o "=" existente entre os fields e seus valores.
#define S3 "[;.3.;]"            // Separador mais interno. Pode ser entendido como o "," existente entre os dados do show.

// Macros
FILE* F;

#define COMPILE(X,...) F = fopen ("teste.html", "a");       \
                       fprintf ( F, (X), ##__VA_ARGS__ );   \
                       fclose (F)                           \

#define LOG(X,...) F = fopen ("debug.err", "a");            \
                   fprintf ( F, (X), ##__VA_ARGS__ );       \
                   fprintf ( F, "\n" );                     \
                   fclose (F)
/* =============================================================================== */ 
/* FUNCTIONS                                                                       */ 
/* =============================================================================== */ 
char *concat(int count, ...);
void sub_str(char* src, char* dst, int howMany);
void split_str(char* str, char* delimiter, char** vector);

/* =============================================================================== */ 
