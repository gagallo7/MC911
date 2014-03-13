%{
#include "parser.h"
// buf e' uma string auxiliar para formar
// textos que contenham aspas \"
char buf[50000];
// s e' o ponteiro que vai escrevendo no buffer
char *s;
/*TEXT \"[\-0-9a-zá-ú \n\t\r]*\"*/
//TEXT \"[^"\n(\\")]*[\"\n]
%}

%x STRING
%x COMMENT
NAME [a-z][a-z0-9_]*
NUMBER [0-9]*
WHITESPACE  [ \n\t\r]+
ERROR (^\")*

%option case-insensitive

%%

\/\/              { BEGIN COMMENT; }
<COMMENT>\n     { BEGIN 0; }

\"               { 
                    BEGIN STRING; 
                    s= buf; 
                 } 
<STRING>\\n     { *s++ = '\n'; } 
<STRING>\\t     { *s++ = '\t'; } 
<STRING>\\\"    { *s++ = '\"'; } 
<STRING>\"  { 
                *s = 0; 
                BEGIN 0; 
                printf("\n'\e[0;32m'Encontrado: '%s''\e[0m'\n", buf); 
                yylval.str = (char* ) strdup (buf);
                return T_STRING;
            } 
<STRING>.   {*s++ = yytext [0];} 


"{"       return '{';
"}"       return '}';
"="       return '=';
"\""       return 'a';
","       return ',';
"newspaper" return T_NEWSPAPER;
"date"    return T_DATE;
"title"    return T_TITLE;
"structure" return T_STRUCTURE;
"author" return T_AUTHOR;
"text" return T_TEXT;
"col" return T_COL;
"show" return T_SHOW;
"source" return T_SOURCE;
"image" return T_IMAGE;
"abstract" return T_ABSTRACT;


{NAME} {	
	yylval.str = (char *) strdup(yytext);
    printf ( "Format: %s\n", yytext );
	return T_NAME;
}

{NUMBER} {	
	yylval.intval = (int *) atoi(yytext);
	return T_NUMBER;
}

{WHITESPACE} ;

{ERROR} {
	printf ("Caracter %c (ASCII=%d) inesperado\n", yytext[0], yytext[0]);
}
