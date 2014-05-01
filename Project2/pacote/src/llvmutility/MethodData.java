package llvmutility;
import llvmast.*;
import syntaxtree.*;
import java.util.*;

public class MethodData extends Data
{
    public LlvmType returnType;

    // Mapeamento de variáveis locais do método
    //private Map < String, Data > attr;
    // Mapeamento de argumentos do método
    public Map < String, LlvmType > args;

    public List < Statement > statements;

    public MethodData ( )
    {
    //    this.attr = new HashMap < String, Data > ();
        this.args = new HashMap < String, LlvmType > ();
        this.statements = new LinkedList < Statement > ();
    }

    public void addArg ( String name, LlvmType whichData )
    {
        this.args.put ( name, whichData );
    }

    public void addStmt ( Statement s )
    {
        this.statements.add ( s );
    }

    public LlvmType getArg ( String whichName )
    {
        return this.args.get ( whichName );
    }

    public void print ( )
    {
        System.out.println ( "=========" );
        System.out.println ( "Args:\n" );

        for ( String key : this.args.keySet() )
        {
            if ( key.isEmpty() ) break;
            System.out.println ( "\t" + key + " " + this.args.get (key).toString() );
        }

        System.out.println ( "=========\n" );
    }

}
