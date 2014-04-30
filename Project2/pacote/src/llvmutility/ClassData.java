package llvmutility;
import java.util.*;

public class ClassData extends Data
{
    private String parent;
    private Map<String, Data> info;

    // Use this constructor for classes declaration with no parent
    public ClassData() 
    {
        this.parent = "";
        this.info = new HashMap<String, Data>();
        this.offset = 0;
    }

    // Use this constructor for classes declaration with parent
    public ClassData( String parent ) 
    {
        this.parent = parent;
        this.info = new HashMap<String, Data>();
        this.offset = 0;
    }

    // Use this constructor for classes inside classes (attribute classes)
    public ClassData ( String parent, int offset ) 
    {
        this.parent = parent;
        this.info = new HashMap<String, Data>();
        this.offset = offset;
    }

    public String getParent() 
    {
        return this.parent;
    }

    public void add( String dataName, Data whichData ) 
    {
        info.put( dataName, whichData );
    }

    // Returns -1 if key was not founded
    public int getOffset( String whichClassInfo ) 
    {
        Data aux = info.get( whichClassInfo );

        if ( aux == null )
            return -1;

        return aux.offset;
    }

    public void print() 
    {
        for ( String key : this.info.keySet() ) 
        {
            if ( key.isEmpty() ) break;
            System.out.println( "- " + key + " -" );
        }
    }

}
