%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include "checker.h"
#include "utility.h"

%}

%union {
    char* str;
    int intval;
}

%token T_NAME 
%token T_NUMBER
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

%type <str> T_STRING T_NEWSPAPER T_DATE date title T_TITLE news f_list f_opt image T_IMAGE source T_SOURCE T_NAME T_NUMBER
%type <str> text T_TEXT abstract T_ABSTRACT author T_AUTHOR col T_COL show T_SHOW sComponents f_list_comma f_names

%start newspaper

%error-verbose

%%

/* newspaper: T_NEWSPAPER '{' desc structure news_list '}' */
newspaper: T_NEWSPAPER 
         { 
            LOG ( "Reconheci T_NEWSPAPER" );   
         }

         '{' 
         { 
            COMPILE("<html>\n"); 
         }

         desc 
         {  
            LOG ( "desc ready" ); 
         }

         structure
         {  LOG ( "structure ready" ); }

         news_list '}' 
         { 
            printf ("Completed!\n");
            COMPILE("\n<html>\n");
            LOG ( "Completed!" );
         }
;

/* news_list: news
            | news_list news 
*/
news_list: news 
         {
            LOG ("news");
         }

         | news_list news 
         {
            LOG ("list + news"); 
         }
;

/* news: T_NAME '{' f_list newsStructure '}' */
news: T_NAME '{' f_list 
    {
        int i = 0;
        char** fields; 
        char** values;

        LOG( "news name = %s\n", $1);

        // Alocando memoria pra fields e values
        fields = (char**) calloc ( 7, sizeof(char*) );
        for (i = 0; i < 7; i++) 
        {
            fields[i] = (char*) calloc ( 512,  sizeof(char) );
        }

        values = (char**) calloc ( 7, sizeof(char*) );
        for (i = 0; i < 7; i++) 
        {
            values[i] = (char*) calloc ( 32000, sizeof(char) );
        }

        LOG ( "f_list sent to checker: %s", $3 );

        // Verificando a validade
        if ( check_f_list($3, fields, values) ) 
        {
            printf("f_list is ok!\n");

            LOG ("\n CAMPOS LIDOS:");
            for (i = 0; i < 7; i++) 
            {
                if ( fields[i] != 0 ) 
                    LOG( "%s", fields[i] );      
            }

        } else 
        {
            printf("Checker error!\n");
            yyerror("ERROR: Some mandatory fields is missing.\n");
        }

        // Liberando a memoria alocada
        LOG ( "Liberando memoria alocada para validacao...\n" );

        for (i = 0; i < 7; i++) 
        {
            free( fields[i] );
            free( values[i] );
        } 

        free(fields);
        free(values);
    }
    
    newsStructure '}'
;

/* f_list_comma: f_names
               | f_list_comma ',' f_names
*/
f_list_comma: f_names 
            {
                $$ = $1;
            }

            | f_list_comma ',' f_names 
            {
                $$ = concat(3, $1, S3, $3);
            }
;

/* possiveis valores para nomes de lista */
f_names: T_TITLE 
       {
            $$ = "title";
       }

       | T_AUTHOR 
       {
            $$ = "author";
       }

       | T_ABSTRACT        
       {
            $$ = "abstract";
       }

       | T_DATE
       {
            $$ = "date";
       }

       | T_IMAGE
       {
            $$ = "image";
       }

       | T_SOURCE
       {
            $$ = "source";
       }

       | T_TEXT
       {
            $$ = "text";
       }

;

/* f_list: f_opt
         | f_list f_opt
*/
f_list: f_opt 
      {
        $$ = $1;
      }

      | f_list f_opt 
      {
        $$ = concat(3, $1, S1, $2);
      }
;

/* especificacoes de cada campo */
f_opt: title        { LOG ("f_required\ttitle");    }
     | author       { LOG ("f_required\tauthor");   }
     | abstract     { LOG ("f_required\tabstract"); }
     | date         { LOG ("f_opt\tdate");          }
     | image        { LOG ("f_opt\timage");         }
     | source       { LOG ("f_opt\tsource");        }
     | text         { LOG ("f_opt\ttext");          }
;


/* desc: title date
       | date title
*/ 
desc: title date 
    {
        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $1, $2 );
        LOG ( "Title = %s \nDate = %s", $1, $2 );
    }
    
    | date title
    { 
        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", $2, $1 ); 
        LOG ( "Date = %s \nTitle = %s", $1, $2 );
    }
;


date: T_DATE  '=' T_STRING 
    {
        $$ = concat(3, "date", S2, $3);
    }
;


title: T_TITLE '=' T_STRING 
     { 
        $$ = concat(3, "title", S2, $3);
     }
;


/* structure: T_STRUCTURE '{' col show '}' */
structure: T_STRUCTURE '{' col 
         {
            LOG ( "structure possui col = %s", $3 ) ;
         }
         
         show '}' 
         {
            /*LOG ( "show = %s", $1);*/
         }
;


/* newsStructure: T_STRUCTURE '{' col showNews '}' */
newsStructure: T_STRUCTURE '{' col 
             {
                LOG ( "structure possui col = %s", $3 ) ;
             }
            
             showNews 
             {
                /*LOG ( "showNews = %s", $1 ) ;*/
             }
         
             '}'
;


image: T_IMAGE '=' T_STRING 
     {
        $$ = concat(3, "image", S2, $3);
     } 
;


source: T_SOURCE '=' T_STRING
      {
        $$ = concat(3, "source", S2, $3);
      } 
;


text: T_TEXT '=' T_STRING
    {
        $$ = concat(3, "text", S2, $3);
    } 
;


abstract: T_ABSTRACT '=' T_STRING
        {
            $$ = concat(3, "abstract", S2, $3);
        } 
;


author: T_AUTHOR '=' T_STRING
      {
        $$ = concat(3, "author", S2, $3);
      } 
;


col: T_COL '=' T_NUMBER
   {
        LOG ( "Dentro do col. T_NUMBER = %s", $3);
        $$ = concat(3, "col", S2, $3);
   } 
;


show: T_SHOW '=' sComponents
    {
        $$ = concat(3, "show", S2, $3);
    } 
;


/* showNews: T_SHOW '=' f_list_comma */
showNews: T_SHOW '=' f_list_comma
        {
            /*$$ = concat(3, "show", S2, $3);*/
        }
;



/* sComponents: T_NAME
              | sComponents ',' T_NAME
*/
sComponents: T_NAME 
           {
                $$ = $1;
           }

           | sComponents ',' T_NAME 
           {
                $$ = concat(3, $1, S3, $3);
           }
;

%%

int yyerror(const char* errmsg) { printf("\n*** Erro: %s\n", errmsg); } 
int yywrap(void) { return 1; } 
extern int yy_flex_debug;

int main(int argc, char** argv)
{
     yyparse();
     return 0;
}
