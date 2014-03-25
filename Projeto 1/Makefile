CFLAGS=-g
BISON=bison
FLEX=flex

all: parser

run: renew parser
	./parser < sempre_online.npl
 
parser: parser.o scanner.o checker.o utility formatter
	$(CC) -o parser scanner.o parser.o checker.o utility.o formatter.o
 
parser.c: parser.y
	$(BISON) -d parser.y  -o parser.c
 
scanner.c: scanner.l
	$(FLEX)  -o scanner.c scanner.l

utility: utility.c
	$(CC) -c utility.c

formatter: formatter.c
	$(CC) -c formatter.c

checker: checker.c
	$(CC) -c checker.c

clean:
	rm -f scanner.c scanner.o parser.c parser.o parser.h parser utility.o checker.o formatter.o

renew:
	touch teste.html
	rm teste.html
