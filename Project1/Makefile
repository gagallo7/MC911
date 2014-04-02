CFLAGS=-g
BISON=bison
FLEX=flex

all: parser

run: renew parser
	./parsernpl < sempre_online.npl

debug:
	$(CC) -c libs/formatter.c -g
	$(CC) -o parser scanner.o parser.o libs/checker.o libs/utility.o libs/formatter.o
 
parser: parser.o scanner.o libs/checker.o utility formatter
	$(CC) -o parsernpl scanner.o parser.o libs/checker.o libs/utility.o libs/formatter.o
 
parser.c: parser.y
	$(BISON) -d parser.y  -o parser.c
 
scanner.c: scanner.l
	$(FLEX)  -o scanner.c scanner.l

utility: libs/utility.c
	cd libs/ && $(CC) -c utility.c

formatter: libs/formatter.c
	cd libs/ && $(CC) -c formatter.c

checker: libs/checker.c
	cd libs/ && $(CC) -c checker.c

clean:
	rm -f scanner.c scanner.o parser.c parser.o parser.h parsernpl libs/utility.o libs/checker.o libs/formatter.o
	rm -f index.html
	rm -f noticias/*.html

renew:
	mkdir -p noticias
	rm -f index.html
