%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
FILE* fp ;

#define COMPILE(X) fp = fopen ("teste.html", "a"); fprintf ( fp, (X) ); fclose (fp)
#define COMPILE2(X,...) fp = fopen ("teste.html", "a"); fprintf ( fp, (X), ##__VA_ARGS__ ); fclose (fp)
#define COMPILE3(X,...) printf ( (X), ##__VA_ARGS__ )
#define LOG(X,...) fp = fopen ("debug.err", "a"); \
                   fprintf ( fp, (X), ##__VA_ARGS__ ); \
                   fprintf ( fp, "\n" ); \
                   fclose (fp)
%}

%union {
    char* str;
    int* intval;
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
%token T_IMAGE
%token T_SHOW
%token T_STRING
%token T_SOURCE
%token T_ABSTRACT
%token T_WHITESPACE

/*%type <> title date show*/

%type <str> date T_STRING title

%start newspaper

%error-verbose

%%

newspaper: T_NEWSPAPER { fp = fopen ("teste.html","w"); 
                                LOG ( "T_NEWSPAPER" );   
                        }
'{' { COMPILE("<html>\n"); }
 desc structure news_list '}' { 
                                 printf ("Completed!\n");
                                 COMPILE("\n<html>\n");
                               }
;

news_list:
         news                   { LOG ("news"); }
         | news_list ',' news   { LOG ("list + news"); }
;

news:
    T_NAME '{' f_opt_list f_required f_opt_list f_required f_opt_list f_required f_opt_list 
    { LOG ( "Waiting structure" ); }
    structure '}'
;

f_opt_list:
          f_opt
          | f_opt_list f_opt
          |               { LOG ("f_opt\tempty!!!"); }
;

f_opt:
    image
    | date          { LOG ("f_opt\tdate"); }
    | source        { LOG ("f_opt\tsource"); }
    | text          { LOG ("f_opt\ttext"); }
;

f_required:
          title         { LOG ("f_required\ttitle"); }
          | author      { LOG ("f_required\tauthor"); }
          | abstract    { LOG ("f_required\tabstract"); }
;

desc: 
    title date { COMPILE2 ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $1, $2 );
                LOG ( "desc ready" );
                }
    | date title { COMPILE2 ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $2, $1 ); }
;

date: 
    T_DATE  '=' T_STRING { printf ("Date = %s\n", $3); $$ = $3; }
   ;


title: 
     T_TITLE '=' T_STRING { $$ = strdup ($3); }
;

structure: 
         T_STRUCTURE '{' col show '}'
;

image:
     T_IMAGE '=' T_STRING 
;

source:
       T_SOURCE '=' T_STRING
;

text:
    T_TEXT '=' T_STRING
;

abstract:
        T_ABSTRACT '=' T_STRING
;

author:
      T_AUTHOR '=' T_STRING
      ;

col: 
   T_COL '=' T_NUMBER
;

show: 
    T_SHOW '=' sComponents
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
 
 extern int yy_flex_debug;
int main(int argc, char** argv)
{
    yy_flex_debug = 0;
     yyparse();
     return 0;
}
