#
# pacote v02
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
#

all: clean compile
	@echo ""

clean:
	rm -f src/llvm/*.class src/llvmast/*.class

compile:
	javac -classpath lib/projeto2.jar src/llvm/Codegen.java src/llvmast/*.java

run:
	java -classpath src:lib/projeto2.jar main/Main $(INPUT) $(OUTPUT)

bytecode:
	llvm-as $(INPUT) -o $(OUTPUT)
