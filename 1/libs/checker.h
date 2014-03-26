#ifndef CHECKER_H
#define CHECKER_H

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include "utility.h"



/* RESUMO
 *
 * Recebe f_list e um vetor de palavras VAZIO como parametro.
 * Retorna 0 caso f_list nao seja valida e 1 caso seja valida.
 *
 * fields contera' em ordem os fields passados.
 * values contera' em ordem os valores relacionados de cada field.
*/
int check_f_list( char* f_list, char*** fields, char*** values ); 



// RESUMO
//
// Recebe um vetor de palavras e checa se os elementos neste vetor
// sao unicos. Retorna 0 se o vetor contem so elementos unicos
// ou retorna -1, caso existam elementos repetidos.
int check_singleness( char** vector );

#endif
