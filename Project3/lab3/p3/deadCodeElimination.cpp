#include "llvm/Pass.h"
#include "llvm/Support/CFG.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/InstIterator.h"

using namespace llvm;

#include <vector>
#include <set>
#include <map>
#include <algorithm>

using namespace std;

// =============================
// Liveness Data
// =============================

namespace 
{
    class BasicBlockData 
    {
        public:
            // Sucessors
            vector< BasicBlock* > sucessors;

            // Sets
            set< Instruction* > use; 
            set< Instruction* > def; 

            set< Instruction* > in; 
            set< Instruction* > out; 
    };

    class InstructionData 
    {
        public:
            // Sets
            set< Instruction* > use; 
            set< Instruction* > def; 

            set< Instruction* > in; 
            set< Instruction* > out; 
    };

    // This class contains all data we'll need in liveness analysis 
    class LivenessData 
    {
        public:
            // These vectors contains all data we'll need
            map< BasicBlock*, BasicBlockData* > blocks;
            map< Instruction*, InstructionData* > instructions;

            // Destructor
            ~LivenessData() 
            {
                for( map< BasicBlock*, BasicBlockData* >::iterator i = blocks.begin();  i != blocks.end(); i++ ) 
                    delete i->second;

                for( map< Instruction*, InstructionData* >::iterator i = instructions.begin();  i != instructions.end(); i++ ) 
                    delete i->second;

                blocks.clear();
                instructions.clear();
            }

            // This method stores a new BasicBlock
            void addBasicBlock( BasicBlock* block ) 
            {
                blocks[block] = new BasicBlockData();

                // Adding sucessors
                for( succ_iterator succesor = succ_begin(block); succesor != succ_end(block); succesor++ ) 
                {
                    BasicBlock* Succ = *succesor;
                    blocks[block]->sucessors.push_back( Succ );
                }
            }

            // This method stores a new Instruction
            void addInstruction( Instruction* inst ) 
            {
                instructions[inst] = new InstructionData();
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
        // Set operations
        // =============================

        set< Instruction* > getSetUnion( const set< Instruction* >& s1, const set< Instruction* >& s2 ) 
        {
            set< Instruction* > ret;
            set_union( s1.begin(), s1.end(), s2.begin(), s2.end(), inserter( ret, ret.begin() ) );

            // Returning
            return ret;
        }

        set< Instruction* > getSetDifference( const set< Instruction* >& s1, const set< Instruction* >& s2 ) 
        {
            set< Instruction* > ret;
            set_difference( s1.begin(), s1.end(), s2.begin(), s2.end(), inserter( ret, ret.begin() ) );

            // Returning
            return ret;
        }

        // =============================
        // Liveness analysis
        // =============================
        map< Instruction*, InstructionData* > computeLiveness( Function* func ) 
        {
            LivenessData data;

            // ===========================================
            // Step 0: Store all BasicBlocks and
            //         Instructions in LivenessData
            // ===========================================
           
            // Iterating on all blocks of the function
            for( Function::iterator i = func->begin(); i != func->end(); ++i )
            {
                data.addBasicBlock( i );

                // Iterating on all instructions of the block
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {

                    if ( isa < Instruction >( *j ) )
                    {
                        data.addInstruction( j );
                    }
                }
            }  

            // ===========================================
            // Step 1: Compute use/def for all BasicBLocks
            // ===========================================
            int k = 0;
            for (Function::iterator i = func->begin(), e = func->end(); i != e; k++, ++i)
            {
                BasicBlockData * b = data.blocks[i];
                Value * vv;
                //def &= data.blocks[k]->def;
                //use &= data.blocks[k]->use;
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {
                    vv = j->getOperand ( 1 );
                    if ( isa < Instruction > ( *vv ) )
                    {
                        if ( b->def.find ( j ) == b->def.end() )
                        {
                            b->use.insert ( j );
                        }
                    }

                    vv = j->getOperand ( 2 );
                    if ( isa < Instruction > ( *vv ) )
                    {
                        if ( b->def.find ( j ) == b->def.end() )
                        {
                            b->use.insert ( j );
                        }
                    }

                    vv = j->getOperand ( 0 );
                    if ( isa < Instruction > ( *vv ) )
                    {
                        if ( b->use.find ( j ) == b->use.end() )
                        {
                            b->def.insert ( j );
                        }
                    }


                }
            }

            // ===========================================
            // Step 2: Compute in/out for all BasicBLocks
            // ===========================================

            // Reversely iterating on blocks
            bool inChanged = true;

            while ( inChanged == true )
            {
                inChanged = false;
                k = data.blocks.size();
                for (Function::iterator i = func->end(), e = func->begin(); i != e; --i, k--)
                {
                    --i; // DO NOT DELETE!
                    BasicBlockData * b = data.blocks[i];
                    Value * vv;
                    //def &= data.blocks[k]->def;
                    //use &= data.blocks[k]->use;

                    /*
                    // Iterating over successors
                    for (succ_iterator SI = succ_begin(i), E = succ_end(i); SI != E; ++SI) {
                        BasicBlock *Succ = *SI;

                        
                        // ...
                    }
                    */

                    // For each successor
                    for ( int s = 0; s < b->sucessors.size(); s++ )
                    {
                        BasicBlockData * succ = data.blocks[ b->sucessors[s] ];

                        // Union in[S]
                        b->out.insert ( succ->in.begin(), succ->in.end() );
                    }

                    // Used to verify if IN will change
                    set < Instruction * > old = b->in;

                    b->in = b->use;

                    set < Instruction * > tmp = b->out;
                    // Out[B] - defB
                    tmp.erase ( b->def.begin(), b->def.end() );

                    // use[B] U ( out[B] - def[B] )
                    b->in.insert ( tmp.begin(), tmp.end() );

                    // If some IN changed
                    for ( set<Instruction*>::iterator a = b->in.begin(),
                            aa = old.begin(); a != b->in.end(), 
                            aa != old.end(); a++, aa++ )
                    {
                        if ( *aa != *a )
                        {
                            inChanged = true;
                            break;
                        }
                    }

                    /*
                    for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                    {
                    }
                    */
                }
            }

            // ===========================================
            // Step 3: Use data from BasicBlocks to
            //         compute all Instructions use/def
            // ===========================================

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // For every Instruction inside a BasicBlock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {
                    unsigned int n = j->getNumOperands();

                    for( unsigned int k = 0; k < n; k++ ) 
                    {
                        Value* v = j->getOperand(k);

                        if( isa<Instruction>(v) ) 
                        {
                            Instruction *op = cast<Instruction>(v);

                            if ( !data.instructions[j]->use.count(op) ) 
                                data.instructions[j]->use.insert(op);
                        }
                    }

                    data.instructions[j]->def.insert( j );
                }
            }

            // ===========================================
            // Step 4: Use data from BasicBLocks to
            //         compute all Instructions in/out
            // ===========================================

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // Last instruction of the block
                BasicBlock::iterator j = i->end();
                j--;
                data.instructions[j]->out = data.blocks[i]->out;
                data.instructions[j]->in = getSetUnion( data.instructions[j]->use, getSetDifference( data.instructions[j]->out, data.instructions[j]->def ) );

                // Other instructions
                do 
                {
                    BasicBlock::iterator aux = j;
                    j--;

                    data.instructions[j]->out = data.instructions[aux]->in;
                    data.instructions[j]->in = getSetUnion( data.instructions[j]->use, getSetDifference( data.instructions[j]->out, data.instructions[j]->def ) );

                } while( j != i->begin() ); 
            }

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
            map< Instruction*, InstructionData* > liveness = computeLiveness( &F );
            
            // For every BasicBlock...
            for ( Function::iterator i = F.begin(); i != F.end(); i++ ) 
            {
                // For every Instruction inside BasicBLock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {
                    // Is this a instruction?
                    if ( isa<Instruction>( *j ) ) 
                    {
                        // Trivial checks
                        if ( isa<TerminatorInst>( *j ) || isa<LandingPadInst>( *j ) || j->mayHaveSideEffects() )     // TODO: DbgInfoIntrinsic case
                            continue;

                        // If instruction are going to die, remove it
                        if ( liveness[j]->out.count( j ) ) 
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

