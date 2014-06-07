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

#include <vector>
#include <set>

using namespace std;

namespace {
    struct deadCodeElimination : public FunctionPass {
        static char ID;

        // Constructor
        deadCodeElimination() : FunctionPass(ID) {}

        // =============================
        // Liveness analysis
        // =============================
        
        class BasicBlockData 
        {
            public:
                int id;
                BasicBlock block;

                // Constructor
                BasicBlockData( const int ID, const BasicBlock& basicBlock ) 
                    : id( ID ) 
                    , block ( basicBlock )
                {}

                // Sets
                set<Instruction> use; 
                set<Instruction> def; 

                set<Instruction> in; 
                set<Instruction> out; 
        };

        class InstructionData 
        {
            public:
                int id;
                Instruction instruction;

                // Constructor
                InstructionData( const int ID, const Instruction& inst ) 
                    : id( ID ) 
                    , Instruction( inst )
                {}

                // Sets
                set<Instruction> use; 
                set<Instruction> def; 

                set<Instruction> in; 
                set<Instruction> out; 
        };

        // This class contains all data we'll need in liveness analysis 
        class LivenessData 
        {
            private:
                // IDs
                static int blck_id;
                static int inst_id;

            public:
                // These vectors contains all data we'll need
                vector<BasicBlockData> blocks;
                vector<InstructionData> instructions;

                // Constructor
                Data() : blck_id( 0 ) , inst_id( 0 ) {}

                // This method stores a new BasicBlock
                void addBasicBlock( const BasicBlock& block ) 
                {
                    blocks.push_back( new BasicBlockData( blck_id ) );
                    blck_id++;
                }

                // This method stores a new Instruction
                void addInstruction( const Instruction& inst ) 
                {
                    instructions.push_back( new InstructionData( inst_id ) );
                    inst_id++;
                }
        }

        // Liveness analysis
        vector<InstructionData> computeLiveness( const Function& func ) 
        {
            LivenessData data;

            // ===========================================
            // Step 0: Store all BasicBlocks and
            //         Instructions in LivenessData
            // ===========================================
           


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
            vector<InstructionData> liveness = computeLiveness( F );
            
            // This will be used to retrieve instruction information
            int id = 0;
            
            // For every BasicBlock...
            for ( Function::iterator i = func->begin(); i != func->end(); i++, id++ ) 
            {
                // For every Instruction inside BasicBLock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++, id++ ) 
                {
                    // Is this a instruction?
                    if ( isa<Instruction>( *j ) ) 
                    {

                        // Make sure this is the correct instruction
                        if ( liveness[id].id != id ) 
                        {
                            errs() << "We got some strange situation, check it! \n";
                            continue;
                        }

                        // If instruction are going to die, remove it
                        if ( liveness.out.count( *j ) ) 
                        {
                            ( *j )->eraseFromParent();
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

