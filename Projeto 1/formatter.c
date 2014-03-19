#include "formatter.h"

char buffer [10000];

// chamada para bullet: bgay ( root, tail, '*', "ul" )
char* bgay ( char* root, char* tail, char type, char tag[], int firstBlood )
{
    int nv = 0;
    int next = 0;
    int diff;
    char cc[3];
    cc[2] = 0;
    cc[1] = type;
    cc[0] = '\n';
    
    if ( strncmp ( tail, cc, 2 ) == 0 || firstBlood )
    {
        if ( !firstBlood )
        {
            tail++;
        }
    }
    else
    {
        return tail;
    }

    while ( *tail == type )
    {
        tail++;
        nv++;

        while ( *tail == type )
        {
            nv++;
            tail++;
        }

        diff = next - nv;

        while ( diff < 0 )
        {
            root = strcat ( root, "<" );
            root = strcat ( root, tag );
            root = strcat ( root, ">\n" );
            diff++;
        }

        while ( diff > 0 )
        {
            root = strcat ( root, "</" );
            root = strcat ( root, tag );
            root = strcat ( root, ">\n" );
            diff--;
        }

        root = strcat ( root, "<li>" );

        while ( *tail == ' ' )
        {
            tail++;
        }

        while ( *tail != '\n' && *tail != 0 )
        {
            strncat ( root, tail, 1 );
            tail++;
        }

        root = strcat ( root, "</li>\n" );
        
        next = nv;
        nv = 0;
        tail++;
    }

    while ( next )
    {
        root = strcat ( root, "</" );
        root = strcat ( root, tag );
        root = strcat ( root, ">\n" );
        next--;
    }
    return tail;
}


char* lists ( char* root, char* tail, char type, char tag[], char innerTag[], int* level )
{
    char aux [20];
    char* search = tail;
    int newLvl = 0;
    int diff;

    while ( *search == type )
    {
        search++;
        newLvl++;
    }

    diff = newLvl - *level;

    if ( search == tail )
    {
        return tail;
    }
    else
    {
        tail = search-1;
    }

    while ( diff-- )
    {
        root = strcat ( root, "<" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
    }

    tail = subsRepeat ( root, tail, "*", "\n", innerTag, "" );

    while ( diff++ )
    {
        root = strcat ( root, "</" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
    }


    *level = newLvl;

    return tail;
}

char* simpleSubs ( char* root, char* tail, char start[], char tag[], char extra[] )
{
    int n = strlen ( start );
    char tostr [2];
    tostr[1] = '\0';
    int repeats = 0;

    while ( strncmp ( tail, start, n ) == 0 )
    {
        tail += n;
        if ( strlen ( extra ) ) 
        {
            root = strcat ( root, extra );
        }
        root = strcat ( root, tag );
        repeats++;
    }

    return tail;
}

char* lists2 ( char* root, char* tail, char start[], char end[], char tag[], char extra[], int* level )
{
    int n = strlen ( start );
    char tostr [2];
    tostr[1] = '\0';
    int repeats = 0;
    int match = 0;
    int diff, current = 0;

            diff = current - *level;
            if ( diff < 0 )
            if ( strlen ( extra ) ) 
            {
                printf ( "diff = %d\n", diff );
                while ( diff++ < 0 )
                {
                root = strcat ( root, "</" );
                root = strcat ( root, extra );
                root = strcat ( root, ">" );
                }
            }

    if ( strncmp ( tail, start, n ) == 0 )
    {
        match = 1;
        tail += n;
        current++;
        while ( strncmp ( tail, start, n ) == 0 )
        {
            current++;
            tail += n;
        }
        if ( strlen ( extra ) ) 
        {
            diff = current - *level;
            repeats = diff;
            while ( diff-- > 0 )
            {
            root = strcat ( root, "<" );
            root = strcat ( root, extra );
            root = strcat ( root, ">" );
            }
        }
        root = strcat ( root, "<" );
        root = strcat ( root, tag );
        root = strcat ( root, ">" );
    }

    if ( match )
    {
        n = strlen ( end );
        while ( strncmp ( tail, end, n ) != 0 )
        {
            tostr[0] = *tail;
            strcat ( root, tostr );
            tail++;
        }

        tail += n;

            root = strcat ( root, "</" );
            root = strcat ( root, tag );
            root = strcat ( root, ">" );

    }

    return tail;
}

char* lists3 ( char* root, char* tail, char start[], char end[], char tag[], char extra[], int* guls )
{
    int n = strlen ( start );
    char tostr [2];
    tostr[1] = '\0';
    int repeats = 0;
    int match = 0;
    int step = 3 + strlen ( extra );
    int luls = 0 ;
    int diff;
    int nClose;
    char* leveller = tail;

    char close[20] = "";

    strcat ( close, "</" );
    strcat ( close, extra );
    strcat ( close, ">" );

    nClose = strlen ( close );

    while ( strncmp ( leveller, start, n ) == 0 )
    {
        luls++;
    }
    
    diff = luls - *guls;

    if ( diff > 0 )
    {
        leveller = root [ strlen (root) ] - nClose * guls[0];
    }
    else
    {
        leveller = root [ strlen (root) ] - nClose * luls;
    }

    *leveller = 0;

    /*
    while ( diff++ < 0 && leveller > root+step && strncmp ( leveller, close, nClose ) )
    {
        *leveller = 0;
     //   tail -= step;
    }
    */

    while ( strncmp ( tail, start, n ) == 0 )
    {
        match = 1;
        tail += n;
        diff = luls - *guls;
        if ( strlen ( extra ) && diff-- > 0 ) 
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

    if ( match )
    {
        n = strlen ( end );
        while ( strncmp ( tail, end, n ) != 0 )
        {
            printf ( "\nroot:%s\n", root );
            tostr[0] = *tail;
            strcat ( root, tostr );
            tail++;
        }

        tail += n;

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
    }

    return tail;
}

char* subsRepeat ( char* root, char* tail, char start[], char end[], char tag[], char extra[] )
{
    int n = strlen ( start );
    char tostr [2];
    tostr[1] = '\0';
    int repeats = 0;
    int match = 0;

    while ( strncmp ( tail, start, n ) == 0 )
    {
        match = 1;
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

    if ( match )
    {
        n = strlen ( end );
        while ( strncmp ( tail, end, n ) != 0 )
        {
            printf ( "\nroot:%s\n", root );
            tostr[0] = *tail;
            strcat ( root, tostr );
            tail++;
        }

        tail += n;

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
    }

    return tail;
}

char* subs ( char* root, char* tail, char start[], char end[], char tag[] )
{
    int n = strlen ( start );
    char tostr [2];
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

char* push_back ( char* fmt, char s[], char* root, int* len )
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

char* push_back_char ( char* fmt, char s, char* root, int* len )
{
    (*len)++;
    root = (char *) realloc ( root, (*len) * sizeof (char) );
    printf ( "len:%d\n", *len );

    root[*len-1] = s;
    root[*len] = 0;

    fmt = &root[*len];

    return fmt;
}

/* Gerador de hyperlinks */
char* hyperlink ( char* root, char* tail )
{
    //char cc[2];
    //cc[1] = '\0';
    char http[] = "http://";
    char www[] = "www";
    //int n = strlen ( http );
    char* aux;
    int validURL = 0;
    int isWWW = 0;
    char url[1] = "";

    /* Primeiro caractere da formatação */
    if ( *tail == '[' )
    {
        tail++;

        /* "[[" requer uma outra formatação */
        if ( *tail == '[' )
        {
            tail = hyperlink2 ( root, ++tail );
            return tail;
        }

        /* Caso seja somente "[", continue */

        /* Pulando espaços */
        while ( *tail == ' ' ) tail++;

        /* Verificando se a URL começa com http:// ou www */
        aux = strstr ( tail, http );

        if ( aux == NULL )
        {
            aux = strstr ( tail, www );
            isWWW = 1;
        }

        if ( aux != NULL )
        {
            /* Montando HTML */
            root = strcat ( root, "<a href=\"" );
            if ( isWWW )
            {
                strcat ( root, "http://" );
            }


            /* Copiando corpo da URL */
            while ( *tail != ' ' && *tail != '|' && *tail != ']' )
            {
                strncat ( url, tail, 1 );
                strncat ( root, tail++, 1 );
            }

            /* Fechando tag <a> */
            root = strcat ( root, "\">" );

            /* Procurando separador '|' */
            while ( *tail == ' ' || *tail == '|' )
            {
                if ( *tail == '|' )
                {
                    validURL = 1;
                }
                tail++;
            }

            /* Se a URL for inválida */
            if ( !validURL && *(tail) != ']' )
            {
                printf ( "Erro hypL2: url mal feita. Esperando por '|', encontrou-se: \"%s\"\n", strtok ( tail, "|\n" ) );
                exit (1);
            }

            /*
            */

            /* Procurando fim da URL */
            while ( *tail != ']' )
            {
                strncat ( root, tail++, 1 );
            }

            tail++;

            //strcat ( root, strtok_r ( tail, "]", &tail ) );

            /* Fechando tag </a> */
            if ( !validURL )
            {
                strcat ( root, url );
            }
            root = strcat ( root, "</a>" );

        }
        else
        {
            printf ( "Erro: url mal feita. Esperando por \"%s\" ou \"%s\", encontrou-se: \"%s\"\n", http, www, strtok ( tail, "|\n" ) );
            exit (1);
        }
        /*
        */
    }

    return tail;
}

char* hyperlink2 ( char* root, char* tail )
{
        int validURL = 0;
    /* Pulando espaços */
    while ( *tail == ' ' ) tail++;

    /* Montando HTML */
    root = strcat ( root, "<img src=\"" );

    /* Copiando corpo da URL */
    while ( *tail != ' ' && *tail != '|' && *tail != ']' )
    {
        strncat ( root, tail++, 1 );
    }

    /* Fechando tag <a> */
    root = strcat ( root, "\" alt=\"" );

    /* Procurando separador '|' */
    while ( *tail == ' ' || *tail == '|' )
    {
        if ( *tail == '|' )
        {
            validURL = 1;
        }
        tail++;
        if ( *tail == ']' )
        {
            validURL = 1;
            break;
        }
    }


    /* Se a URL for inválida */
    if ( !validURL && *tail == ']' )
    {
        printf ( "Erro hyp2: url mal feita. Esperando por '|', encontrou-se: \"%s\"\n", strtok ( tail, "|\n" ) );
        exit (1);
    }

    /*
    */

    /* Procurando fim da URL */
    //while ( *tail != ']' )
    while ( strncmp ( tail, "]]", 2 ) != 0 )
    {
        strncat ( root, tail++, 1 );
    }

    tail += 2;

    //strcat ( root, strtok_r ( tail, "]", &tail ) );

    /* Fechando tag </a> */
    root = strcat ( root, "\">" );

    /*
    */

    return tail;
}

char* format ( char* s )
{
    char* c;
    char open[10];
    strcpy ( open, "" );
    char* fmt = strdup ("");
    char* root = fmt;
    char cc[2];
    cc[1] = '\0';
    //int len = 0;
    int astLevel = 0;
    c = s;

    c = bgay ( root, c, '*', "ul", 1 );
    c = bgay ( root, c, '#', "ol", 1 );
    while ( *c != 0 )
    {
        //c = lists ( root, c, '*', "ul", "li", &astLevel );
        //c = lists3 ( root, c, "*", "\n", "li", "ul", &astLevel );
        c = bgay ( root, c, '*', "ul", 0 );
        c = bgay ( root, c, '#', "ol", 0 );
        /*
        c = subsRepeat ( root, c, "*", "\n", "li", "ul" );
        c = subsRepeat ( root, c, "#", "\n", "li", "ol" );
        */

        c = hyperlink ( root, c );
        // simpleSubs ( char* root, char* tail, char start[], char tag[], char extra[] )
//        c = simpleSubs ( root, c, "<br />", "\n", "" );
        c = simpleSubs ( root, c, "\\\"", "\"", "" );
        //c = simpleSubs ( root, c, "\n\n", "<br />", "" );

        //subs3 ( char* tail, char open[], char type, char lvl1[], char lvl2[], char lvl3[] )
        c = subs3 ( root, c, open, '=', "h4", "h3", "h2" );
        //subs ( char* root, char* tail, char start[], char end[], char tag[] )
        //c = subs ( root, c, "**", "\n", "li" );
        c = simpleSubs ( root, c, "\n:", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "" );
        //c = subsRepeat ( root, c, "\n:", "\n", "dd", "dl" );
        c = subsRepeat ( root, c, " :", "\n", "dd", "dl" );
        c = subsRepeat ( root, c, "'''''", "'''''", "b", "i" );
        c = subsRepeat ( root, c, "'''", "'''", "b", "" );
        c = subsRepeat ( root, c, "''", "''", "i", "" );
        /*
        */

        //*fmt = *c;

        //len = strlen ( root );
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
    char tst [10000];
    FILE* fp;
    char* fmt;

    fp = fopen ( "format.html", "w" );

    scanf ( "%[^\&]s", str );

//    str [ strlen (str) - 1 ] = 0;

    fmt = format ( str );

    printf ( "\nInput:%s\nFormatted:%s\n", str, fmt );

    /*
    fprintf ( fp, "%s", fmt );
    */

    while ( *fmt != 0 )
    {
        fprintf ( fp, "%c", *fmt );
        fmt++;
    }

    fclose ( fp );

    return 0;
}
