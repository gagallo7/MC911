package llvmutility;
import java.util.*;
import llvmast.*;
import llvmutility.*;

public class ClassData extends Data
{
    private String parent;
    private Map<String, Data> info;

    // Use this constructor for classes declaration with no parent
    public ClassData() 
    {
        this.parent = "";
        this.info = new HashMap<String, Data>();
        this.offset = -1;
    }

    // Use this constructor for classes declaration with parent
    public ClassData( String parent ) 
    {
        this.parent = parent;
        this.info = new HashMap<String, Data>();
        this.offset = -1;
    }

    // Use these constructors for classes inside classes (attribute classes)
    public ClassData ( String parent, int offset ) 
    {
        this.parent = parent;
        this.info = new HashMap<String, Data>();
        this.offset = offset;
    }

    public ClassData ( int offset ) 
    {
        this.parent = "";
        this.info = new HashMap<String, Data>();
        this.offset = offset;
    }

    public String getParent() 
    {
        return this.parent;
    }

    public void add( String dataName, Data whichData ) 
    {
        this.info.put( dataName, whichData );
    }

    public Data get( String dataName ) 
    {
        return this.info.get( dataName );
    }

    // Returns "" if key was not founded
    public String getOffset( String whichClassInfo ) 
    {
       Data aux = get( whichClassInfo );

       if ( aux == null )
           return "";

       return Integer.toString( aux.offset );
    }

    public LlvmType getAttributeType( String whichAttr ) 
    {
        AttributeData attr = (AttributeData) get( whichAttr );

        if ( attr != null ) 
        {
            System.out.println( "getAttributeType( " + whichAttr + " ) forneceu: " + attr.type.toString() );
            return attr.type;
        }

        System.out.println( "getAttributeType( " + whichAttr + " ) forneceu: NULL" );
        return null; 
    }

    public void print() 
    {
        System.out.println ( "Parent: " + this.parent + "\n" ) ;

        for ( String key : this.info.keySet() ) 
        {
            if ( key.isEmpty() ) break;
            System.out.println( "\n-> " + key );
            this.info.get (key).print();
        }
    }

    public int getSize() 
    {
        int size = 0;
        
        for ( String key : this.info.keySet() ) 
        {
            if ( key.isEmpty() ) break;

            size += this.info.get( key ).getSize();
        }

        return size;
    }
}
