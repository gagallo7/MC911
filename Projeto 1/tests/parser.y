%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
FILE* fp ;

char* concat ( int, ... );

#define COMPILE(X) fp = fopen ("teste.html", "a"); fprintf ( fp, (X) ); fclose (fp)

#define COMPILE2(X,...) fp = fopen ("teste.html", "a"); fprintf ( fp, (X), ##__VA_ARGS__ ); fclose (fp)

#define LOG(X,...) fp = fopen ("debug.err", "a"); \
                   fprintf ( fp, (X), ##__VA_ARGS__ ); \
                   fprintf ( fp, "\n" ); \
                   fclose (fp)
%}

%union {
    char* str;
    int intval;
}

%token <str> T_NAME
%token <intval> T_NUMBER
%token T_NEWSPAPER
%token T_DATE
%token T_TITLE
%token T_STRUCTURE
%token T_TEXT
%token T_AUTHOR
%token T_COL
%token T_FORMAT
%token T_IMAGE
%token T_SHOW
%token T_STRING
%token T_SOURCE
%token T_ABSTRACT
%token T_WHITESPACE

%type <str> date T_STRING title T_UNFORMATTED T_FORMAT

%start newspaper

%error-verbose

%%

/* LOG ( ... ) recebe e transforma os argumentos num printf para debug.err */
/* COMPILE2 ( ... ) faz o mesmo que o log, mas já deixa num formato html
no arquivo teste.html */
newspaper: T_NEWSPAPER 
            { fp = fopen ("teste.html","w"); 
              LOG ( "T_NEWSPAPER" );   
            }

            '{' 
            { 
                COMPILE("<html>\n"); 
            }

             desc 
            {  LOG ( "desc ready. Waiting structure..." ); }

             structure
            {  LOG ( "structure ready" ); }

             news_list '}' 
            { 
                 printf ("Completed!\n");
                 COMPILE("\n</html>\n");
            }
;

T_UNFORMATTED:
             T_STRING                   { $$ = strdup ($1); }
             | T_FORMAT                  { $$ = strdup ($1); }
             | T_UNFORMATTED T_STRING   { $$ = concat ( 2, $1, $2 ); }
             | T_UNFORMATTED T_FORMAT   { $$ = concat ( 2, $1, $2 ); }

news_list:
         news 
         {
            LOG ("news");
         }
         | news_list news 
         {
            LOG ("list + news"); 
         }
;

news:
    T_NAME 
    { 
        LOG ( "---------------News >> %s", $1 ); 
    }
    '{' f_list 
    {
        LOG ( "Waiting structure" );
    }
    newsStructure '}'
;

/* lista com nomes de componentes de uma noticia */
f_list_comma:
          f_names
          | f_list_comma ',' f_names
;

/* possiveis valores para nomes de lista */
f_names:
        T_TITLE             { LOG ("fname_required\ttitle"); }
        | T_AUTHOR          { LOG ("fname_required\tauthor"); }
        | T_ABSTRACT        { LOG ("fname_required\tabstract"); }
        | T_DATE
        | T_IMAGE
        | T_SOURCE
        | T_TEXT
;

/* lista com as ESPECIFICACOES de um componente de uma noticia */
/* tratarei os obrigatorios com funcoes em C, o que acha? */
f_list:
          f_opt
          | f_list f_opt
;

/* especificacoes de cada campo */
f_opt:
     title         { LOG ("f_required\ttitle"); }
     | author      { LOG ("f_required\tauthor"); }
     | abstract    { LOG ("f_required\tabstract"); }
     | date          { LOG ("f_opt\tdate"); }
     | image         { LOG ("f_opt\timage"); }
     | source        { LOG ("f_opt\tsource"); }
     | text          { LOG ("f_opt\ttext"); }
;


desc: 
    title date
        {
            COMPILE2 ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $1, $2 );
        }
    | date title
        { 
            COMPILE2 ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $2, $1 ); 
        }
;

date: 
    T_DATE  '=' T_UNFORMATTED
        {
            printf ("Date = %s\n", $3); 
            $$ = $3;
        }
   ;


title: 
     T_TITLE '=' T_UNFORMATTED 
             { 
                $$ = strdup ($3);
             }
;

structure: 
         T_STRUCTURE '{' col 
               {
                   LOG ( "Waiting show..." ) ; 
               }
         show '}'
;

newsStructure: 
         T_STRUCTURE '{' col 
           {
                 LOG ( "Waiting newsShow..." ) ;
            }
            showNews 
           {
                LOG ( "showNews parsed!" ) ;
            }
         '}'
;

image:
     T_IMAGE '=' T_UNFORMATTED 
;

source:
       T_SOURCE '=' T_UNFORMATTED
;

text:
    T_TEXT '=' T_UNFORMATTED
;

abstract:
        T_ABSTRACT '=' T_UNFORMATTED
;

author:
      T_AUTHOR '=' T_UNFORMATTED
      ;

col: 
   T_COL '=' T_NUMBER
   { LOG ( "col = %d", $3 ) ; }
;

show: 
    T_SHOW '=' sComponents
;

showNews:
     T_SHOW
       {
            LOG ( "T_SHOW" ) ;
        }
     '='
       {
            LOG ( "=" ) ;
        }
     f_list_comma
       {
            LOG ( "f_list" ) ;
        }
;

sComponents: 
           T_NAME
           | sComponents ',' T_NAME
;

%%

int yyerror(const char* errmsg)
{
	printf("\n*** Erro: %s\n", errmsg);
}
 
int yywrap(void) { return 1; }

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
 
int main(int argc, char** argv)
{
     yyparse();
     return 0;
}
