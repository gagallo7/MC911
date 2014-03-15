#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/* concatena uma string e retorna um ponteiro
 * para o fim da mesma */

char*  push_back ( char* fmt, char s[], char* root, int* len )
{
    int i = 0;
    (*len)++;
    root = (char *) realloc ( root, (*len) * sizeof (char) );
    fmt++;

    while ( s [i] ) 
    {
        *fmt = s[i];
        fmt++;
        i++;
    }

    return fmt;
}



char* format ( char* s )
{
    char* c;
    c = s;
    char* open = strdup ("");
    char* cmp;
    char* fmt = strdup (s);
    int len = 0;

    while ( *c != 0 )
    {
        len++;
        if ( *c == '=' )
        {
            cmp = strdup ( "=" );
            c++;
            if ( c[0] == '=' )
            {
                c++;
                cmp = strdup ( "==" );
                /* === */
                if ( c[0] == '=' )
                {
                    c++;
                    cmp = strdup ( "===" );
                    /* fim da formatacao */
                    if ( strcmp ( open, cmp ) == 0 )
                    {
                        c = push_back ( c, "</h2>" , fmt, &len );
                        open = strdup ("");
                    }
                    /* inicio da formatacao */       
                    else
                    {
                        open = strdup (cmp);
                        c = push_back ( c, "<h2>" , fmt, &len );
                    }
                }
                /* == */
                else
                {
                    /* fim da formatacao */
                    if ( strcmp ( open, cmp ) == 0 )
                    {
                        c = push_back ( c, "</h3>" , fmt, &len );
                        open = strdup ("");
                    }
                    /* inicio da formatacao */       
                    else
                    {
                        open = strdup (cmp);
                        c = push_back ( c,  "<h3>" , fmt, &len );
                    }
                }
            }
            /* = */
            else
            {
                /* fim da formatacao */
                if ( strcmp ( open, cmp ) == 0 )
                {
                    c = push_back ( c, "</h4>" , fmt, &len );
                    open = strdup ("");
                }
                /* inicio da formatacao */       
                else
                {
                    open = strdup (cmp);
                    c = push_back ( c,  "<h4>" , fmt, &len );
                }
            }
        }
        *fmt = *c;
        fmt++;
        c++;
    }

    return fmt;
}


int main ()
{
    char str [10000];
    char* fmt;

    scanf ( " %s", str );

    fmt = format ( str );

    printf ( "\nFormatted:%s\n", fmt );

    return 0;
}
