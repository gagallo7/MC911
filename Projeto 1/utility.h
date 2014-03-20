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

// Separador mais externo. E' o que separa os fields.
#define S1 "[;.1.;]"            

// Separador mediano. Pode ser entendido como o "=" existente entre os fields e seus valores.
#define S2 "[;.2.;]"            

// Separador mais interno. Pode ser entendido como o "," existente entre os dados do show.
#define S3 "[;.3.;]"            

// Macros
FILE* F;

#define COMPILE(X,...) F = fopen ("teste.html", "a");       \
                       fprintf ( F, (X), ##__VA_ARGS__ );   \
                       fclose (F)                           \

#define COMPILE_FILE(TITLE, X,...) F = fopen ((TITLE), "a");       \
                       fprintf ( F, (X), ##__VA_ARGS__ );   \
                       fclose (F)                           \

#define LOG(X,...) F = fopen ("debug.err", "a");            \
                   fprintf ( F, (X), ##__VA_ARGS__ );       \
                   fprintf ( F, "\n" );                     \
                   fclose (F)

/* =============================================================================== */ 
/* FUNCTIONS                                                                       */ 
/* =============================================================================== */ 



// ARGUMENTOS:
//
// 1.   Numero X de strings que serao passadas
// 2.   X strings separadas por ","
//
// RETORNA:
//
// Um ponteiro para uma string que e' a concatencao de todas
// as strings passadas. Ela deve ser desalocada.
char* concat(int count, ...);



// ARGUMENTOS:
//
// 1.   Posicao da string que inicia a substring
// 2.   Quantos caracteres a partir da posicao informada
//
// RETORNA:
//
// Um apontador para a substring formada, que termina com '\0'.
// Repare que você deve desalocar esse apontador.
char* sub_str(char* src, int howMany);


// ARGUMENTOS:
//
// 1.   Uma string
// 2.   Outra string, que e' um delimitador
//
// RETORNA:
//
// Um vetor de palavras, resultado do split.
// Este vetor, assim como cada palavra dele, devem ser desalocados.
//
// EXEMPLO DE USO:
//
// char* str = "ab<delimitador>cd<delimitador>ef";
// char* delim = "<delimitador";
// char** ret = split_str( str, delim );
//
// int i = 0;
// while ( ret[i] != NULL ) 
// {
//      free( ret[i] );
//      i++;
// }
// free( ret );
//
// 
// Neste caso, "ret" seria um vetor de 4 elementos:
// 1.   ret[0] = "ab"
// 2.   ret[1] = "cd"
// 3.   ret[2] = "ef"
// 4.   ret[3] = NULL
char** split_str(char* str, char* delimiter);

// Struct que define uma noticia, com todos os seus campos 
// armazenados. A ideia é imprimí-la somente no final da 
// compilação
typedef struct
{
    char* name;
    int col;         // Numero de colunas da noticia
    char** show;     // Ordem com que os campos devem ser imprimidos
    char** fields;   // Campos presentes na noticia
    char** values;   // Valores associados dos campos
} news;

// Verifica se o campo existe
int issetField ( news, char* );

// Retorna o valor do campo requerido
char* fetchField ( news, char* );

// Use para liberar rapidamente a memoria retornada pelo split_str()
void free_split(char** vector);



/* =============================================================================== */ 
