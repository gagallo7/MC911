%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include "libs/checker.h"
#include "libs/formatter.h"
#include "libs/utility.h"

// GLOBALS

int web_col;     // Numero de colunas da pagina
char** web_show; // Ordem com que as noticias devem ser imprimidas
news** list;     // Lista de ponteiros de todas as noticias da pagina

int index_news;  // Variavel auxiliar
char header [1000];    // Cabeçalho HTML
char* npTitle;          // Título do jornal

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

            list = (news**) calloc( 1, sizeof(news*) );         // Inicializando o vetor de news
            index_news = 0;                                     // Posicao inicial no vetor de news
         }

         '{' desc 
         {  
            LOG ( "Descricao compilada com sucesso!" ); 
         }

         structure
         {  LOG ( "Structure da pagina compilada com sucesso!" ); }

         news_list '}' 
         { 
            int i;
            int j;
            char* aux;        // Variável auxiliar para otimização
            char* Title;
            news* nextNews;
            char* title = (char* ) calloc ( 200, sizeof ( char ) );
            char* filename = (char* ) calloc ( 200, sizeof ( char ) );

            // Inserindo cauda na lista de noticias
            list = (news**) realloc( list, sizeof(news*) * (index_news+1) ); 
            list[index_news] = NULL;
            
            // Gerando o HTML
            LOG( "Gerando o HTML..." );

            i = 0;
            j = 0;

            // Varrendo a lista de noticias que devem ser impressas
            while ( web_show[i] != NULL )
            {
                // nextNews aponta para noticia a ser impressa
                nextNews = fetchNews ( list, web_show[i] );

                // Conferindo se a noticia existe
                if ( nextNews == NULL )
                {
                    LOG ( "Noticia %s não encontrada!", web_show[i] );
                    i++;
                    continue;
                }

                LOG ( "Montando a notícia %s", nextNews->name );
                // Gerando tag div para encapsular a notícia
                // Com o tamanho da coluna já definido
                //COMPILE ( "\n<div style=\"float: left; width: %d%;\">\n"
                COMPILE ( "\n<div class=\"news\" style=\"width: %d%;\">\n"
                , (nextNews->col)*89/web_col
                        );

                // Título da notícia com hyperlink para possível texto completo
                aux = fetchField ( nextNews, "title" );

                // Conferindo a priori se há texto completo
                if ( fetchField ( nextNews, "text" ) > 0 )
                {
                    LOG ( "Texto completo encontrado, gerando arquivo separado." );

                    // Formatando título com hyperlink
                    sprintf ( title, "<h3><a href=\"noticias/%s.html\">%s</a></h3>\n", nextNews->name, aux );
                    // Encontrando caminho da notícia
                    sprintf ( filename, "noticias/%s.html", nextNews->name );
                Title = fetchField ( nextNews, "title" );

                    aux = format ( fetchField ( nextNews, "text" ) );
                    //LOG ( "Testando formatter: %s", format ( aux ) );
                    COMPILE_FILE ( filename, "<html>\n<head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\">\n<title>%s - %s</title>\n</head>\n%s\n<div class=\"newsText\"><h1 class=\"title\">%s</h1>\n%s\n</div>\n</html>"
                                , npTitle, Title, header, Title, aux  );
                 }
                 else
                 {
                    sprintf ( title, "<h3>%s</h3>\n", aux );
                 }

                 aux = title;

                // Adicionando título ao arquivo HTML
                COMPILE ( "%s", title );

                j = 0;

                LOG ( "Ordem dos campos:", nextNews->name );

            // Gerando os campos tratados em ordem
                while ( nextNews->show[j] != NULL )
                {
                    // Não imprimir título novamente
                  if  ( strcmp ( nextNews->show[j], "title" ) == 0 )
                  {
                    j++;
                    continue;
                  }
                  LOG ( "\t%d. %s: %s", j+1, nextNews->show[j], fetchField ( nextNews, nextNews->show[j] ) );
                  COMPILE ( "%s", fetchField ( nextNews, nextNews->show[j] ) );
                  j++;
                }

                // Finalizando implementação da notícia na página principal
                COMPILE ( "\n</div>\n" );
                LOG ( "%s finalizado!", nextNews->name );

                // Gerando arquivo do texto completo da notícia
                // Num arquivo html separado
                i++;
            }

            // Limpando a memoria das variaveis globais
            free_split( web_show );

            i = 0;
            while ( list[i] != NULL ) 
            {
                j = 0;
                while ( list[i]->fields[j] != NULL ) 
                {
                    free( list[i]->fields[j] );
                    free( list[i]->values[j] );
                    j++;
                }
                free( list[i]->fields );
                free( list[i]->values );
                free_split( list[i]->show );

                free( list[i] );
                i++;
            }
            free( list );

            free ( title );
            free ( filename );
            free ( npTitle );

            // Fim do processo
            // Fechando a div de conteúdo
            COMPILE("\n</div>");
            COMPILE("\n</html>");
            LOG ( "Compilacao finalizada com sucesso!" );
         }
;

/* news_list: news
            | news_list news 
*/
news_list: news 
         | news_list news 
;

/* news: T_NAME '{' f_list newsStructure '}' */
news: T_NAME '{' f_list 
    {
        int i;
        char** fields; 
        char** values;

        // Realocando memoria
        list = (news**) realloc( list, sizeof(news*) * (index_news+1) ); 
        list[index_news] = (news*) calloc( 1, sizeof(news) );

        // Preenchendo noticia
        list[index_news]->name = strdup($1);       
        toLower( list[index_news]->name );

        LOG( "\n------" );
        LOG( "NOTICIA: %s", list[index_news]->name );
        LOG( "------\n" );

        LOG( "Enviado ao checker:" );
        LOG( "\n------" );
        LOG( "%s", $3 );
        LOG( "------\n" );

        // Verificando a validade
        if ( check_f_list($3, &fields, &values) ) 
        {
            LOG( "Os parametros obrigatorios estao presentes!" );

            // Transformandos todos os campos para minusculo
            i = 0;
            while ( fields[i] != NULL ) 
            {
                toLower( fields[i] );
                i++;
            }

        } else 
        {
            LOG( "ERRO: Um ou mais parametros obrigatorios nao foram encontrados!\n" );
            yyerror( "Um ou mais parametros obrigatorios nao foram encontrados!\n" );
            exit (-2);
        }

        // Verificando a unicidade
        if ( check_singleness( fields ) ) 
        {
            LOG( "ERRO: Ha nomes repetidos!\n" );
            yyerror( "Ha nomes repetidos!\n" );
            exit (-1);

        } else 
        {
            LOG ( "Os nomes sao unicos!" );
        }

        // Guardando os dados encontrados da noticia
        list[index_news]->fields = fields;
        list[index_news]->values = values;

        LOG( "\nCampos lidos:" );
        LOG( "\n------" );

        i = 0;
        while ( list[index_news]->fields[i] != NULL ) 
        {
            LOG( "%s", list[index_news]->fields[i] );
            i++;
        }

        LOG( "------\n" );
    }
    
    newsStructure '}' 
    {
        // Nesse ponto, uma noticia foi inteiramente armazenada
        index_news++;
    }
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
f_opt: title        
     | author      
     | abstract   
     | date      
     | image    
     | source  
     | text   
;


/* desc: title date
       | date title
*/ 
desc: title date 
    {
        // Realizando split
        char** title_aux = split_str($1, S2);
        char** date_aux = split_str($2, S2);

        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">\n</HEAD>\n", title_aux[1], date_aux[1] );
        COMPILE ( "\n<div class=\"header\">\n<h1>%s</h1>\n<h2>%s</h2>\n</div>\n", title_aux[1], date_aux[1] );
        COMPILE ( "\n<div class=\"content\">\n" );

        sprintf ( header, "<div class=\"header\">\n<a href=\"../index.html\"><h1>%s</h1>\n<h2>%s</h2></a>\n</div>\n", title_aux[1], date_aux[1], title_aux[1], date_aux[1] );

        npTitle = (char* ) calloc ( strlen ( title_aux[1] ), sizeof ( char ) );
        sprintf ( npTitle, "%s", title_aux[1] );

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

        COMPILE ( "\n<HEAD>\n<TITLE>%s</TITLE>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">\n</HEAD>\n", title_aux[1], date_aux[1] );
        COMPILE ( "\n<div class=\"header\">\n<h1>%s</h1>\n<h2>%s</h2>\n</div>\n", title_aux[1], date_aux[1] );
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

            // Transformandos show_aux[1] em minusculo
            i = 0;
            while ( show_aux[1][i] != NULL ) 
            {
                toLower( show_aux[1][i] );
                i++;
            }

            web_show = show_aux[1];                        
            LOG ( "Show:" ) ;

            i = 0;
            while ( web_show[i] != NULL ) 
            {
                LOG ( "%d.   %s", i+1, web_show[i] ) ;
                i++; 
            }
            LOG( "------\n" );

            // Verificando a unicidade
            if ( check_singleness( web_show ) ) 
            {
                LOG( "ERRO: Ha nomes repetidos!\n" );
                yyerror( "Ha nomes repetidos!\n" );
                exit ( -3 );

            } else 
            {
                LOG ( "Os nomes sao unicos!" );
            }

            // Liberando memoria
            free_split( show_aux[0] );                      // show_aux[1] é web_show, e sera' desalocado posteriormente
         }
;


/* newsStructure: T_STRUCTURE '{' col showNews '}' */
newsStructure: T_STRUCTURE '{' col 
             {
                // Realizando split
                char** col_aux = split_str($3, S2);

                list[index_news]->col = atoi( col_aux[1] );             
                LOG( "\nSTRUCTURE DA NOTICIA:");
                LOG( "------" );
                LOG ( "Col: %d", list[index_news]->col ) ;

                // Liberando memoria
                free_split( col_aux );
             }
            
             showNews '}'
             {
                int i;

                // Realizando split
                char** show_aux[2];
                
                show_aux[0] = split_str( $5, S2 );
                show_aux[1] = split_str( show_aux[0][1], S3 );

                // Transformandos show_aux[1] em minusculo
                i = 0;
                while ( show_aux[1][i] != NULL ) 
                {
                    toLower( show_aux[1][i] );
                    i++;
                }

                list[index_news]->show = show_aux[1];                        
                LOG ( "Show:" ) ;

                i = 0;
                while ( list[index_news]->show[i] != NULL ) 
                {
                    LOG ( "%d.   %s", i+1, list[index_news]->show[i] ) ;
                    i++; 
                }
                LOG( "------\n" );

                // Verificando a unicidade
                if ( check_singleness( list[index_news]->show ) ) 
                {
                    LOG( "ERROR: Ha nomes repetidos!\n" );
                    yyerror( "Ha nomes repetidos!\n" );

                } else 
                {
                    LOG ( "Os nomes sao unicos!" );
                }

                // Liberando memoria
                free_split( show_aux[0] );                      // show_aux[1] é web_show, e sera' desalocado posteriormente
             }
;


image: T_IMAGE '=' T_STRING 
     {
        $$ = concat(5, "image", S2, "<img src=\"", $3, "\">\n");
     } 
;


source: T_SOURCE '=' T_STRING
      {
        $$ = concat(5, "source", S2, "<p><strong>Fonte:</strong> ", $3, "</p>\n");
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
        $$ = concat(5, "author", S2, "<p><strong>Autor:</strong> ", $3, "</p>\n");
        //LOG ( "html Author >>> %s", concat(5, "author", S2, "<p><strong>Fonte:</strong>: ", $3, "</p>\n") );
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

int yyerror(const char* errmsg) { printf("\n*** ERRO: %s\n", errmsg); } 
int yywrap(void) { return 1; } 
extern int yy_flex_debug;

int main(int argc, char** argv)
{
     yyparse();
     return 0;
}
