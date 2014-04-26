/*****************************************************
Esta classe Codegen é a responsável por emitir LLVM-IR. 
Ela possui o mesmo método 'visit' sobrecarregado de
acordo com o tipo do parâmetro. Se o parâmentro for
do tipo 'While', o 'visit' emitirá código LLVM-IR que 
representa este comportamento. 
Alguns métodos 'visit' já estão prontos e, por isso,
a compilação do código abaixo já é possível.

class a{
    public static void main(String[] args){
    	System.out.println(1+2);
    }
}

O pacote 'llvmast' possui estruturas simples 
que auxiliam a geração de código em LLVM-IR. Quase todas 
as classes estão prontas; apenas as seguintes precisam ser 
implementadas: 

// llvmasm/LlvmBranch.java          DONE
// llvmasm/LlvmIcmp.java            DONE
// llvmasm/LlvmMinus.java           DONE
//llvmasm/LlvmTimes.java           DONE


Todas as assinaturas de métodos e construtores 
necessárias já estão lá. 


Observem todos os métodos e classes já implementados
e o manual do LLVM-IR (http://llvm.org/docs/LangRef.html) 
como guia no desenvolvimento deste projeto. 

 ****************************************************/
package llvm;

import semant.Env;
import syntaxtree.*;
import visitor.Visitor;
import llvmast.*;
import llvmutility.*;

import java.util.*;
//import util.*;

public class Codegen extends VisitorAdapter {
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

	private SymTab symTab;
	private ClassType classEnv; 	// Aponta para a classe atualmente em uso em symTab
	
	public Codegen() {
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env) {
		System.out.println("++++++++++AST: translate");
		codeGenerator = new Codegen();

		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		// codeGenerator.symTab.FillTabSymbol(p);
		
		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add(new LlvmConstantDeclaration(
				"@.formatting.string",
				"private constant [4 x i8] c\"%d\\0A\\00\""));

		// NOTA: sempre que X.accept(Y), então Y.visit(X);
		// NOTA: Logo, o comando abaixo irá chamar codeGenerator.visit(Program),
		// linha 75
		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@printf",
				LlvmPrimitiveType.I32, pts));
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@malloc",
				new LlvmPointer(LlvmPrimitiveType.I8), mallocpts));

		String r = new String();
		for (LlvmInstruction instr : codeGenerator.assembler)
			r += instr + "\n";
		return r;
	}

	public LlvmValue visit(Program n) {
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n) {
		System.out.println("++++++++++AST: MainClass");

		// definicao do main
		assembler.add(new LlvmDefine("@main", LlvmPrimitiveType.I32,
				new LinkedList<LlvmValue>()));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		LlvmRegister R1 = new LlvmRegister(new LlvmPointer(
				LlvmPrimitiveType.I32));
		assembler.add(new LlvmAlloca(R1, LlvmPrimitiveType.I32,
				new LinkedList<LlvmValue>()));
		assembler.add(new LlvmStore(new LlvmIntegerLiteral(0), R1));

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por
		// exemplo, a classe "Print".
		n.stm.accept(this);

		// Final do Main
		LlvmRegister R2 = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(R2, R1));
		assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}

	public LlvmValue visit(Plus n) {
		System.out.println("++++++++++AST: Plus");

		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

	public LlvmValue visit(Print n) {
		System.out.println("++++++++++AST: Print");

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
		assembler.add(new LlvmCall(new LlvmRegister(LlvmPrimitiveType.I32),
				LlvmPrimitiveType.I32, pts, "@printf", args));
		return null;
	}

	public LlvmValue visit(IntegerLiteral n) {
		System.out.println("++++++++++AST: IntegerLiteral");
		return new LlvmIntegerLiteral(n.value);
	};

	// Todos os visit's que devem ser implementados
	public LlvmValue visit(ClassDeclSimple n) {
		System.out.println("++++++++++AST: ClassDeclSimple: " + n.name.toString() );
        classEnv = new ClassType ( n.name.toString() );

		ListConverter<VarDecl> util = new ListConverter<VarDecl>();
        List<VarDecl> varList = util.getTList( n.varList );

        List<LlvmType> attr_aux = new LinkedList<LlvmType>();
        List<LlvmValue> var_aux = new LinkedList<LlvmValue>();

        for ( VarDecl var : varList )
        {
            System.out.println ( var.name );
            attr_aux.add( var.accept(this).type );
            var_aux.add( var.accept(this) );
        }
        LlvmStructure struct_attr = new LlvmStructure( attr_aux );

        ListConverter<MethodDecl> converter0 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter0.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            System.out.println ( method.name );
            //attr_aux.add( method.accept(this).type );
            method.accept(this);
        }

        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( classEnv.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

		return null;
	}

	public LlvmValue visit(ClassDeclExtends n) {
		System.out.println("++++++++++AST: ClassDeclExtends");

		ListConverter<VarDecl> util = new ListConverter<VarDecl>();
        List<VarDecl> varList = util.getTList( n.varList );

        List<LlvmType> attr_aux = new LinkedList<LlvmType>();
        List<LlvmValue> var_aux = new LinkedList<LlvmValue>();

        classEnv = new ClassType ( n.superClass.toString() );
        attr_aux.add( classEnv );
        for ( VarDecl var : varList )
        {
            System.out.println ( var.name );
            attr_aux.add( var.accept(this).type );
            var_aux.add( var.accept(this) );
        }
        LlvmStructure struct_attr = new LlvmStructure( attr_aux );

        ListConverter<MethodDecl> converter0 = new ListConverter<MethodDecl>();
        List<MethodDecl> methodList = converter0.getTList( n.methodList );

        for ( MethodDecl method : methodList )
        {
            System.out.println ( method.name );
            //attr_aux.add( method.accept(this).type );
            method.accept(this);
        }

        classEnv = new ClassType ( n.name.toString() );
        LlvmConstantDeclaration const_attr = new LlvmConstantDeclaration( classEnv.toString(), "type " + struct_attr.toString() + "\n" );
        assembler.add( const_attr );

		return null;
	}

	public LlvmValue visit(VarDecl n) {
		System.out.println("++++++++++AST: VarDecl");
        //LlvmValue newReg = new LlvmRegister ( n.name.toString(), n.type.accept(this).type )
        return n.type.accept(this);
        //return newReg;
	}

	public LlvmValue visit(MethodDecl n) {
		System.out.println("++++++++++AST: MethodDecl");
        ListConverter < Formal > luFormal = new ListConverter < Formal > ();

        LlvmValue retType = n.returnType.accept ( this );

        LlvmValue name = n.name.accept(this);

        List < Formal > FormalList = luFormal.getTList ( n.formals );

        List < LlvmValue > argsList = new LinkedList < LlvmValue > ();

        // Adicionando o ponteiro para classe
        argsList.add( new LlvmNamedValue (  "* %this" , classEnv) );

        for ( Formal formal : FormalList )
        {
            argsList.add( formal.accept(this) );
            System.out.println ( formal.toString() );
        }
		assembler.add(new LlvmDefine( "@"+name.toString(), retType.type,
			argsList	));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));

        ListConverter<Statement> converter0 = new ListConverter<Statement>();
        List<Statement> bodyList = converter0.getTList( n.body );
        
        for ( Statement var : bodyList )
        {
            var.accept(this);
            System.out.println ( var.toString() );
        }
        n.returnExp.accept(this);

        //LlvmInstruction const_attr = new LlvmDefine ( name.toString(), retType.type, argsList );
        //assembler.add( const_attr );
		//assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());

		return null;
	}

	public LlvmValue visit(Formal n) {
		System.out.println("++++++++++AST: Formal");
        
        LlvmRegister R1 = new LlvmRegister("%"+n.name.accept(this).toString(), new LlvmPointer(
				LlvmPrimitiveType.I32));
             
		return R1;
	}

	public LlvmValue visit(IntArrayType n) {
		System.out.println("++++++++++AST: IntArrayType");
		return null;
	}

	public LlvmValue visit(BooleanType n) {
		System.out.println("++++++++++AST: BooleanType");
		return null;
	}

	public LlvmValue visit(IntegerType n) {
		System.out.println("++++++++++AST: IntegerType");
        System.out.println ( "toString of IntType: " + n.toString() );

		return new LlvmIntegerLiteral (0);
	}

	public LlvmValue visit(IdentifierType n) {
		System.out.println("++++++++++AST: IdentifierType");
		return null;
	}

	public LlvmValue visit(Block n) {
		System.out.println("++++++++++AST: Block");
		return null;
	}

	public LlvmValue visit(If n) {
		System.out.println("++++++++++AST: If");
		LlvmValue cond = n.condition.accept(this);
		LlvmType type = cond.type;
		LlvmValue Then = n.thenClause.accept(this);
		LlvmValue Else = n.elseClause.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		//assembler.add(new LlvmIcmp(lhs, LlvmPrimitiveType.I32, type, v1, v2));
		return null;
	}

	public LlvmValue visit(While n) {
		System.out.println("++++++++++AST: While");
		return null;
	}

	public LlvmValue visit(Assign n) {
		System.out.println("++++++++++AST: Assign");
        System.out.println ( "var = " + n.var +  " exp = " +  n.exp.toString() );
        LlvmValue lhs = n.var.accept(this);
        n.exp.accept(this);

		return null;
	}

	public LlvmValue visit(ArrayAssign n) {
		System.out.println("++++++++++AST: ArrayAssign");
		return null;
	}

	public LlvmValue visit(And n) {
		System.out.println("++++++++++AST: And");
		return null;
	}

	public LlvmValue visit(LessThan n) {
		System.out.println("++++++++++AST: LessThan");
		LlvmValue l1 = n.lhs.accept(this);
		LlvmValue l2 = n.rhs.accept(this);
		LlvmValue type = n.type.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		//assembler.add(new LlvmIcmp(lhs, LlvmPrimitiveType.I32, type, l1, l2));
		return lhs;
	}

	public LlvmValue visit(Minus n) {
		System.out.println("++++++++++AST: Minus");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

	public LlvmValue visit(Times n) {
		System.out.println("++++++++++AST: Times");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(lhs, LlvmPrimitiveType.I32, v1, v2));
		return lhs;
	}

	public LlvmValue visit(ArrayLookup n) {
		System.out.println("++++++++++AST: ArrayLookup");
		return null;
	}

	public LlvmValue visit(ArrayLength n) {
		System.out.println("++++++++++AST: ArrayLength");
		return null;
	}

	public LlvmValue visit(Call n) {
		System.out.println("++++++++++AST: Call");
		return null;
	}

	public LlvmValue visit(True n) {
		System.out.println("++++++++++AST: True");
		return null;
	}

	public LlvmValue visit(False n) {
		System.out.println("++++++++++AST: False");
		return null;
	}

	public LlvmValue visit(IdentifierExp n) {
		System.out.println("++++++++++AST: IdentifierExp");
        // Criando um LlvmNamedValue para criar um identificador
        return new LlvmNamedValue ( n.name.accept(this).toString(),n.type.accept(this).type );
	}

	public LlvmValue visit(This n) {
		System.out.println("++++++++++AST: This");
		return null;
	}

	public LlvmValue visit(NewArray n) {
		System.out.println("++++++++++AST: NewArray");
		return null;
	}

	public LlvmValue visit(NewObject n) {
		System.out.println("++++++++++AST: NewObject");
		return null;
	}

	public LlvmValue visit(Not n) {
		System.out.println("++++++++++AST: Not");
		return null;
	}

	public LlvmValue visit(Identifier n) {
		System.out.println("++++++++++AST: Identifier");
		System.out.println( "s: " + n.s );
		return new LlvmLabelValue ( n.s );
	}
}

/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
    public Map<String, Data> tab;     
    public Map<String, ClassType> classes;     
    private ClassType classEnv;    //aponta para a classe em uso

    public LlvmValue FillTabSymbol(Program n){
        n.accept(this);
        return null;
    }

    public LlvmValue visit(Program n){
        n.mainClass.accept(this);

        for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
            c.head.accept(this);

        return null;
    }

    public LlvmValue visit(MainClass n){
        classes.put( n.className.s, new ClassType( n.className.s ) );
        return null;
    }

    public LlvmValue visit(ClassDeclSimple n){
        System.out.println("++++++++++AST: ClassDeclSimple ");

        List<LlvmType> attr_type = new LinkedList<LlvmType>();
        List<LlvmValue> attr_value = new LinkedList<LlvmValue>();

        ListConverter<VarDecl> converter0 = new ListConverter<VarDecl>();
        List<VarDecl> varList = converter0.getTList( n.varList );
        
        for ( VarDecl var : varList )
        {
            System.out.println ( var.name );
            attr_type.add( var.accept(this).type );
            attr_value.add( var.accept(this) );
        }

        classes.put( n.name.s, new ClassType( n.name.s ) );

        return null;
    }

    public LlvmValue visit(ClassDeclExtends n){
        System.out.println("++++++++++AST: ClassDeclExtends ");

        return null;
    }
    public LlvmValue visit(VarDecl n){
        System.out.println("++++++++++AST: VarDecl ");
        return n.type.accept(this);
    }
    public LlvmValue visit(Formal n){
        System.out.println("++++++++++AST: Formal ");
        return null;
    }
    public LlvmValue visit(MethodDecl n){
        System.out.println("++++++++++AST: MethodDecl ");
        return null;
    }
    public LlvmValue visit(IdentifierType n){
        System.out.println("++++++++++AST: IdentifierType ");
        return null;
    }
    public LlvmValue visit(IntArrayType n){
        System.out.println("++++++++++AST: IntArrayType ");
        return null;
    }
    public LlvmValue visit(BooleanType n){
        System.out.println("++++++++++AST: BooleanType ");
        return null;
    }
    public LlvmValue visit(IntegerType n){
        System.out.println("++++++++++AST: IntegerType ");
        return null;
    }
}
