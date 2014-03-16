#include "utility.h"


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

void sub_str(char* src, char* dst, int howMany) 
{
    int i;

    for (i = 0; i < howMany; i++) 
    {
        if (src[i] == '\0') 
            break;

        dst[i] = src[i];
    }

    dst[i] = '\0';
}


void split_str(char* str, char* delimiter, char** vector) 
{
    int delim_len = strlen(delimiter);
    int i = 0;
    int str_len = strlen(str);
    int index = 0, sub_index = 0;
    char* aux;

    // Alocando memoria pra aux
    aux = (char*) calloc ( (delim_len+1), sizeof(char) );

    while (i < str_len) 
    {
        sub_str(str+i, aux, delim_len);

        if ( strcmp(aux, delimiter) != 0 ) 
        {
            vector[index][sub_index] = str[i];
            i++;
            sub_index++;
            continue;
        }

        vector[index][sub_index] = '\0';
        i += delim_len;
        index++;
        sub_index = 0;
    }

    // Liberando memoria alocada
    free(aux);
}










