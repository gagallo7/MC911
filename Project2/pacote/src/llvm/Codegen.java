// =============================================================================================
// PACKAGES AND IMPORTS
// =============================================================================================
package llvm;

import semant.Env;
import syntaxtree.*;
import visitor.Visitor;
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

	private SymTab symTab;

    // Constutor
	public Codegen() {
		assembler = new LinkedList<LlvmInstruction>();
		symTab = new SymTab();
	}

    // =============================================================================================
	// ENTRADA DO CODEGEN
    // =============================================================================================
	public String translate(Program p, Env env) {
		System.out.println("[ AST ] : translate");
		codeGenerator = new Codegen();
        codeGenerator.symTab.FillTabSymbol( p );

		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add( new LlvmConstantDeclaration( "@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\"" ) );

		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add( new LlvmExternalDeclaration( "@printf", LlvmPrimitiveType.I32, pts ) );
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add( new LlvmExternalDeclaration( "@malloc", new LlvmPointer( LlvmPrimitiveType.I8 ), mallocpts ) );

		String r = new String();
		for (LlvmInstruction instr : codeGenerator.assembler)
        {
            System.out.println ( instr );
			r += instr + "\n";
        }

        // =====================================
        // Gerando RI
        // =====================================
        codeGenerator.symTab.print();

        System.out.println ( "|||||||||||||||||||||||||" );
        for ( String key : codeGenerator.symTab.methods.keySet() )
        {
            String[] parts = key.split("_");
            symTab.className = parts[1];

            MethodData aux = codeGenerator.symTab.methods.get ( key );
            String s = "";
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
                s += "\t" + arg + "_tmp = alloca " + aux.args.get ( arg ).toString() + "\n";
                s += "\tstore " + aux.args.get ( arg ).toString() + " " + arg + ", " + aux.args.get ( arg ).toString() + " * " + arg + "_tmp\n";
            }

            //s += "ret " + aux.returnType.toString() + " " 
            s += "}\n";

            System.out.println ( s );
        }
        System.out.println ( "|||||||||||||||||||||||||\n" );
        // TODO

        // =====================================

        // Exit
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
		System.out.println("[ AST ] : MainClass");

		// Definicao da Main
		assembler.add(new LlvmDefine( "@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>() ) );
		assembler.add( new LlvmLabel( new LlvmLabelValue( "entry" ) ) );
		LlvmRegister R1 = new LlvmRegister( new LlvmPointer( LlvmPrimitiveType.I32 ) );
		assembler.add( new LlvmAlloca( R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>() ) );
		assembler.add( new LlvmStore( new LlvmIntegerLiteral( 0 ), R1 ) );

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por
		// exemplo, a classe "Print".
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
		System.out.println("[ AST ] : Plus");

		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Print n) {
		System.out.println("[ AST ] : Print");

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
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(IntegerLiteral n) {
		System.out.println("[ AST ] : IntegerLiteral");
		return new LlvmIntegerLiteral(n.value);
	};

    // =============================================================================================
	// Todos os visit's que devem ser implementados
	public LlvmValue visit(ClassDeclSimple n) {
		System.out.println("[ AST ] : ClassDeclSimple: " + n.name.toString() );

        symTab.className = n.name.toString();

		ListConverter<VarDecl> converter0 = new ListConverter<VarDecl>();
        List<VarDecl> varList = converter0.getTList( n.varList );
        List<LlvmType> attr_aux = new LinkedList<LlvmType>();

        for ( VarDecl var : varList )
        {
            LlvmValue tmp = var.accept (this);
            attr_aux.add( tmp.type );
        }
        LlvmStructure struct_attr = new LlvmStructure( attr_aux );

        ListConverter<MethodDecl> converter1 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter1.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            method.accept(this);
        }

        ClassType name_aux = new ClassType( symTab.className );
        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( name_aux.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(ClassDeclExtends n) {
		System.out.println("[ AST ] : ClassDeclExtends");

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

        ListConverter<MethodDecl> converter0 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter0.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            method.accept(this);
        }

        ClassType name_aux = new ClassType( symTab.className );
        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( name_aux.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(VarDecl n) {
		System.out.println("[ AST ] : VarDecl");
        //LlvmValue newReg = new LlvmRegister ( n.name.toString(), n.type.accept(this).type )
        return n.type.accept(this);
        //return newReg;
	}

    // =============================================================================================
	public LlvmValue visit(MethodDecl n) {
		System.out.println("[ AST ] : MethodDecl");

		ListConverter<Formal> converter0 = new ListConverter<Formal>();
        List < Formal > FormalList = converter0.getTList ( n.formals );

        for ( Formal formal : FormalList )
        {
            formal.accept(this);
        }

        ListConverter<Statement> converter1 = new ListConverter<Statement>();
        List<Statement> bodyList = converter1.getTList( n.body );

        for ( Statement stmt : bodyList )
        {
            stmt.accept(this);
        }

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Formal n) {
		System.out.println("[ AST ] : Formal");

        LlvmRegister R1 = new LlvmRegister("%"+n.name.accept(this).toString(), LlvmPrimitiveType.I32);

		return R1;
	}

    // =============================================================================================
	public LlvmValue visit(IntArrayType n) {
		System.out.println("[ AST ] : IntArrayType");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(BooleanType n) {
		System.out.println("[ AST ] : BooleanType");
        System.out.println ( "BoolType: " + n.toString() );
        return new LlvmBool ( 0 );
		//return new LlvmBool ();
	}

    // =============================================================================================
	public LlvmValue visit(IntegerType n) {
		System.out.println("[ AST ] : IntegerType");

		return new LlvmIntegerLiteral (0);
	}

    // =============================================================================================
	public LlvmValue visit(IdentifierType n) {
		System.out.println("[ AST ] : IdentifierType");

        return new LlvmRegister ( n.name, new LlvmPointer ( new ClassType ( n.name ) ) );
	}

    // =============================================================================================
	public LlvmValue visit(Block n) {
		System.out.println("[ AST ] : Block");
        
		ListConverter<Statement> converter0 = new ListConverter<Statement>();
        List<Statement> blockList = converter0.getTList( n.body );

        for ( Statement stmt : blockList )
        {
            stmt.accept(this);
        }
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(If n) {
        LlvmLabelValue eElse;
        LlvmLabelValue endIf = new LlvmLabelValue( "entryEndIf" );
		System.out.println("[ AST ] : If");
        LlvmValue cond = n.condition.accept(this);
        System.out.println ( "condition no If: " + cond );

        /*
		LlvmType type = n.condition.type.accept(this).type;
        System.out.println ( "Tipo no If: " + type );
        */

        LlvmLabelValue eThen = new LlvmLabelValue ( "entryThen" );
        if ( n.elseClause != null )
        {
            eElse = new LlvmLabelValue ( "entryElse");
        }
        else
        {
            eElse = null;
        }

       // assembler.add ( new LlvmBranch ( new LlvmRegister ( LlvmPrimitiveType.I32 ), eThen, eElse ) );
        assembler.add ( new LlvmBranch ( cond, eThen, eElse ) );

		assembler.add( new LlvmLabel( new LlvmLabelValue( "entryThen" ) ) );
        n.thenClause.accept(this);
        assembler.add ( new LlvmBranch ( endIf ) );

        if ( n.elseClause != null )
        {
            assembler.add( new LlvmLabel( new LlvmLabelValue( "entryElse" ) ) );
            n.elseClause.accept(this);
        }

		assembler.add( new LlvmLabel( endIf ) );
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		//assembler.add(new LlvmIcmp(lhs, LlvmPrimitiveType.I32, type, v1, v2));
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(While n) {
		System.out.println("[ AST ] : While");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Assign n) {
		System.out.println("[ AST ] : Assign");
        System.out.println ( "var = " + n.var +  " exp = " +  n.exp.toString() );
        LlvmValue lhs = n.var.accept(this);
        n.exp.accept(this);

		return null;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayAssign n) {
		System.out.println("[ AST ] : ArrayAssign");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(And n) {
		System.out.println("[ AST ] : And");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(LessThan n) {
		System.out.println("[ AST ] : LessThan");
		LlvmValue l1 = n.lhs.accept(this);
		LlvmValue l2 = n.rhs.accept(this);
		LlvmType type = n.type.accept(this).type;
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmIcmp( new LlvmRegister ( LlvmPrimitiveType.I32 ), 0, type, l1, l2));
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Minus n) {
		System.out.println("[ AST ] : Minus");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(Times n) {
		System.out.println("[ AST ] : Times");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayLookup n) {
		System.out.println("[ AST ] : ArrayLookup");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(ArrayLength n) {
		System.out.println("[ AST ] : ArrayLength");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Call n) {
		System.out.println("[ AST ] : Call");
        //assembler.add ( new LlvmCall ( new LlvmRegister ( n.method.accept(this).type ), new LlvmMalloc (  
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(True n) {
		System.out.println("[ AST ] : True");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(False n) {
		System.out.println("[ AST ] : False");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(IdentifierExp n) {
		System.out.println("[ AST ] : IdentifierExp");
        // Criando um LlvmNamedValue para criar um identificador
        return new LlvmNamedValue ( n.name.accept(this).toString(),n.type.accept(this).type );
	}

    // =============================================================================================
	public LlvmValue visit(This n) {
		System.out.println("[ AST ] : This");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(NewArray n) {
		System.out.println("[ AST ] : NewArray");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(NewObject n) {
		System.out.println("[ AST ] : NewObject");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Not n) {
		System.out.println("[ AST ] : Not");
		return null;
	}

    // =============================================================================================
	public LlvmValue visit(Identifier n) {
		System.out.println("[ AST ] : Identifier");
		return new LlvmLabelValue ( n.s );
	}
}


// =============================================================================================
// TABELA DE SIMBOLOS
// =============================================================================================
class SymTab extends VisitorAdapter{
    private Map<String, ClassData> classes;     
	public ClassData classEnv;
    public String className, methodName;
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
        System.out.println( "====================================" );
        System.out.println( "Printing SymTab..." );
        System.out.println( "====================================" );

        for ( String key : this.classes.keySet() ) 
        {
            if ( key.isEmpty() ) break;
            System.out.println( "\n------ " + key + " ------" );
            this.classes.get( key ).print();
        }

        System.out.println( "\n====================================\n" );
    }

    public String getOffset( String whichClass, String whichData ) 
    {
        ClassData aux = getClassData( whichClass );
        String offset = aux.getOffset( whichData );
        
        if ( offset.isEmpty() )
            return "0, " + getOffset( aux.getParent(), whichData );

        return offset;
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
        System.out.println("[ SymTab ] : ClassDeclSimple");

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
        System.out.println("[ SymTab ] : ClassDeclExtends");

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
        LlvmRegister R1 = new LlvmRegister("%"+n.name.toString(), LlvmPrimitiveType.I32);

		return R1;
    }

    // =============================================================================================
    public LlvmValue visit(MethodDecl n){
        System.out.println("[ SymTab ] : MethodDecl ");

		ListConverter<Formal> converter0 = new ListConverter<Formal>();
        List < Formal > FormalList = converter0.getTList ( n.formals );
        methodName = n.name.toString() + "_" + this.className;
        methodEnv.returnType = n.returnType.accept ( this ).type;

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
        return null;
    }

    // =============================================================================================
    public LlvmValue visit(BooleanType n){
        System.out.println("[ SymTab ] : BooleanType ");
        return null;
    }

    // =============================================================================================
    public LlvmValue visit(IntegerType n){
        System.out.println("[ SymTab ] : IntegerType ");
		return new LlvmIntegerLiteral (0);
    }

    // =============================================================================================
}
