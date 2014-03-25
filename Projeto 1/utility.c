/* =============================================================================== */ 
#include "utility.h"

/* =============================================================================== */ 
char* concat(int count, ...)
{
    va_list ap;
    int len = 1, i;

    va_start(ap, count);
    for(i=0 ; i<count ; i++)
        len += strlen(va_arg(ap, char*));
    va_end(ap);

    char *result = (char*) calloc(sizeof(char),len);
    int pos = 0;

    // Actually concatenate strings
    va_start(ap, count);
    for(i=0 ; i<count ; i++)
    {
        char *s = va_arg(ap, char*);
        strcpy(result+pos, s);
        pos += strlen(s);
    }
    va_end(ap);

    return result;
}

/* =============================================================================== */ 
char* sub_str(char* src, int howMany) 
{
    char* dst = (char*) calloc( howMany+1, sizeof(char) );
    int i;

    for (i = 0; i < howMany; i++) 
    {
        if (src[i] == '\0') 
            break;

        dst[i] = src[i];
    }

    dst[i] = '\0';
    return dst;
}


/* =============================================================================== */ 
char** split_str(char* str, char* delimiter) 
{
    int delim_len = strlen(delimiter);
    int i = 0;
    int str_len = strlen(str);
    int index = 0, sub_index = 0;
    char* aux;
    char** vector;

    // Condicao de saida imediata
    if ( str_len == 0 || delim_len == 0 ) return NULL;

    // Alocando memoria
    vector = (char**) calloc( 1, sizeof(char*) );
    vector[0] = (char*) calloc( 1, sizeof(char) );

    while (i < str_len) 
    {
        aux = sub_str(str+i, delim_len);

        if ( strcmp(aux, delimiter) != 0 ) 
        {
            vector[index][sub_index] = str[i];

            i++;
            sub_index++;

            vector[index] = (char*) realloc( vector[index], sizeof(char) * (sub_index+2) );
            
        } else 
        {
            vector[index][sub_index] = '\0';

            i += delim_len;
            sub_index = 0;

            if ( strlen(vector[index]) > 0 ) 
            {
                index++;
                vector = (char**) realloc( vector, sizeof(char*) * (index+2) );
                vector[index] = (char*) calloc( 1, sizeof(char) );
            }
        }

        // Liberando memoria alocada
        free( aux );
    }

    vector[index][sub_index] = '\0';

    // Adicionando ultima palavra igual a NULL
    vector = (char**) realloc( vector, sizeof(char*) * (index+2) );
    vector[index+1] = NULL;

    // Exit
    return vector;
}

char* fetchField ( news* nw, char* query )
{
    int i = 0;

    while ( strcmp ( query, nw->fields[i] ) != 0 )
    {
        i++;
        if ( nw->fields[i] == NULL )
        {
            return 0;
        }
    }

    return nw->values[i];
}

int issetField ( news* nw, char* field )
{
    int i = 0;
    int match = 0;

    while ( nw->show[i] != NULL )
    {
        LOG ( "issetField: Comparando %s == %s ? ", nw->show[i], field );
        if ( strcmp ( nw->show[i], field ) == 0 )
        {
           return 1;
        }
        i++;
    }

    return 0;
}

news* fetchNews ( news** list, char* query )
{
    int i = 0;

    while ( list[i] != NULL )
    {
        if ( strcmp ( query, list[i]->name ) == 0 )
        {
            return list[i];
        }
        i++;
    }

    return NULL;
}
     

/* =============================================================================== */ 
void free_split(char** vector) 
{
    int index = 0;
    
    while ( vector[index] != NULL ) 
    {
        free( vector[index] );
        index++;
    }
    
    free( vector );
}

/* =============================================================================== */ 
char* str_dup ( char* str ) 
{
    char *d = (char*) calloc( strlen( str )+1, sizeof(char) );  
    if ( d == NULL ) return NULL;          
    strcpy ( d ,str );                    

    return d;                           
}

/* =============================================================================== */ 








