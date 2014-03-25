#include "checker.h"


    int check_f_list( char* f_list, char*** fields, char*** values ) 
{
    char** aux;
    char** tmp;
    int index;
    int title = 0, abstract = 0, author = 0;

    // Alocando memoria
    (*fields) = (char**) calloc( 1, sizeof(char*) );
    (*values) = (char**) calloc( 1, sizeof(char*) );
    (*fields)[0] = (char*) calloc( 1, sizeof(char) );
    (*values)[0] = (char*) calloc( 1, sizeof(char) );

    // Guarda separadamente "FIELD=STRING" dentro de aux
    aux = split_str( f_list, S1 );

    // Preenche fields e values
    index = 0;
    while ( aux[index] != NULL ) 
    {
        tmp = split_str( aux[index], S2 );
        
        // Expandindo palavras
        (*fields)[index] = (char*) realloc( (*fields)[index], sizeof(char) * strlen(tmp[0])+1 ); 
        (*values)[index] = (char*) realloc( (*values)[index], sizeof(char) * strlen(tmp[1])+1 ); 

        strcpy( (*fields)[index], tmp[0] );
        strcpy( (*values)[index], tmp[1] );
        index++;

        // Expandindo vetores
        (*fields) = (char**) realloc( (*fields), sizeof(char*) * (index+1) );
        (*values) = (char**) realloc( (*values), sizeof(char*) * (index+1) );
        (*fields)[index] = (char*) calloc( 1, sizeof(char) );
        (*values)[index] = (char*) calloc( 1, sizeof(char) );

        // Limpando memoria alocada
        free_split( tmp );
    }

    // Ultimos elementos de fields/values = NULL
    (*fields)[index] = NULL;
    (*values)[index] = NULL;

    // Limpando memoria alocada
    free_split( aux );

    // Verifica se os 3 campos mandatorios estao presentes
    index = 0;
    while ( (*fields)[index] != NULL ) 
    {
        if ( strcmp( (*fields)[index], "title" ) == 0 ) title = 1;
        if ( strcmp( (*fields)[index], "abstract" ) == 0 ) abstract = 1;
        if ( strcmp( (*fields)[index], "author" ) == 0 ) author = 1;

        index++;
    }

    // Exit
    if (title && abstract && author)
        return 1;
    else
        return 0;    
}




int check_singleness( char** vector ) 
{
    // Duplicando o vector
    char** aux;
    int i, j;

    i = 0;
    while ( vector[i] != NULL ) i++;        // Tamanho do vector

    aux = (char**) calloc( i+2, sizeof(char*) );

    i = 0;
    while ( vector[i] != NULL ) 
    {
        aux[i] = (char*) str_dup( vector[i] );
        i++;
    }
    aux[i] =  NULL;
    
    // Transformando todas as strings pra lower-case
    i = 0;
    while ( aux[i] != NULL ) 
    {
        for ( j = 0; (unsigned long) j < strlen( aux[i] ); j++ ) 
        {
            aux[i][j] = tolower( aux[i][j] );
        }

        i++;
    }
    
    // Verificando unicidade
    i = 0;
    while ( aux[i] != NULL ) 
    {
        for ( j = i+1; aux[j] != NULL; j++ ) 
        {
            if ( strcmp( aux[i], aux[j] ) == 0 ) return -1;
        }

        i++;
    }
    
    // Limpando memoria
    free_split( aux );    
    
    // Exit
    return 0;
}



