

CFLAGS=-g
BISON=bison
FLEX=flex

run: parser
	rm teste.html
	./parser < sempre_online.npl
 
parser: parser.o scanner.o checker.o utility.o
	$(CC) -o parser scanner.o parser.o checker.o utility.o
 
parser.c: parser.y
	$(BISON) -d parser.y  -o parser.c
 
scanner.c: scanner.l
	$(FLEX)  -o scanner.c scanner.l

clean:
	rm -f scanner.c scanner.o parser.c parser.o parser.h parser utility.o checker.o

