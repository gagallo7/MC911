#include <stdio.h>
#include <string.h>
#include <stdlib.h>

// Separador mais externo. E' o que separa os fields.
#define S1 "[;.1.;]"            

// Separador mediano. Pode ser entendido como o "=" existente entre os fields e seus valores.
#define S2 "[;.2.;]"            

// Separador mais interno. Pode ser entendido como o "," existente entre os dados do show.
#define S3 "[;.3.;]"            
/* RESUMO
 *
 * Recebe f_list e um vetor de palavras VAZIO como parametro.
 * Retorna 0 caso f_list nao seja valida e 1 caso seja valida.
 *
 * fields contera' em ordem os fields passados.
 * values contera' em ordem os valores relacionados de cada field.
*/
int check_f_list( char* f_list, char*** fields, char*** values ); 
