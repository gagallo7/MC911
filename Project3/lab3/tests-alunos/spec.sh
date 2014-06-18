#!/bin/bash

# Este script:
# 1. Aplica o DCE contido em P3.so
# 2. Gera o executável i386 do resultado
# 3. Executa-o
# 4. Compara a saída do código otimizado com a golden

base=$PWD

case $1 in
    gcc)
        opt -load ../Release/P3.so -dce-p3 ./spec/403.gcc/gcc.bc -o ./spec/403.gcc/out-dce.bc  
        #opt -load ../Release/P3.so -dce-p3 ./spec/403.gcc/gcc.bc -o ./spec/403.gcc/out-dce.bc  &> /dev/null
        cd spec/403.gcc/
        llc out-dce.bc
        gcc -m32 out-dce.s -o out-dce
        ./out-dce test/cccp.i -o test/cccp.s
        diff test/cccp.s golden/output/cccp.s
        rm out-*
        cd $base
        ;;

    dealII)
        opt -load ../Release/P3.so -dce-p3 ./spec/447.dealII/dealII.bc -o ./spec/447.dealII/out-dce.bc &> /dev/null
        cd spec/447.dealII/
        llc out-dce.bc
        gcc -m32 -Ddeal_II_dimension=3 -DBOOST_DISABLE_THREADS  out-dce.s -o out-dce -lstdc++ -lm 
        ./out-dce 3
        for i in solution*; do 
            diff $i golden/output/$i
        done
        rm out-*
        rm solution-*
        rm grid-*
        cd $base
        ;;
    
    xalan)
        #opt -load ../Release/P3.so -dce-p3 ./spec/483.xalancbmk/xalan.bc -o ./spec/483.xalancbmk/out-dce.bc &> /dev/null
        opt -load ../Release/P3.so -dce-p3 ./spec/483.xalancbmk/xalan.bc -o ./spec/483.xalancbmk/out-dce.bc
        cd spec/483.xalancbmk/
        llc out-dce.bc
        gcc -m32 -DNDEBUG -DAPP_NO_THREADS -DXALAN_INMEM_MSG_LOADER        \
            -DPROJ_XMLPARSER -DPROJ_XMLUTIL -DPROJ_PARSERS                 \
            -DPROJ_SAX4C -DPROJ_SAX2 -DPROJ_DOM -DPROJ_VALIDATORS          \
            -DXML_USE_NATIVE_TRANSCODER -DXML_USE_INMEM_MESSAGELOADER      \
            -DXML_USE_PTHREADS out-dce.s -o out-dce -lstdc++ -lm 
        ./out-dce -v test/test.xml test/xalanc.xsl > test.out
        diff test.out golden/output/test.out
        rm out-*
        rm test.out
        ;;
    *)
        echo "Aplicativo SPEC incorreto"
        ;;
esac


