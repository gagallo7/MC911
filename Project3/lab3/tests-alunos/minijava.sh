#!/bin/bash

# meu DCE (que também chama o deadLoad)
opt -load ../Release/P3.so -dce-p3 $1 > out-dce

# DCE do LLVM
#
# Esta comparacao nao eh mais valida pois 
# pois o meu DCE possui também o deadLoad
#
#./drive opt -dce out-cp > out-gab-dce
#diff -w out-gab-dce out-dce

rm out*







