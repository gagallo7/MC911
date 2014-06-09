#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/InstIterator.h"

using namespace llvm;

#include <vector>
#include <set>

using namespace std;

// =============================
// Liveness Data
// =============================

namespace 
{
    class BasicBlockData 
    {
        public:
            const int id;
            const BasicBlock* block;

            // Constructor
            BasicBlockData( const int ID, const BasicBlock* basicBlock ) 
                : id( ID )
                , block( basicBlock )
            {}

            // Sets
            set< const Instruction* > use; 
            set< const Instruction* > def; 

            set< const Instruction* > in; 
            set< const Instruction* > out; 
    };

    class InstructionData 
    {
        public:
            const int id;
            const Instruction* instruction;

            // Constructor
            InstructionData( const int ID, const Instruction* inst ) 
                : id( ID ) 
                , instruction( inst )
            {}

            // Sets
            set< const Instruction* > use; 
            set< const Instruction* > def; 

            set< const Instruction* > in; 
            set< const Instruction* > out; 
    };

    // This class contains all data we'll need in liveness analysis 
    class LivenessData 
    {
        private:
            // IDs
            int blck_id;
            int inst_id;

        public:
            // These vectors contains all data we'll need
            vector< BasicBlockData*  > blocks;
            vector< InstructionData* > instructions;

            // Constructor
            LivenessData() : blck_id( 0 ) , inst_id( 0 ) {}

            // Destructor
            ~LivenessData() 
            {
                for( unsigned int i = 0;  i < blocks.size(); i++ ) 
                    delete blocks[i];

                for( unsigned int i = 0;  i < instructions.size(); i++ ) 
                    delete instructions[i];

                blocks.clear();
                instructions.clear();
            }

            // This method stores a new BasicBlock
            void addBasicBlock( const BasicBlock* block ) 
            {
                blocks.push_back( new BasicBlockData( blck_id, block ) );
                blck_id++;
            }

            // This method stores a new Instruction
            void addInstruction( const Instruction* inst ) 
            {
                instructions.push_back( new InstructionData( inst_id, inst ) );
                inst_id++;
            }
    };
}

// =============================

namespace 
{
    struct deadCodeElimination : public FunctionPass 
    {
        static char ID;

        // Constructor
        deadCodeElimination() : FunctionPass(ID) {}

        // =============================
        // Liveness analysis
        // =============================
        vector< InstructionData* > computeLiveness( Function& F ) 
        {
            LivenessData data;
            Function * func = &F;

            // ===========================================
            // Step 0: Store all BasicBlocks and
            //         Instructions in LivenessData
            // ===========================================
           
            // ITERANDO SOBRE TODOS OS BLOCOS
            for (Function::iterator i = func->begin(), e = func->end(); i != e; ++i)
            {
                data.addBasicBlock ( i );

                // Iterating on all instructions of the block
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {

                    if ( isa < Instruction > ( *j ) )
                    {
                        data.addInstruction ( j );
                    }
                }
            }  

            // ===========================================
            // Step 1: Compute use/def for all BasicBLocks
            // ===========================================



            // ===========================================
            // Step 2: Compute in/out for all BasicBLocks
            // ===========================================



            // ===========================================
            // Step 3: Use data from BasicBlocks to
            //         compute all Instructions use/def
            // ===========================================

            for( unsigned int i = 0 ; i < data.instructions.size(); i++ ) 
            {
                const Instruction* inst_aux = cast<Instruction>( data.instructions[i]->instruction );
                unsigned int n = inst_aux->getNumOperands();

                for( unsigned int j = 0; j < n; j++ ) 
                {
                    Value* v = inst_aux->getOperand(j);

                    if( isa<Instruction>(v) ) 
                    {
                        Instruction *op = cast<Instruction>(v);

                        if ( !data.instructions[i]->use.count(op) ) 
                            data.instructions[i]->use.insert(op);
                    }
                }

                data.instructions[i]->def.insert( inst_aux );
            }

            // ===========================================
            // Step 4: Use data from BasicBLocks to
            //         compute all Instructions in/out
            // ===========================================




            // ===========================================
            // Returning
            // ===========================================

            return data.instructions;

            // ===========================================
        } 

        // =============================
        // Optimization
        // =============================
        virtual bool runOnFunction( Function &F ) 
        {
            bool changed = false;
            vector< InstructionData* > liveness = computeLiveness( F );
            
            // This will be used to retrieve instruction information
            int id = 0;
            
            // For every BasicBlock...
            for ( Function::iterator i = F.begin(); i != F.end(); i++ ) 
            {
                // For every Instruction inside BasicBLock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++, id++ ) 
                {
                    // Is this a instruction?
                    if ( isa<Instruction>( *j ) ) 
                    {

                        // Make sure this is the correct instruction
                        if ( liveness[id]->id != id ) 
                        {
                            errs() << "We got some strange situation, check it! \n";
                            continue;
                        }

                        // Trivial checks
                        if ( isa<TerminatorInst>( *j ) || isa<LandingPadInst>( *j ) || j->mayHaveSideEffects() )     // TODO: DbgInfoIntrinsic case
                            continue;

                        // If instruction are going to die, remove it
                        if ( liveness[id]->out.count( j ) ) 
                        {
                            j->eraseFromParent();
                            changed = true;
                        }
                    }
                }
            }

            // Return
            return changed;
        }

        // =============================
    };
}

char deadCodeElimination::ID = 0;
static RegisterPass<deadCodeElimination> X( "deadCodeElimination", "Hello World Pass", false, false );

