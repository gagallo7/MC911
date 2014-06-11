#include "llvm/Pass.h"
#include "llvm/Support/CFG.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/InstIterator.h"
#include "llvm/IR/IntrinsicInst.h"

using namespace llvm;

#include <vector>
#include <set>
#include <map>
#include <queue>
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

            errs() << ":D:D:D:D Step 0\n\n";
            // ===========================================
            // Step 0: Store all BasicBlocks and
            //         Instructions in LivenessData
            // ===========================================
           
            // Iterating on all blocks of the function
            for( Function::iterator i = func->begin(); i != func->end(); ++i )
            {
                data.addBasicBlock( &*i );

                // Iterating on all instructions of the block
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {

                    if ( isa < Instruction >( *j ) )
                    {
                        data.addInstruction( &*j );
                    }
                }
            }  

            errs() << ":D:D:D:D Step 1\n\n";

            // ===========================================
            // Step 1: Compute use/def for all BasicBLocks
            // ===========================================
            int k = 0;
            unsigned numOp, opr;
            for (Function::iterator i = func->begin(), e = func->end(); i != e; k++, ++i)
            {
                BasicBlockData * b = data.blocks[ &*i ];
                Value * vv;
                for (BasicBlock::iterator j = i->begin(), e = i->end(); j != e; ++j)
                {
                    numOp = j->getNumOperands();

                    for ( opr = 0; opr < numOp; opr++ )
                    {
                        vv = j->getOperand ( opr );
                        if ( isa < Instruction > ( *vv ) )
                        {
                            if ( b->def.find ( &*j ) == b->def.end() )
                            {
                                b->use.insert ( &*j );
                            }
                        }
                    }

                    if ( isa < Instruction > ( j ) )
                    {
                        if ( b->use.find ( &*j ) == b->use.end() )
                        {
                            b->def.insert ( &*j );
                        }
                    }
                }
            }

            errs() << ":D:D:D:D Step 2\n\n";

            // ===========================================
            // Step 2: Compute in/out for all BasicBLocks
            // ===========================================

            // Reversely iterating on blocks
            bool inChanged = true;

            while ( inChanged == true )
            {
                inChanged = false;
                Function::iterator fe = func->end();
                fe--;
                for (Function::iterator i = fe, e = func->begin(); i != e; --i)
                {
                    BasicBlockData * b = data.blocks[ &*i ];

                    // For each successor
                    for ( unsigned int s = 0; s < b->sucessors.size(); s++ )
                    {
                        BasicBlockData * succ = data.blocks[ b->sucessors[s] ];

                        // Union in[S]
                        b->out.insert ( succ->in.begin(), succ->in.end() );
                    }

                    // Used to verify if IN will change
                    set < Instruction * > old ( b->in );

                    b->in = b->use;

                    set < Instruction * > tmp;

                    errs() << "Before diff\n";
                    // Out[B] - defB
                    tmp = getSetDifference ( b->out, b->in );

                    errs() << "After diff\n";
                    // use[B] U ( out[B] - def[B] )
                    b->in.insert ( tmp.begin(), tmp.end() );

                    // If some IN changed
                    set<Instruction*>::iterator a, aa;
                    a = b->in.begin();
                    aa = old.begin();

                    while ( a != b->in.end() || aa != old.end() )
                    {
                        errs() << "aa == " << *aa << "\t";
                        errs() << "a == " << *a << "\n";
                        if ( *aa != *a )
                        {
                            inChanged = true;
                            errs() << "Breaking\n";
                            break;
                        }
                        ++aa;
                        ++a;
                    }
                }
            }

            errs() << ":D:D:D:D Step 3\n\n";

            // ===========================================
            // Step 3: Use data from BasicBlocks to
            //         compute all Instructions use/def
            // ===========================================

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // For every Instruction inside a BasicBlock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {
                    if( isa<Instruction>( j ) ) 
                    {
                        unsigned int n = j->getNumOperands();

                        for( unsigned int k = 0; k < n; k++ ) 
                        {
                            Value* v = j->getOperand( k );

                            if( isa<Instruction> ( v ) ) 
                            {
                                Instruction *op = cast<Instruction>( v );

                                if ( !data.instructions[ &*j ]->use.count( op ) ) 
                                    data.instructions[ &*j ]->use.insert( op );
                            }
                        }

                        data.instructions[ &*j ]->def.insert( &*j );
                    }
                }
            }

            errs() << ":D:D:D:D Step 4\n\n";

            // ===========================================
            // Step 4: Use data from BasicBLocks to
            //         compute all Instructions in/out
            // ===========================================

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // Last instruction of the block
                BasicBlock::iterator j = i->end();
                j--;
                data.instructions[ &*j ]->out = data.blocks[ &*i ]->out;
                data.instructions[ &*j ]->in = getSetUnion( data.instructions[ &*j ]->use, getSetDifference( data.instructions[ &*j ]->out, data.instructions[ &*j ]->def ) );

                // Other instructions
                BasicBlock::iterator aux = j;

                while( j != i->begin() )
                {
                    aux = j;
                    j--;

                    data.instructions[ &*j ]->out = data.instructions[ &*aux ]->in;
                    data.instructions[ &*j ]->in = getSetUnion( data.instructions[ &*j ]->use, getSetDifference( data.instructions[ &*j ]->out, data.instructions[ &*j ]->def ) );

                } 

                data.instructions[ &*j ]->out = data.instructions[ &*aux ]->in;
                data.instructions[ &*j ]->in = getSetUnion( data.instructions[ &*j ]->use, getSetDifference( data.instructions[ &*j ]->out, data.instructions[ &*j ]->def ) );
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
            queue< Instruction* > toDelete;

            errs() << "Tamanho do map: " << liveness.size() << "\n";
            
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
                        if ( isa<TerminatorInst>( *j ) || isa<LandingPadInst>( *j ) || j->mayHaveSideEffects() || isa<DbgInfoIntrinsic>( *j ) )
                            continue;

                        // If instruction are going to die, remove it
                        if ( !liveness[ &*j ] ) 
                        {
                            errs() << "Acessando uma instrução do map \n";

                            if ( liveness[ &*j ]->out.count( &*j ) ) 
                            {
                                toDelete.push( &*j );
                                changed = true;
                            }
                        }
                    }
                }
            }

            errs() << "Instruções deletadas:: " << toDelete.size() << "\n";

            // Deleting
            while( toDelete.size() > 0 ) 
            {
                Instruction* deadInst = toDelete.front();
                toDelete.pop();
                deadInst->eraseFromParent();
            }

            // Return
            return changed;
        }

        // =============================
    };
}

char deadCodeElimination::ID = 0;
static RegisterPass<deadCodeElimination> X( "deadCodeElimination", "Dead Code Elimination Pass", false, false );

