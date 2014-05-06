package llvmutility;
import llvmast.*;
import syntaxtree.*;
import java.util.*;

public class MethodData extends Data
{
    public LlvmType returnType;
    public Exp returnExp;
    public String myClass;

    // Mapeamento de variáveis locais do método
    public Map < String, LlvmType > locals;

    // Mapeamento de argumentos do método
    public Map < String, LlvmType > args;
    public List < String > argsInOrder;

    public List < Statement > statements;

    public MethodData ( )
    {
        this.args = new HashMap < String, LlvmType > ();
        this.locals = new HashMap < String, LlvmType > ();
        this.statements = new LinkedList < Statement > ();
        this.argsInOrder = new LinkedList< String > ();
    }

    public void addLocal( String localName, LlvmType whichLocal ) 
    {
        this.locals.put( localName, whichLocal );
    }

    public LlvmType getLocal( String whichLocal ) 
    {
        return this.locals.get( whichLocal );
    }

    public void addArg ( String name, LlvmType whichData )
    {
        this.args.put ( name, whichData );
        this.argsInOrder.add( name );
    }

    public LlvmType getArg( String whichArg ) 
    {
        return this.args.get( whichArg );
    }

    public void addStmt ( Statement s )
    {
        this.statements.add ( s );
    }

    public String getVarCase( String varName ) 
    {
        LlvmType aux = this.getLocal( varName );

        if ( aux != null )
            return "local";

        aux = this.getArg( "%" + varName );
        
        if ( aux != null )
            return "arg";

        return "attribute";
    }

    public void print ( )
    {
        System.out.println ( "=========" );
        System.out.println( "myClass: " + this.myClass );
        System.out.println( "returnType: " + this.returnType + "\n" );

        System.out.println ( "Args:\n" );
        for ( String key : this.args.keySet() )
        {
            if ( key.isEmpty() ) break;
            System.out.println ( "\t" + key + " " + this.args.get (key).toString() );
        }

        System.out.println ( "\nLocals:\n" );
        for ( String key : this.locals.keySet() )
        {
            if ( key.isEmpty() ) break;
            System.out.println ( "\t" + key + " " + this.locals.get (key).toString() );
        }

        System.out.println ( "=========\n" );
    }
}
