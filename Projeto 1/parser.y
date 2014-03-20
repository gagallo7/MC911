%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include "checker.h"
#include "utility.h"

// GLOBALS
typedef struct
{
char* name;
int col;         // Numero de colunas da noticia
char** show;     // Ordem com que os campos devem ser imprimidos
char** fields;   // Campos presentes na noticia
char** values;   // Valores associados dos campos

} news;

int web_col;     // Numero de colunas da pagina
char** web_show; // Ordem com que as noticias devem ser imprimidas
news** list;      // Lista de todas as noticias da pagina

%}

%union {
    char* str;
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
%type <str> text T_TEXT abstract T_ABSTRACT author T_AUTHOR col T_COL show T_SHOW sComponents f_list_comma f_names showNews structure

%start newspaper

%error-verbose

%%

/* newspaper: T_NEWSPAPER '{' desc structure news_list '}' */
newspaper: T_NEWSPAPER 
         { 
            LOG ( "Compilando..." );   
            COMPILE("<html>\n"); 

            // Inicializando o vetor de news
            list = (news**) calloc( 1, sizeof(news*) );
            list[0] = (news*) calloc( 1, sizeof(news) );
         }

         '{' desc 
         {  
            LOG ( "Descricao compilada com sucesso!" ); 
         }

         structure
         {  LOG ( "Structure da pagina compilada com sucesso!" ); }

         news_list '}' 
         { 
            LOG( "Gerando o HTML..." );
            
            // Gerando o HTML











            // Fim do processo
            COMPILE("\n</html>");
            LOG ( "Compilacao finalizada com sucesso." );

            // Limpando a memoria das variaveis globais


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
        
        
        int i;
        char** fields; 
        char** values;

        LOG( "news name: %s\n", $1);
        LOG ( "f_list sent to checker: %s", $3 );

        // Verificando a validade
        if ( check_f_list($3, &fields, &values) ) 
        {
            printf("f_list is ok!\n");

            LOG ("\nCAMPOS LIDOS:");
            i = 0;
            while ( fields[i] != NULL ) 
            {
                LOG( "%s", fields[i] );      
                i++;
            }

        } else 
        {
            printf("Checker error!\n");
            yyerror("ERROR: Some mandatory fields is missing.\n");
        }

        // Liberando a memoria alocada
        LOG ( "Liberando memoria alocada para validacao...\n" );

        i = 0;
        while ( fields[i] != NULL ) 
        {
            free( fields[i] );
            free( values[i] );
            i++;
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
        // Realizando split
        char** title_aux = split_str($1, S2);
        char** date_aux = split_str($2, S2);

        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", title_aux[1], date_aux[1] );
        LOG( "\nDESCRICAO:");
        LOG( "------" );
        LOG ( "Title: %s ", title_aux[1] );
        LOG ( "Date: %s ", date_aux[1] );
        LOG( "------\n" );

        // Liberando memoria
        free_split( title_aux );
        free_split( date_aux );
    }
    
    | date title
    { 
        // Realizando split
        char** title_aux = split_str($2, S2);
        char** date_aux = split_str($1, S2);

        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n</HEAD>\n<h1>%s</h1>\n", title_aux[1], date_aux[1] );
        LOG( "\nDESCRICAO:");
        LOG( "------" );
        LOG ( "Title: %s ", title_aux[1] );
        LOG ( "Date: %s ", date_aux[1] );
        LOG( "------\n" );

        // Liberando memoria
        free_split( title_aux );
        free_split( date_aux );
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
            // Realizando split
            char** col_aux = split_str($3, S2);

            web_col = atoi( col_aux[1] );                   // Setting web_col
            LOG( "\nSTRUCTURE DA WEBPAGE:");
            LOG( "------" );
            LOG ( "Col: %d", web_col ) ;

            // Liberando memoria
            free_split( col_aux );
         }
         
         show '}' 
         {
            int i;

            // Realizando split
            char** show_aux[2];
            
            show_aux[0] = split_str( $5, S2 );
            show_aux[1] = split_str( show_aux[0][1], S3 );

            web_show = show_aux[1];                         // Setting web_show
            LOG ( "Show:" ) ;

            i = 0;
            while ( web_show[i] != NULL ) 
            {
                LOG ( "%d.   %s", i+1, web_show[i] ) ;
                i++; 
            }
            LOG( "------\n" );

            // Liberando memoria
            free_split( show_aux[0] );                      // show_aux[1] é web_show, e sera' desalocado posteriormente
         }
;


/* newsStructure: T_STRUCTURE '{' col showNews '}' */
newsStructure: T_STRUCTURE '{' col 
             {
                LOG ( "structure possui col = %s", $3 ) ;
             }
            
             showNews 
             {
                LOG ( "showNews = %s", $5 ) ;
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
            $$ = concat(3, "show", S2, $3);
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
