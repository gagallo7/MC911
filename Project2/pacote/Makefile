
# pacote v03
# 
# Como compilar:
# $ make compile
#
# Como executar o compilador:
# $ make run INPUT=test/teste.java OUTPUT=teste.s
#
# Como gerar bytecode:
# $ make bytecode INPUT=teste.s OUTPUT=teste.bc
#
# Como executar o bytecode:
# $ lli teste.bc

INPUT_run = test/teste.java
OUTPUT_run = teste.s
OUTPUT_bytecode = teste.bc

all: clean compile run bytecode exec
	@echo ""

clean:
	find .. -name "*.class" | xargs -I file rm file

compile:
	javac -classpath lib/projeto2.jar src/llvm/*.java src/llvmast/*.java

run:
	java -classpath src:lib/projeto2.jar main/Main $(INPUT_run) $(OUTPUT_run)

bytecode:
	llvm-as $(OUTPUT_run) -o $(OUTPUT_bytecode)

clang:
	clang $(INPUT_run) -S -emit-llvm -o $(OUTPUT_bytecode)

exec:
	lli $(OUTPUT_bytecode)
