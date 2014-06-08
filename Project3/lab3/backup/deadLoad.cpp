/* Guilherme Alcarde Gallo
   A FIM DE FACILITAR A VISUALIZAÇÃO, MEUS COMENTÁRIOS ESTÃO EM CAIXA ALTA!
   SORRY FOR BURNING YOUR EYES...
   */
#include <typeinfo>
#include <queue>
#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/InstIterator.h"

using namespace llvm;

namespace {
    struct deadLoad : public FunctionPass {
        static char ID;
        std::queue < Instruction * > delQueue;
        bool changed = false, lastInstIsStore = false;
        Instruction * store;
        Value * v;
        deadLoad() : FunctionPass(ID) {}

        virtual bool runOnFunction(Function &F) {
            errs() << "deadLoad: ";

            Function * func = &F;
            // func is a pointer to a Function instance

            // ITERANDO SOBRE TODAS AS FUNÇÕES
            for (Function::iterator i = func->begin(), e = func->end(); i != e; ++i)
            {
                // Print out the name of the basic block if it has one, and then the
                // number of instructions that it contains
                errs() << "Basic block (name=" << i->getName() << ") has "
                    << i->size() << " instructions.\n";
                // blk is a pointer to a BasicBlock instance

                // ITERANDO SOBRE TODOS OS BLOCOS
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {
                    // The next statement works since operator<<(ostream&,...)
                    // is overloaded for Instruction&

                    // VERIFICANDO SE A INSTRUÇÃO ATUAL É UM LOAD E A ANTERIOR É UM STORE
                    if ( typeid ( *j ) == typeid ( LoadInst ) && lastInstIsStore )
                    {

                        // SE O SEGUNDO OPERANDO DO STORE FOR O MESMO QUE O PRIMEIRO OPERANDO DO LOAD
                        // OU SEJA O LOAD ESTÁ CARREGANDO O MESMO ENDEREÇO DA MEMÓRIA QUE ACABOU DE SER
                        // "STORED"
                        if ( ((*store).getOperand(1))->getName() == j->getOperand(0)->getName() )
                        {
                            errs() << "\n\n!!!!!! Subsequent Load found!" << "\n";
                            errs() << *store << "\n";
                            errs() << *j << "\n\n\n";

                            // TROCAR TODOS OS VALORES QUE USAM O VALOR DO LOAD COM O VALOR A SER "STORED"
                            j->replaceAllUsesWith(store->getOperand(0));

                            // DEIXANDO AS INSTRUÇÕES A SEREM REMOVIDAS PARA NO FINAL

                            delQueue.push ( j );

                            // O CÓDIGO FOI MODIFICADO, runOnFunction AVISARÁ
                            changed = true;
                        }
                    }

                    // SE UM STORE FOR ENCONTRADO, AVISAR QUE A ÚLTIMA INSTRUÇÃO FOI UM STORE
                    else if ( typeid ( *j ) == typeid ( StoreInst ) )
                    {
                        store = j;
                        lastInstIsStore = true;
                    }

                    // SE NÃO FOR LOAD NEM STORE, CORRIGIR O AVISO
                    else
                    {
                        lastInstIsStore = false;
                    }
                }
            }

            // DELETANDO CÓDIGO MORTO
            while ( delQueue.size() )
            {
                Instruction * I = delQueue.front();
                delQueue.pop();
                I->eraseFromParent();
            }
            /*
            */
            return changed;
        }
    };
}

char deadLoad::ID = 0;
static RegisterPass<deadLoad> X("deadLoad", "Hello World Pass", false, false);

