#include "checker.h"


int check_f_list( char* f_list, char** fields, char** values ) 
{
    char** aux;
    char** tmp;
    int index;
    int title = 0, abstract = 0, author = 0;

    // Alocando memoria pra aux e tmp
    aux = (char**) calloc ( 7, sizeof(char*) );
    for (index = 0; index < 7; index++) 
    {
        aux[index] = (char*) calloc ( 32000, sizeof(char) );
    }

    tmp = (char**) calloc ( 2, sizeof(char*) );
    for (index = 0; index < 2; index++) 
    {
        tmp[index] = (char*) calloc ( 16000, sizeof(char) );
    }

    // Guarda separadamente "FIELD=STRING" dentro de aux
    split_str(f_list, S1, aux);

    // Preenche fields e values
    index = 0;

    while ( aux[index] != 0 && index < 7) 
    {
        split_str( aux[index], S2, tmp );
        strcpy( fields[index], tmp[0] );
        strcpy( values[index], tmp[1] );

        memset( tmp[0], 0, strlen( tmp[0] ) );
        memset( tmp[1], 0, strlen( tmp[1] ) );
        index++;
    }

    // Verifica se os 3 campos mandatorios estao presentes
    for (index = 0; index < 7; index++) 
    {
        if ( strcmp(fields[index], "title") == 0 ) title = 1;
        if ( strcmp(fields[index], "abstract") == 0 ) abstract = 1;
        if ( strcmp(fields[index], "author") == 0 ) author = 1;
    }

    // Liberando a memoria alocada
    for (index = 0; index < 7; index++) 
    {
        free( aux[index] );
    } 
    free( aux );

    for (index = 0; index < 2; index++) 
    {
        free( tmp[index] );
    } 
    free( tmp );

    // Exit
    if (title && abstract && author)
        return 1;
    else
        return 0;    
}
