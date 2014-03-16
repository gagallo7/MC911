#include <stdio.h>
#include <string.h>
#include <stdlib.h>

char* lists ( char* root, char* tail, char* lvl1, char* lvl2 )
{
    if ( tail == '*' )
    {
    }

    return tail;
}

char* simpleSubs ( char* root, char* tail, char start[], char tag[], char extra[] )
{
    int n = strlen ( start );
    char* tostr [2];
    tostr[1] = '\0';
    int repeats = 0;

    while ( strncmp ( tail, start, n ) == 0 )
    {
        tail += n;
        if ( strlen ( extra ) ) 
        {
            root = strcat ( root, "<" );
            root = strcat ( root, extra );
            root = strcat ( root, ">" );
        }
        root = strcat ( root, "<" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
        repeats++;
    }

    return tail;
}

char* subsRepeat ( char* root, char* tail, char start[], char end[], char tag[], char extra[] )
{
    int n = strlen ( start );
    char* tostr [2];
    tostr[1] = '\0';
    int repeats = 0;

    while ( strncmp ( tail, start, n ) == 0 )
    {
        tail += n;
        if ( strlen ( extra ) ) 
        {
            root = strcat ( root, "<" );
            root = strcat ( root, extra );
            root = strcat ( root, ">" );
        }
        root = strcat ( root, "<" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
        repeats++;
    }

    n = strlen ( end );
    while ( strncmp ( tail, end, n ) != 0 )
    {
        printf ( "\nroot:%s\n", root );
        tostr[0] = *tail;
        strcat ( root, tostr );
        tail++;
    }

    while ( repeats-- )
    {
        root = strcat ( root, "</" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
        if ( strlen ( extra ) ) 
        {
            root = strcat ( root, "</" );
            root = strcat ( root, extra );
            root = strcat ( root, ">" );
        }
    }

    return tail;
}

char* subs ( char* root, char* tail, char start[], char end[], char tag[] )
{
    int n = strlen ( start );
    char* tostr [2];
    tostr[1] = '\0';

    if ( strncmp ( tail, start, n ) == 0 )
    {
        tail += n;
        root = strcat ( root, "<" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );

        n = strlen ( end );
        while ( strncmp ( tail, end, n ) != 0 )
        {
            printf ( "\nroot:%s\n", root );
            tostr[0] = *tail;
            strcat ( root, tostr );
            tail++;
        }

        root = strcat ( root, "</" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
    }

    return tail;
}


char* subs3 ( char* root, char* tail, char open[], char type, char lvl1[], char lvl2[], char lvl3[] )
{
    char tag1[2], tag2[3], tag3[4];
    tag1[1] = '\0';
    tag1[0] = type;

    tag2[2] = '\0';
    tag2[1] = type;
    tag2[0] = type;

    tag3[3] = '\0';
    tag3[2] = type;
    tag3[1] = type;
    tag3[0] = type;

    if ( *tail == type )
    {
        tail++;
        if ( tail[0] == type )
        {
            tail++;
            /* === */
            if ( tail[0] == type )
            {
                tail++;
                /* fim da formatacao */
                if ( strcmp ( open, tag3 ) == 0 )
                {
                    strcpy ( open, "" );
                    root = strcat ( root, "</" );
                    root = strcat ( root, lvl3 );
                    root = strcat ( root, ">" );
                }
                /* inicio da formatacao */       
                else
                {
                    strcpy ( open, tag3 );
                    root = strcat ( root, "<" );
                    root = strcat ( root, lvl3 );
                    root = strcat ( root, ">" );
                }
            }
            /* == */
            else
            {
                /* fim da formatacao */
                if ( strcmp ( open, tag2 ) == 0 )
                {
                    strcpy ( open, "" );
                    root = strcat ( root, "</" );
                    root = strcat ( root, lvl2 );
                    root = strcat ( root, ">" );
                }
                /* inicio da formatacao */       
                else
                {
                    strcpy ( open, tag2 );
                    root = strcat ( root, "<" );
                    root = strcat ( root, lvl2 );
                    root = strcat ( root, ">" );
                }
            }
        }
        /* = */
        else
        {
            /* = */
            printf ( "open:%s\n", open );
            /* fim da formatacao */
            if ( strcmp ( open, tag1 ) == 0 )
            {
                //fmt = push_back ( fmt, "</h4>" , root, &len );
                root = strcat ( root, "</" );
                root = strcat ( root, lvl1 );
                root = strcat ( root, ">" );
                strcpy ( open, "" );
            }
            /* inicio da formatacao */       
            else
            {
                strcpy ( open, tag1 );
                root = strcat ( root, "<" );
                root = strcat ( root, lvl1 );
                root = strcat ( root, ">" );
                //fmt = push_back ( fmt,  lvl3 , root, &len );
            }
        }
    }

    return tail;
}

/* concatena uma string e retorna um ponteiro
 * para o fim da mesma */



char*  push_back ( char* fmt, char s[], char* root, int* len )
{
    int i = 0;
    while ( s [i++] != 0 ) 
    {
        (*len)++;
    }
    root = (char *) realloc ( root, (*len) * sizeof (char) );
    printf ( "len:%d\n", *len );

    i = 0;
    while ( s [i] ) 
    {
        *fmt = s[i];
        fmt++;
        i++;
    }

    fmt++;
    *fmt = 0;

    return fmt;
}

char*  push_back_char ( char* fmt, char s, char* root, int* len )
{
    int i = 0;
    (*len)++;
    root = (char *) realloc ( root, (*len) * sizeof (char) );
    printf ( "len:%d\n", *len );

    root[*len-1] = s;
    root[*len] = 0;

    fmt = &root[*len];

    return fmt;
}

char* clearStr ( char* s )
{
    char* aux;
    if ( s != NULL )
    {
        free ( s );
    }
    aux = (char *) malloc ( 1 * sizeof ( char ) );
    aux[0] = 0;

    return aux;
}

char* format ( char* s )
{
    char* c;
    c = s;
    char open[10];
    strcpy ( open, "" );
    char* cmp;
    char* fmt = strdup ("");
    char* root = fmt;
    char cc[2];
    cc[1] = '\0';
    int len = 0;

    while ( *c != 0 )
    {
        //subs3 ( char* tail, char open[], char type, char lvl1[], char lvl2[], char lvl3[] )
        c = subs3 ( root, c, open, '=', "h4", "h3", "h2" );
        c = subs3 ( root, c, open, '=', "h4", "h3", "h2" );
        //subs ( char* root, char* tail, char start[], char end[], char tag[] )
        //c = subs ( root, c, "**", "\n", "li" );
        c = subsRepeat ( root, c, ":", "\n", "dd", "dl" );
        c = simpleSubs ( root, c, "<br />", "li", "" );
        //*fmt = *c;

        len = strlen ( root );
        //fmt = push_back_char ( fmt, *c, root, &len );
        cc[0] = *c;
        root = strcat ( root, cc );
        c++;
    }

    return root;
}


int main ()
{
    char str [10000];
    char* fmt;

    scanf ( "%[^\"]s", str );

    fmt = format ( str );

    printf ( "\nInput:%s\nFormatted:%s\n", str, fmt );

    return 0;
}
