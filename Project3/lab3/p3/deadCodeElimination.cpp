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
#include <fstream>
#include <sstream>

using namespace std;

#define LOG(X) log.open("log", ios::app); \
                log << X; \
                log.close();

#define LOGC(X) log.open("log", ios::app); \
                log << "\033[1;21m" << X << "\033[0m"; \
                log.close();

#define LOGC2(X) log.open("log", ios::app); \
                log << "\033[32m" << X << "\033[0m"; \
                log.close();

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
                /*
            ~LivenessData() 
            {
                for( map< BasicBlock*, BasicBlockData* >::iterator i = blocks.begin();  i != blocks.end(); i++ ) 
                    delete i->second;

                for( map< Instruction*, InstructionData* >::iterator i = instructions.begin();  i != instructions.end(); i++ ) 
                    delete i->second;

                blocks.clear();
                instructions.clear();
            }
                    */

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

            void logLiveness ()
            {
            }

            // This method stores a new Instruction
            void addInstruction( Instruction* inst ) 
            {
                instructions[inst] = new InstructionData();
            }
    };

    struct deadCodeElimination : public FunctionPass 
    {
        static char ID;
        ofstream log;

        // Constructor
        deadCodeElimination() : FunctionPass(ID) {
        }

        // Destructor
        ~deadCodeElimination()
        {
        }

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

        string newInst ( )
        {
            static int ni = 0;
            std::stringstream ss;
            ss << " tmp" << ni++;
            //errs() << ss.str() << "\n";
            return ss.str();
        }

        // =============================
        // Liveness analysis
        // =============================
        map< Instruction*, InstructionData* > computeLiveness( Function* func ) 
        {
            LivenessData data;

            LOGC ( "\n-------------------------NEW LIVENESS ANALYSIS-----------------------\n");

            errs() << ":D:D:D:D Step 0\n";
            LOGC2("\n++++++++Step 0\n");
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

            // Logging
            for ( map < BasicBlock*, BasicBlockData* >::iterator bb = data.blocks.begin();
                    bb != data.blocks.end();
                    bb++
                )
            {
                LOG( "Block " << (*bb).first << "\n\tSuccesors:\n" );

                for ( unsigned int sc = 0; sc < (*bb).second->sucessors.size(); sc++ )
                {
                    LOG ( "\t--"<< (*bb).second->sucessors[sc] << "\n" );
                }
                
            }

            // Logging
            LOG("Instructions\n");
            for ( map < Instruction*, InstructionData* >::iterator ib = data.instructions.begin();
                    ib != data.instructions.end();
                    ib++
                )
            {
                LOG("\t~~" << ib->first << " " << ((*ib).first)->getName().str() << "\n");
            }

            errs() << ":D:D:D:D Step 1\n";
            LOGC2("\n++++++++Step 1\n");

            // ===========================================
            // Step 1: Compute use/def for all BasicBLocks
            // ===========================================
            unsigned numOp, opr;
            for (Function::iterator i = func->begin(), e = func->end(); i != e; ++i)
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
                            Instruction * vvv = cast < Instruction > (&*vv);
                            
                            if ( b->def.find ( &*vvv ) == b->def.end() )
                            {
                                b->use.insert ( &*vvv );
                            }
                        }
                    }

                    if ( isa < Instruction > ( *j ) )
                    {
                        if ( b->use.find ( &*j ) == b->use.end() )
                        {
                            b->def.insert ( &*j );
                        }
                    }
                }

                // Logging
                LOG ( "Block " << &*i << "\n" );
                LOG ( "\tDEF: { " );
                for ( set < Instruction* >::iterator si = b->def.begin();
                        si != b->def.end();
                        si++
                    )
                {
                    Instruction * I = *si;
                    LOG ( (*si) <<  " " << I->getName().str() << ", " );
                }
                LOG ( "}\n\tUSE: { " );
                for ( set < Instruction* >::iterator si = b->use.begin();
                        si != b->use.end();
                        si++
                    )
                {
                    LOG ( *si << " " << (*si)->getName().str() << ", " );
                }
                LOG ( "}\n" );

            }

            errs() << ":D:D:D:D Step 2\n";
            LOGC2("\n++++++++Step 2\n");

            // ===========================================
            // Step 2: Compute in/out for all BasicBLocks
            // ===========================================

            // Reversely iterating on blocks
            bool inChanged = true;

            while ( inChanged == true )
            {
                inChanged = false;
                Function::iterator fe = func->end();
                //fe--;
                //for (Function::iterator i = fe, e = func->begin(); i != e;)
                while ( fe != func->begin() )
                {
                    fe--;
                    BasicBlockData * b = data.blocks[ &*fe ];

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

                    // tmp = out - def
                    set < Instruction * > tmp;

                    // Out[B] - defB
                    tmp = getSetDifference ( b->out, b->def );
                    
                    // LOGGING results
                    LOGC ( "out: { " );
                    for ( set < Instruction* >::iterator si = b->out.begin();
                            si != b->out.end();
                            si++
                        )
                    {
                        LOG ( *si << " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 

                    LOGC ( "def: { " );
                    for ( set < Instruction* >::iterator si = b->def.begin();
                            si != b->def.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 
                    
                    LOGC ( "TMP: { " );
                    for ( set < Instruction* >::iterator si = tmp.begin();
                            si != tmp.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 

                    // use[B] U ( out[B] - def[B] )
                    b->in.insert ( tmp.begin(), tmp.end() );

                    // LOGGING results
                    LOGC ( "out: { " );
                    LOGC ( " in: { " );
                    for ( set < Instruction* >::iterator si = b->in.begin();
                            si != b->in.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n\n" ); 

                    // If some IN changed
                    if ( inChanged == false )
                    {
                        set<Instruction*>::iterator a, aa;
                        a = b->in.begin();
                        aa = old.begin();

                        while ( a != b->in.end() || aa != old.end() )
                        {
                            if ( *aa != *a )
                            {
                                inChanged = true;
                                break;
                            }
                            ++aa;
                            ++a;
                        }
                    }
                }
            }

            // LOGGING results
            for (Function::iterator i = func->begin(), e = func->end(); i != e; ++i)
            {
                BasicBlockData * b = data.blocks[ &*i ];

                LOG ( "Block " << &*i << "\n" );

                LOG ( "\tIN: { " );
                for ( set < Instruction* >::iterator si = b->in.begin();
                        si != b->in.end();
                        si++
                    )
                {
                    LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                }

                LOG ( " }\n\tOUT: { " );
                for ( set < Instruction* >::iterator si = b->out.begin();
                        si != b->out.end();
                        si++
                    )
                {
                    LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                }
                LOG ( " }\n" );
            }

            errs() << ":D:D:D:D Step 3\n";
            LOGC2("\n++++++++Step 3\n");

            // ===========================================
            // Step 3: Use data from BasicBlocks to
            //         compute all Instructions use/def
            // ===========================================
            //errs() << "Quantidade de instruções no map: " << data.instructions.size() << "\n";

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // For every Instruction inside a BasicBlock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {
                    if( isa<Instruction>( *j ) ) 
                    {
                        unsigned int n = j->getNumOperands();

                        for( unsigned int k = 0; k < n; k++ ) 
                        {
                            Value* v = j->getOperand( k );

                            if( isa<Instruction> ( v ) ) 
                            {
                                Instruction* op = cast<Instruction>( v );

                                if ( !data.instructions[ &*j ]->def.count( op ) ) 
                                    data.instructions[ &*j ]->use.insert( op );
                            }
                        }

                        if ( !data.instructions[ &*j ]->use.count ( &*j ) )
                        {
                            data.instructions[ &*j ]->def.insert( &*j );
                        }
                    }
                }

                // LOGGING results
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {
                    LOG ( "Instruction " << &*j << "\n" );

                    LOG ( "\tUSE: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->use.begin();
                            si != data.instructions[ &*j ]->use.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    
                    LOG ( " }\n\tDEF: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->def.begin();
                            si != data.instructions[ &*j ]->def.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOG ( " }\n" );
                }

            }

            errs() << ":D:D:D:D Step 4\n";
            LOGC2("\n++++++++Step 4\n");

            // ===========================================
            // Step 4: Use data from BasicBLocks to
            //         compute all Instructions in/out
            // ===========================================
            errs() << "Quantidade de instruções no map: " << data.instructions.size() << "\n";

            for( Function::iterator i = func->begin(); i != func->end(); i++ ) 
            {
                // Last instruction of the block
                BasicBlock::iterator j = i->end();
                j--;
                data.instructions[ &*j ]->out = data.blocks[ &*i ]->out;

                // in = use U ( out - def )
                data.instructions[ &*j ]->in = getSetUnion( data.instructions[ &*j ]->use, getSetDifference( data.instructions[ &*j ]->out, data.instructions[ &*j ]->def ) );

                // Other instructions
                BasicBlock::iterator aux;

                // for each instruction other than the last one
                while( j != i->begin() )
                {
                    aux = j;
                    j--;

                    data.instructions[ &*j ]->out = data.instructions[ &*aux ]->in;

                    // in = use U ( out - def )
                    data.instructions[ &*j ]->in = 
                        getSetUnion( 
                                data.instructions[ &*j ]->use, 
                                // out - def
                                getSetDifference( 
                                    data.instructions[ &*j ]->out, 
                                    data.instructions[ &*j ]->def 
                                    )
                                );
                } 

                // Logging
                j = i->end();
                while ( j != i->begin() )
                {
                    j--;
                    LOG ( "Instruction " << &*j << "\n" );
                    LOGC ( "out: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->out.begin();
                            si != data.instructions[ &*j ]->out.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 

                    LOGC ( "def: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->def.begin();
                            si != data.instructions[ &*j ]->def.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 

                    LOGC ( "use: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->use.begin();
                            si != data.instructions[ &*j ]->use.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 

                    LOGC ( " in: { " );
                    for ( set < Instruction* >::iterator si = data.instructions[ &*j ]->in.begin();
                            si != data.instructions[ &*j ]->in.end();
                            si++
                        )
                    {
                        LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                    }
                    LOGC ( " }\n" ); 
                    /*
                    */
                }
            }

            errs() << "Quantidade de instruções no map: " << data.instructions.size() << "\n";

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

            LOGC ( "\n!!!!!!!!!!!!!! OPTIMIZATION !!!!!!!!!!!!!!!\n" );

            LOG("Instructions: ");
            for ( map < Instruction*, InstructionData* >::iterator ib = liveness.begin();
                    ib != liveness.end();
                    ib++
                )
            {
                LOG( (*ib).first << " (" << ib->first->getName().str() << "), " );
            }
            LOG("\n");

            //errs() << "Tamanho do map: " << liveness.size() << "\n";
            
            // For every BasicBlock...
            for ( Function::iterator i = F.begin(); i != F.end(); i++ ) 
            {
                // For every Instruction inside BasicBLock...
                for ( BasicBlock::iterator j = i->begin(); j != i->end(); j++ ) 
                {

                    // Is this an instruction?
                    if ( isa<Instruction>( *j ) ) 
                    {
                        // Trivial checks
                        if ( isa<TerminatorInst>( *j ) || isa<LandingPadInst>( *j ) || 
                                j->mayHaveSideEffects() || isa<DbgInfoIntrinsic>( *j ) )
                            continue;
                        /*
                            */

                        if ( liveness [ &*j ]->out.size() == 0 )
                            LOGC ("==>> ");
                        LOGC ( "Candidate Instruction " << &*j << " (" << j->getName().str() << ") out: { " );
                        for ( set < Instruction* >::iterator si = liveness[ &*j ]->out.begin();
                                si != liveness[ &*j ]->out.end();
                                si++
                            )
                        {
                            LOG ( *si <<  " " << (*si)->getName().str() << ", " );
                        }
                        LOGC ( " } Set test: " << liveness[ &*j ]->out.count( &*j ) << "\n" ); 
                        //                        errs() << &*j << *j << "\n";

                        // If the instruction is going to die, remove it

                        if ( liveness[ &*j ]->out.count( &*j ) == 0 ) 
                        {
                            toDelete.push( &*j );
                            changed = true;
                        }
                    }
                }
            }

            // Deleting
            if ( toDelete.size() )
            {
                errs() << "Instruções deletadas:: " << toDelete.size() << "\n";
            }

            while( toDelete.size() > 0 ) 
            {
                Instruction* deadInst = toDelete.front();
                toDelete.pop();
                errs() << "- - - - - Deleting " << deadInst << *deadInst << "\n";
                //deadInst->eraseFromParent();
            }

            // Return
            return changed;
        }

        // =============================
    };
}

char deadCodeElimination::ID = 0;
static RegisterPass<deadCodeElimination> X( "deadCodeElimination", "Dead Code Elimination Pass", false, false );
