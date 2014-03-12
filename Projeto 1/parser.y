%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
FILE* fp ;

#define COMPILE(X) fp = fopen ("teste.html", "a"); fprintf ( fp, (X) ); fclose (fp)
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
%token T_WHITESPACE

/*%type <> title date show*/

%type <str> date T_STRING

%start newspaper

%error-verbose

%%

newspaper: T_NEWSPAPER { fp = fopen ("teste.html","w"); }
'{' { COMPILE("<html>\n"); }
 desc structure '}' { printf ("newsp\n"); }
;

desc: 
    title date
    | date title
;

date: 
    T_DATE  '=' T_STRING { printf ("Date = %s\n", $3); }
   ;


title: 
     T_TITLE '=' T_STRING
;

structure: 
         T_STRUCTURE '{' col show '}'
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
