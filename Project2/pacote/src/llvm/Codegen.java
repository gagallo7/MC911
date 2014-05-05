// =============================================================================================
// PACKAGES AND IMPORTS
// =============================================================================================
package llvm;

import semant.Env;
import syntaxtree.*;
//import visitor.Visitor;
import llvmast.*;
import llvmutility.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

// =============================================================================================
// CODEGEN
// =============================================================================================
public class Codegen extends VisitorAdapter {
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;
    private String tab = "";

	private static SymTab symTab;

	// Globais auxiliares
    static int numberIf = 0;
    static int numberWhile = 0;

    // Auxiliares do Call
    String objectClassName;

    // Constutor
	public Codegen() {
		this.assembler = new LinkedList<LlvmInstruction>();
		this.symTab = new SymTab();
	}

    // =============================================================================================
	// ENTRADA DO CODEGEN
    // =============================================================================================
	public String translate(Program p, Env env) {
		System.out.println("[ AST ]" + tab + " : translate"); 
 	    tab += "\t";
		codeGenerator = new Codegen();

		// Preenche e imprime a tabela de simbolos
        codeGenerator.symTab.FillTabSymbol( p );
        codeGenerator.symTab.print();

		// Formato da String para o System.out.printlnijava "%d\n"
		assembler.add( new LlvmConstantDeclaration( "@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\"" ) );

		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		assembler.add( new LlvmExternalDeclaration( "@printf", LlvmPrimitiveType.I32, pts ) );
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		assembler.add( new LlvmExternalDeclaration( "@malloc", new LlvmPointer( LlvmPrimitiveType.I8 ), mallocpts ) );

        // =====================================
        // Gerando RI
        // =====================================
        System.out.println( "\nGerando métodos em RI...\n" );

        for ( String key : codeGenerator.symTab.methods.keySet() )
        {
            // Assinatura em RI do método
            String[] parts = key.split("_");
            symTab.className = parts[1];

            codeGenerator.symTab.methodEnv = codeGenerator.symTab.methods.get ( key );
            MethodData aux = codeGenerator.symTab.methodEnv;

            String s = "\n";
            s += "define " + aux.returnType.toString() + " @" + key + " ( %class." + symTab.className + " * %this";

            // Gerando argumentos do método
            for ( String arg : aux.args.keySet() )
            {
                s += ", " + aux.args.get ( arg ).toString() + " " + arg;
            }

            s += " ) {\n";

            s += "entry0:\n";

            // Alocando argumentos
            for ( String arg : aux.args.keySet() )
            {
                LlvmType argType = aux.args.get( arg );
                LlvmRegister regArg = new LlvmRegister( arg, argType );
                LlvmRegister regArgTmp = new LlvmRegister( arg + "_tmp", new LlvmPointer( argType ) );

                s += new LlvmAlloca( regArgTmp, regArg.type, new LinkedList<LlvmValue>() ).toString();
                s += "\n";
                s += new LlvmStore( regArg, regArgTmp ).toString();
                s += "\n";
            }

            // Alocando variaveis locais
            for ( String localName : aux.locals.keySet() ) 
            {
                LlvmType localType = aux.locals.get( localName );
                LlvmRegister regLocal = new LlvmRegister( "%" + localName, localType );

                s += new LlvmAlloca( regLocal, localType, new LinkedList<LlvmValue>() ).toString();
                s += "\n";
            }

            // Escreve código RI no .s
            assembler.add( new Compile( s ) );

            // Corpo do método. Os visits do AST escrevem o corpo do método
            for ( Statement meth_stmt : aux.statements ) 
            {
                meth_stmt.accept( this );
            } 

            // Return expression
            assembler.add( new LlvmRet( aux.returnExp.accept( this ) ) ); 

            // Fecha bloco do método
            assembler.add( new Compile ( "}\n" ) );
        }

        // =====================================

		// Aqui o codigo eh gerado de fato, a partir do assembler
		String r = new String();
		for (LlvmInstruction instr : codeGenerator.assembler)
        {
			r += instr + "\n";
        }

		for (LlvmInstruction instr : assembler)
        {
			r += instr + "\n";
        }

        // Exit
        System.out.println( "\n\nFIM DA TRADUCAO\n" );
        tab = tab.substring(0, tab.length() - 1);
		return r;
	}

    // =============================================================================================
	public LlvmValue visit(Program n) {
		n.mainClass.accept(this);

        for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
            c.head.accept(this);

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(MainClass n) {
		System.out.println("[ AST ]" + tab + " : MainClass" ); 
 	    tab += "\t";

 	    // Definicao de simbolos
 	    symTab.className = "main";

		// Definicao da Main
		assembler.add(new LlvmDefine( "@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>() ) );
		assembler.add( new LlvmLabel( new LlvmLabelValue( "entry" ) ) );
		LlvmRegister R1 = new LlvmRegister( new LlvmPointer( LlvmPrimitiveType.I32 ) );
		assembler.add( new LlvmAlloca( R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>() ) );
		assembler.add( new LlvmStore( new LlvmIntegerLiteral( 0 ), R1 ) );

        // Chamando o Statement da Main
		n.stm.accept(this);

		// Final do Main
		LlvmRegister R2 = new LlvmRegister( LlvmPrimitiveType.I32 );
		assembler.add( new LlvmLoad( R2, R1 ) );
		assembler.add( new LlvmRet( R2 ) );
		assembler.add( new LlvmCloseDefinition() );
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Plus n) {
		System.out.println("[ AST ]" + tab + " : Plus"); 
 	    tab += "\t";

		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs, LlvmPrimitiveType.I32, v1, v2));
        tab = tab.substring(0, tab.length() - 1);
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Print n) {
		System.out.println("[ AST ]" + tab + " : Print"); 
 	    tab += "\t";

		LlvmValue v = n.exp.accept(this);

		// getelementptr:
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer( LlvmPrimitiveType.I8) );
		LlvmRegister src = new LlvmNamedValue("@.formatting.string", new LlvmPointer( new LlvmArray(4, LlvmPrimitiveType.I8) ) );
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(lhs);
		args.add(v);
		assembler.add(new LlvmGetElementPointer(lhs, src, offsets));

		pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);

		// printf:
		assembler.add( new LlvmCall( new LlvmRegister( LlvmPrimitiveType.I32 ), LlvmPrimitiveType.I32, pts, "@printf", args ) );
        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(IntegerLiteral n) {
		System.out.println( "[ AST ]" + tab + " : IntegerLiteral -> " + n.toString() ); 
		return new LlvmIntegerLiteral(n.value);
	};

    // =============================================================================================
	// Todos os visit's que devem ser implementados
	public LlvmValue visit(ClassDeclSimple n) {
		System.out.println("[ AST ]" + tab + " : ClassDeclSimple: " + n.name.toString() ); 
 	    tab += "\t";

 	    // Inicializando variaveis globais auxiliares
        symTab.className = n.name.toString();

		ListConverter<VarDecl> converter0 = new ListConverter<VarDecl>();
        List<VarDecl> varList = converter0.getTList( n.varList );
        List<LlvmType> attr_aux = new LinkedList<LlvmType>();

        for ( VarDecl var : varList )
        {
            LlvmValue tmp = var.accept(this);
            attr_aux.add( tmp.type );
        }
        LlvmStructure struct_attr = new LlvmStructure( attr_aux );

        /*
         *        ListConverter<MethodDecl> converter1 = new ListConverter<MethodDecl>();
         *        List<MethodDecl> methodList = converter1.getTList( n.methodList );
         *
         *        for ( MethodDecl method : methodList )
         *        {
         *            method.accept(this);
         *        }
         */

        ClassType name_aux = new ClassType( symTab.className );
        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( name_aux.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(ClassDeclExtends n) {
		System.out.println("[ AST ]" + tab + " : ClassDeclExtends: " + n.name.toString() ); 
 	    tab += "\t";

		ListConverter<VarDecl> util = new ListConverter<VarDecl>();
        List<VarDecl> varList = util.getTList( n.varList );

        List<LlvmType> attr_aux = new LinkedList<LlvmType>();

        // Starting a new class info. Here, we have no parent and offset is unnecessary
        String parent = n.superClass.toString();
        symTab.className = n.name.toString();

        attr_aux.add( new ClassType ( parent ) );
        for ( VarDecl var : varList )
        {
            LlvmValue tmp = var.accept (this);
            attr_aux.add( tmp.type );
        }
        LlvmStructure struct_attr = new LlvmStructure( attr_aux );

        /*
         *        ListConverter<MethodDecl> converter0 = new ListConverter<MethodDecl>();
         *        List<MethodDecl> methodList = converter0.getTList( n.methodList );
         *
         *        for ( MethodDecl method : methodList )
         *        {
         *            method.accept(this);
         *        }
         */

        ClassType name_aux = new ClassType( symTab.className );
        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( name_aux.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(VarDecl n) {
		System.out.println("[ AST ]" + tab + " : VarDecl"); 
        //LlvmValue newReg = new LlvmRegister ( n.name.toString(), n.type.accept(this).type )
        return n.type.accept(this);
        //return newReg;
	}

    // =============================================================================================
	public LlvmValue visit(MethodDecl n) {
		System.out.println("[ AST ]" + tab + " : MethodDecl"); 
 	    tab += "\t";

        /*
         *        ListConverter<Formal> converter0 = new ListConverter<Formal>();
         *        List < Formal > FormalList = converter0.getTList ( n.formals );
         *
         *        for ( Formal formal : FormalList )
         *        {
         *            formal.accept(this);
         *        }
         *
         *        ListConverter<Statement> converter1 = new ListConverter<Statement>();
         *        List<Statement> bodyList = converter1.getTList( n.body );
         *
         *        for ( Statement stmt : bodyList )
         *        {
         *            stmt.accept(this);
         *        }
         */

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Formal n) {
		System.out.println("[ AST ]" + tab + " : Formal"); 
 	    tab += "\t";

        LlvmRegister R1 = new LlvmRegister("%"+n.name.accept(this).toString(), LlvmPrimitiveType.I32);

        tab = tab.substring(0, tab.length() - 1);
		return R1;
	}

    // =============================================================================================
	public LlvmValue visit(IntArrayType n) {
		System.out.println("[ AST ]" + tab + " : IntArrayType"); 
        LlvmRegister tmp = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        //LlvmRegister tmp = new LlvmRegister ( LlvmPrimitiveType.I32 );
		return tmp;
	}

    // =============================================================================================
	public LlvmValue visit(BooleanType n) {
		System.out.println("[ AST ]" + tab + " : BooleanType"); 
   //     System.out.println ( "BoolType: " + n.toString() );
        return new LlvmBool ( 0 );
	}

    // =============================================================================================
	public LlvmValue visit(IntegerType n) {
		System.out.println( "[ AST ]" + tab + " : IntegerType -> " + n.toString() ); 

		return new LlvmIntegerLiteral (0);
	}

    // =============================================================================================
	public LlvmValue visit(IdentifierType n) {
		System.out.println("[ AST ]" + tab + " : IdentifierType: " + n.toString() ); 

        return new LlvmRegister ( n.name, new LlvmPointer ( new ClassType ( n.name ) ) );
	}

    // =============================================================================================
	public LlvmValue visit(Block n) {
		System.out.println("[ AST ]" + tab + " : Block"); 
 	    tab += "\t";

		ListConverter<Statement> converter0 = new ListConverter<Statement>();
        List<Statement> blockList = converter0.getTList( n.body );

        for ( Statement stmt : blockList )
        {
            stmt.accept(this);
        }
        tab = tab.substring(0, tab.length() - 1);

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(If n) {
		System.out.println("[ AST ]" + tab + " : If"); 
        tab += "\t";

        // Identificação única
        numberWhile++;
        numberIf++;

        // Criando labels para cada rumo da condição
        LlvmLabelValue eElse;
        LlvmLabelValue endIf = new LlvmLabelValue( "entryEndIf" + numberIf );

        // Aceitando condição
        LlvmValue cond = n.condition.accept(this);
        LlvmLabelValue eThen = new LlvmLabelValue ( "entryThen" + numberIf );

        // Verificando se há cláusula else
        if ( n.elseClause != null )
        {
            eElse = new LlvmLabelValue ( "entryElse" + numberIf );
        }
        else
        {
            eElse = null;
        }

        assembler.add ( new LlvmBranch ( cond, eThen, eElse ) );

		assembler.add( new LlvmLabel( eThen ) );
        n.thenClause.accept(this);
        System.out.println ( "Accepting: " + n.thenClause );
        assembler.add ( new LlvmBranch ( endIf ) );

        if ( n.elseClause != null )
        {
            assembler.add( new LlvmLabel( eElse ) );
            n.elseClause.accept(this);
            System.out.println ( "Accepting: " + n.elseClause );
            assembler.add ( new LlvmBranch ( endIf ) );
        }

		assembler.add( new LlvmLabel( endIf ) );

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(While n) {
		System.out.println("[ AST ]" + tab + " : While"); 
        tab += "\t";

        // Identificação única
        numberWhile++;

        // Criando labels para cada rumo da condição
        LlvmLabelValue whileLoop = new LlvmLabelValue( "entryWhile" + numberWhile );
        LlvmLabelValue endWhile = new LlvmLabelValue( "entryEndWhile" + numberWhile );

        // Aceitando condição
        LlvmValue cond = n.condition.accept(this);
        assembler.add ( new LlvmBranch ( cond, whileLoop, endWhile ) );

        // ---------loop

        // Marcando começo do bloco
        assembler.add ( new LlvmLabel ( whileLoop ) );
        
        // Aceitando bloco
        n.body.accept(this);

        // Aceitando condição para nova iteração
        cond = n.condition.accept(this);
        assembler.add ( new LlvmBranch ( cond, whileLoop, endWhile ) );

        // ---------loop
        
        // Marcando fim do bloco
        assembler.add ( new LlvmLabel ( endWhile ) );

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Assign n) {
		System.out.println( "[ AST ]" + tab + " : Assign -> " + n.toString() ); 
 	    tab += "\t";

        LlvmValue rhs = n.exp.accept( this );
        LlvmValue lhs = n.var.accept( this );

        assembler.add( new LlvmStore( rhs, lhs ) );

        // Exit
        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayAssign n) {
		System.out.println("[ AST ]" + tab + " : ArrayAssign of var -> " + n.var.accept(this).type ) ; 
 	    tab += "\t";

        assembler.add ( new Compile ( "\n;assigning array" ) );
        // Value é o valor a ser atribuído
        LlvmValue value = n.value.accept(this);

        // Adicionando índice à lista de índices
        LlvmValue index = n.index.accept (this);
        List < LlvmValue > indices = new LinkedList < LlvmValue > ();
        indices.add ( index );

        // source recebe a variável no escopo correto
        LlvmValue sourcePtr = n.var.accept (this);
        LlvmRegister source = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        assembler.add ( new LlvmLoad ( source, sourcePtr ) );

        // dest recebe apontador para 'source [ index ]'
        LlvmRegister destPtr = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        assembler.add ( new LlvmGetElementPointer ( destPtr, source, indices ) );

        // Atribuindo valor ao destino
        assembler.add( new LlvmStore( value, destPtr ) );
        assembler.add ( new Compile ( "" ) );

        tab = tab.substring(0, tab.length() - 1);
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(And n) {
		System.out.println("[ AST ]" + tab + " : And"); 
        System.out.println ( n.lhs + " = and " + n.rhs );
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(LessThan n) {
		System.out.println("[ AST ]" + tab + " : LessThan"); 
 	    tab += "\t";
		LlvmValue l1 = n.lhs.accept(this);
		LlvmValue l2 = n.rhs.accept(this);
		LlvmType type = n.type.accept(this).type;
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmIcmp( lhs, 0, type, l1, l2));
        tab = tab.substring(0, tab.length() - 1);
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Minus n) {
		System.out.println("[ AST ]" + tab + " : Minus"); 
 	    tab += "\t";
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs, LlvmPrimitiveType.I32, v1, v2));
        tab = tab.substring(0, tab.length() - 1);
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Times n) {
		System.out.println("[ AST ]" + tab + " : Times"); 
 	    tab += "\t";
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);

        assembler.add( new LlvmTimes( lhs, LlvmPrimitiveType.I32, v1, v2 ) );

        tab = tab.substring(0, tab.length() - 1);
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayLookup n) {
		System.out.println("[ AST ]" + tab + " : ArrayLookup"); 
 	    tab += "\t";

        // Adicionando índice à lista de índices
        List < LlvmValue> tmp = new LinkedList < LlvmValue > ( );
        tmp.add ( n.index.accept(this) );

        // Criando ponteiro do lookup e o registrador para referenciá-lo
        LlvmValue regPtr = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        LlvmValue reg= new LlvmRegister ( LlvmPrimitiveType.I32 );

        assembler.add ( new LlvmGetElementPointer ( regPtr, n.array.accept(this), tmp ) );
        assembler.add ( new LlvmLoad ( reg, regPtr ) );

        tab = tab.substring(0, tab.length() - 1);
        return reg;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayLength n) {
		System.out.println("[ AST ]" + tab + " : ArrayLength"); 
 	    tab += "\t";
        n.array.accept(this);
        tab = tab.substring(0, tab.length() - 1);
		return LlvmMalloc.lastArraySize;
	}

    // =============================================================================================
	public LlvmValue visit(Call n) {
		System.out.println("[ AST ]" + tab + " : Call : " + n.toString() ); 
 	    tab += "\t";
		
		String className;
		LlvmValue objectReg = n.object.accept( this );
        System.out.println ( "Calling " + n.method );

        String[] classObject = n.toString().split( "\\." );
        String objectName = classObject[ 0 ];
        String[] obj_aux = objectName.split( " " );

        if ( obj_aux[ 0 ].equals( "new" ) ) 
        {
            className = objectClassName;

        } else 
        {
            String varCase = symTab.methodEnv.getVarCase( obj_aux[ 0 ] );
            ClassType objectType;
            LlvmPointer ptr;
            
            if ( varCase == "local" ) 
            {
                ptr = (LlvmPointer) symTab.methodEnv.getLocal( obj_aux[ 0 ] );
                objectType = (ClassType) ptr.content;

            } else if ( varCase == "arg" ) 
            {
                ptr = (LlvmPointer) symTab.methodEnv.getArg( "%" + obj_aux[ 0 ] );
                objectType = (ClassType) ptr.content;

            } else 
            {
                // Se nao, é um atributo da classe, ou do pai, ou do avo...
                ptr = (LlvmPointer) symTab.getAttributeType( symTab.methodEnv.myClass, obj_aux[ 0 ] );
                objectType = (ClassType) ptr.content;
            }

            if ( objectType == null )
                className = "ERROR";
            else
                className = objectType.getName();
        }

		symTab.methodName = n.method.toString() + "_" + className;

		ListConverter<Exp> converter0 = new ListConverter<Exp>();
        List<Exp> argList = converter0.getTList( n.actuals );
        List<LlvmValue> args = new LinkedList<LlvmValue>();

        ClassType class_aux = new ClassType( className );
        LlvmNamedValue named_aux = new LlvmNamedValue( objectReg.toString(), new LlvmPointer( class_aux ) );

        args.add( named_aux );
        for ( Exp exp : argList ) 
        {
            args.add( exp.accept(this) );
        }

        System.out.println ( "Trying to reach " + className + " " + symTab.methodName );
        MethodData meth_aux  = (MethodData) symTab.getClassData( className ).get( symTab.methodName );

        LlvmType type_aux = symTab.methodEnv.returnType;

        LlvmRegister retReg = new LlvmRegister ( type_aux );
        assembler.add ( new LlvmCall ( retReg, meth_aux.returnType, "@" + symTab.methodName, args ) );

        tab = tab.substring(0, tab.length() - 1);
		return retReg;
	}

    // =============================================================================================
	public LlvmValue visit(True n) {
		System.out.println("[ AST ]" + tab + " : True"); 
		return new LlvmBool ( 1 );
	}

    // =============================================================================================
	public LlvmValue visit(False n) {
		System.out.println("[ AST ]" + tab + " : False"); 
		return new LlvmBool ( 0 );
	}

    // =============================================================================================
	public LlvmValue visit(IdentifierExp n) {
		System.out.println( "[ AST ]" + tab + " : IdentifierExp -> " + n.toString() ); 
        tab += "\t";

		LlvmType type = n.type.accept( this ).type;
		LlvmRegister reg = new LlvmRegister( type );
		LlvmRegister rhs;

        String regName;
        String varCase = codeGenerator.symTab.methodEnv.getVarCase( n.toString() );

        if ( varCase == "local" ) 
        {
            regName = "%" + n.toString();
            rhs = new LlvmRegister( regName, new LlvmPointer( type ) );

        } else if ( varCase == "arg" ) 
        {
            regName = "%" + n.toString() + "_tmp";
            rhs = new LlvmRegister( regName, new LlvmPointer( type ) );

        } else 
        {
            // Se nao, é um atributo da classe, ou do pai, ou do avo...
            ClassData class_aux = codeGenerator.symTab.getClassData( codeGenerator.symTab.className );
            Data aux;

            while( true ) 
            {
                aux = class_aux.get( n.toString() );

                if ( aux == null ) 
                {
                    String parent = codeGenerator.symTab.getClassData( codeGenerator.symTab.className ).getParent();
                    class_aux = codeGenerator.symTab.getClassData( parent );

                } else 
                {
                    break;
                }
            }

            AttributeData attr_aux = (AttributeData) aux;
            type = attr_aux.type;

            rhs = new LlvmRegister( new LlvmPointer( type ) );
            ClassType classType = new ClassType( codeGenerator.symTab.className );

            String s = "\t" + rhs.toString() + " = getelementptr " + classType.toString() + " * %this, i32 0, ";
            String offset = codeGenerator.symTab.getOffset( codeGenerator.symTab.className, n.toString() );
            
            assembler.add( new Compile( s + offset ) );
        }

		assembler.add( new LlvmLoad( reg, rhs ) );

        tab = tab.substring(0, tab.length() - 1);
        return reg;
	}

    // =============================================================================================
	public LlvmValue visit(This n) {
		System.out.println("[ AST ]" + tab + " : This"); 
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(NewArray n) {
		System.out.println("[ AST ]" + tab + " : NewArray"); 
 	    tab += "\t";

        //LlvmType type = new LlvmPointer ( LlvmPrimitiveType.I32 );
        LlvmType type = LlvmPrimitiveType.I32;
        LlvmValue size = n.size.accept(this);
        symTab.currentArraySize = size.toString(); //TODO
        LlvmRegister reg = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        LlvmRegister tmp = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );

        // Guardando valor do último array mallocado
        LlvmMalloc.lastArraySize = size;

        // Calculando os bytes necessários para alocar o vetor de inteiros
        assembler.add ( new Compile ( "\t" + tmp + " = mul i32 4, " + size ) );
        assembler.add ( new LlvmMalloc ( reg, tmp, type ) );
        assembler.add ( new Compile ( "" ) );
        tab = tab.substring(0, tab.length() - 1);

		return reg;
	}

    // =============================================================================================
	public LlvmValue visit(NewObject n) {
		System.out.println("[ AST ]" + tab + " : NewObject : " + n.toString() + "\tClass : " + n.className.toString() ); 
        tab += "\t";

        // Instanciando o objeto
        LlvmType type = new LlvmPointer( new ClassType( n.className.toString() ) );
        LlvmRegister reg = new LlvmRegister( type );
        LlvmRegister regSizePointer = new LlvmRegister( new LlvmPointer( LlvmPrimitiveType.I32 ) );
        LlvmRegister regSizeContent = new LlvmRegister( LlvmPrimitiveType.I32 );
        LlvmIntegerLiteral objectSize = new LlvmIntegerLiteral( symTab.getClassSize( n.className.toString() ) );

        // Gerando RI
        assembler.add( new LlvmAlloca( regSizePointer, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>() ) );
        assembler.add( new LlvmStore( objectSize, regSizePointer ) );
        assembler.add( new LlvmLoad( regSizeContent, regSizePointer ) );
        assembler.add( new LlvmMalloc( reg, regSizeContent, new ClassType( n.className.toString() ) ) );

        // Guardando referencia do objeto
        objectClassName = n.className.toString();

        // Exit
        tab = tab.substring(0, tab.length() - 1);

		return reg;
	}

    // =============================================================================================
    // Faz a negação da condição
	public LlvmValue visit(Not n) {
		System.out.println("[ AST ]" + tab + " : Not -> " + n);
        tab += "\t";

        // Obtendo valor da condição dual
        LlvmValue cond = n.exp.accept (this);
        LlvmValue reg = new LlvmRegister ( LlvmPrimitiveType.I1 );

        // Fazendo um xor com 1 para negar o valor binário
        assembler.add ( new Compile ( reg + " = xor i1 1, " + cond ) );

        tab = tab.substring(0, tab.length() - 1);

        return reg;
	}

    // =============================================================================================
	public LlvmValue visit(Identifier n) {
		System.out.println( "[ AST ]" + tab + " : Identifier -> " + n.toString() ); 
        tab += "\t";

		LlvmType type;
        String regName;
        String varCase = codeGenerator.symTab.methodEnv.getVarCase( n.toString() );
        LlvmRegister reg;

        if ( varCase == "local" ) 
        {
            regName = "%" + n.toString();
            type = codeGenerator.symTab.methodEnv.getLocal( n.toString() );
            reg = new LlvmRegister( regName, new LlvmPointer( type ) );

        } else if ( varCase == "arg" ) 
        {
            regName = "%" + n.toString() + "_tmp";
            type = codeGenerator.symTab.methodEnv.getArg( "%" + n.toString() );
            reg = new LlvmRegister( regName, new LlvmPointer( type ) );

        } else 
        {
            // Se nao, é um atributo da classe, ou do pai, ou do avo...
            ClassData class_aux = codeGenerator.symTab.getClassData( codeGenerator.symTab.className );
            Data aux;

            while( true ) 
            {
                aux = class_aux.get( n.toString() );

                if ( aux == null ) 
                {
                    String parent = codeGenerator.symTab.getClassData( codeGenerator.symTab.className ).getParent();
                    class_aux = codeGenerator.symTab.getClassData( parent );

                } else 
                {
                    break;
                }
            }

            AttributeData attr_aux = (AttributeData) aux;
            type = attr_aux.type;

            reg = new LlvmRegister( new LlvmPointer( type ) );
            ClassType classType = new ClassType( codeGenerator.symTab.className );

            String s = "\t" + reg.toString() + " = getelementptr " + classType.toString() + " * %this, i32 0, ";
            String offset = codeGenerator.symTab.getOffset( codeGenerator.symTab.className, n.toString() );
            
            assembler.add( new Compile( s + offset ) );
        }

        tab = tab.substring(0, tab.length() - 1);
		return reg;
	}
}


// =============================================================================================
// TABELA DE SIMBOLOS
// =============================================================================================
class SymTab extends VisitorAdapter{
    private Map<String, ClassData> classes;     
	public ClassData classEnv;
    public String className, methodName, currentArraySize;
    public MethodData methodEnv;
    public Map < String, MethodData > methods;

    public SymTab() 
    {
        this.classes = new HashMap<String, ClassData>();
        this.methods = new HashMap < String, MethodData > ();
    }

    public void add( String className, ClassData whichData ) 
    {
        this.classes.put( className, whichData );
    }

    public ClassData getClassData( String whichClass ) 
    {
        return this.classes.get( whichClass );
    }

    public void print() 
    {
        System.out.println( "\n====================================" );
        System.out.println( "Printing SymTab..." );
        System.out.println( "====================================" );

        for ( String key : this.classes.keySet() ) 
        {
            if ( key.isEmpty() ) break;

            System.out.println( "\n*************************" );
            System.out.println( "CLASS: " + key );
            System.out.println( "*************************" );

            System.out.println( "Size: " + Integer.toString( this.getClassSize( key ) ) );
            this.classes.get( key ).print();
        }

        System.out.println( "\n====================================\n" );
    }

    public String getOffset( String whichClass, String whichData ) 
    {
        ClassData aux = getClassData( whichClass );

        if ( aux == null )
            return "";

        String offset = aux.getOffset( whichData );

        if ( offset.isEmpty() ) 
        {
            String offsetParent = getOffset( aux.getParent(), whichData );

            if ( offsetParent.isEmpty() )
                return "i32 0";

            return "i32 0, " + offsetParent;
        }

        return "i32 " + offset;
    }

    public int getClassSize( String whichClass ) 
    {
        ClassData aux = this.classes.get( whichClass );
        String parent = aux.getParent();

        if ( parent.isEmpty() )
            return aux.getSize();

        return aux.getSize() + getClassSize( parent );
    }

    public LlvmType getAttributeType( String whichClass, String whichAttr ) 
    {
        ClassData class_data = this.getClassData( whichClass );

        if ( class_data == null ) 
        {
            System.out.println( "SymTab getAttributeType( " + whichClass + " ) não encontrou a classe" );
            return null;
        }

        LlvmType type = class_data.getAttributeType( whichAttr );

        if ( type != null )
        {
            return type;
        }

        System.out.println( "SymTab getAttributeType vai procurar na classe pai : " + this.getClassData( whichClass ).getParent() );
        return this.getAttributeType( this.getClassData( whichClass ).getParent(), whichAttr );
    }

    // =============================================================================================
    public LlvmValue FillTabSymbol(Program n){
        n.accept(this);

        return null;
    }

    // =============================================================================================
    public LlvmValue visit(Program n){
        System.out.println("[ SymTab ] : Program");
        n.mainClass.accept(this);

        for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
            c.head.accept(this);

        return null;
    }

    // =============================================================================================

    public LlvmValue visit(MainClass n){
        System.out.println("[ SymTab ] : MainClass");
        return null;
    }

    // =============================================================================================
    public LlvmValue visit(ClassDeclSimple n){
        System.out.println("[ SymTab ] : ClassDeclSimple: " + n.name.toString() );

        // Starting a new class info. Here, we have no parent and offset is unnecessary
        classEnv = new ClassData();
        className = n.name.toString();
        int offset_aux = 0;

		ListConverter<VarDecl> converter0 = new ListConverter<VarDecl>();
        List<VarDecl> varList = converter0.getTList( n.varList );

        for ( VarDecl var : varList )
        {
            LlvmValue tmp = var.accept (this);

            // Adding a new attribute data
            AttributeData att_aux = new AttributeData( tmp.type, tmp, offset_aux );
            offset_aux++;
            classEnv.add( var.name.s, att_aux );
        }

        ListConverter<MethodDecl> converter1 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter1.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            methodEnv = new MethodData ();
            method.accept(this);
            classEnv.add( methodName, methodEnv );
            methods.put ( methodName, methodEnv );
        }

        // Store this class
        this.add( className, classEnv );

        return null;
    }

    // =============================================================================================
    public LlvmValue visit(ClassDeclExtends n){
        System.out.println("[ SymTab ] : ClassDeclExtends : " + n.name.toString() );

		ListConverter<VarDecl> util = new ListConverter<VarDecl>();
        List<VarDecl> varList = util.getTList( n.varList );

        // Starting a new class info. Here, we have no parent and offset is unnecessary
        String parent = n.superClass.toString();
        classEnv = new ClassData ( parent );
        className = n.name.toString();
        // != 0 -- Tem pai
        int offset_aux = 1;

        for ( VarDecl var : varList )
        {
            LlvmValue tmp = var.accept (this);

            // Adding a new attribute data
            AttributeData att_aux = new AttributeData( tmp.type, tmp, offset_aux );
            offset_aux++;
            classEnv.add( var.name.s, att_aux );
        }

        ListConverter<MethodDecl> converter0 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter0.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            methodEnv = new MethodData ();
            method.accept(this);
            classEnv.add( methodName, methodEnv );
            methods.put ( methodName, methodEnv );
        }

        // Store this class
        this.add( className, classEnv );

        return null;
    }

    // =============================================================================================
    public LlvmValue visit(VarDecl n){
        System.out.println("[ SymTab ] : VarDecl ");
        return n.type.accept(this);
    }

    // =============================================================================================
    public LlvmValue visit(Formal n){
        System.out.println("[ SymTab ] : Formal ");

        LlvmType type;
        String formalType = n.type.toString();

        System.out.println( "formalType = [" + formalType + "]" );

        if ( formalType.equals( "int " ) )
            type = LlvmPrimitiveType.I32;
        else
            type = new LlvmPointer( new ClassType( formalType ) );

        LlvmRegister R1 = new LlvmRegister( "%" + n.name.toString(), type );

		return R1;
    }

    // =============================================================================================
    public LlvmValue visit(MethodDecl n){
        System.out.println("[ SymTab ] : MethodDecl ");

		ListConverter<Formal> converter0 = new ListConverter<Formal>();
        List < Formal > FormalList = converter0.getTList ( n.formals );
        methodName = n.name.toString() + "_" + this.className;
        methodEnv.returnType = n.returnType.accept( this ).type;

        for ( Formal formal : FormalList )
        {
            LlvmValue formalAux = formal.accept(this);
            methodEnv.addArg ( formalAux.toString(), formalAux.type );
        }

        ListConverter<Statement> converter1 = new ListConverter<Statement>();
        List<Statement> bodyList = converter1.getTList( n.body );

        for ( Statement stmt : bodyList )
        {
            methodEnv.addStmt ( stmt );
        }
        methodEnv.returnExp = n.returnExp;
        methodEnv.myClass = className;

        ListConverter<VarDecl> converter2 = new ListConverter<VarDecl>();
        List<VarDecl> localList = converter2.getTList( n.locals );

        for ( VarDecl loc : localList ) 
        {
            LlvmValue aux = loc.accept( this );
            methodEnv.addLocal( loc.name.toString(), aux.type );
        }

        return null;
    }

    // =============================================================================================
    public LlvmValue visit(IdentifierType n){
        System.out.println("[ SymTab ] : IdentifierType ");
        return new LlvmRegister ( n.name, new LlvmPointer ( new ClassType ( n.name ) ) );
    }

    // =============================================================================================
    public LlvmValue visit(IntArrayType n){
        System.out.println("[ SymTab ] : IntArrayType ");
        LlvmRegister tmp = new LlvmRegister ( new LlvmPointer ( LlvmPrimitiveType.I32 ) );
        //LlvmRegister tmp = new LlvmRegister ( LlvmPrimitiveType.I8 );
		return tmp;
    }

    // =============================================================================================
    public LlvmValue visit(BooleanType n){
        System.out.println("[ SymTab ] : BooleanType ");
        return new LlvmBool ( 0 );
    }

    // =============================================================================================
    public LlvmValue visit(IntegerType n){
        System.out.println("[ SymTab ] : IntegerType ");
		return new LlvmIntegerLiteral (0);
    }

    // =============================================================================================
}
