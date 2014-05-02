package llvmutility;
import llvmast.*;

public class AttributeData extends Data
{
    public LlvmType type;
    public LlvmValue value;
    private int size;

    // Use this constructor to local attributes
    public AttributeData( LlvmType type, LlvmValue value ) 
    {
        this.type = type;
        this.value = value;
        this.offset = -1;

        if ( type instanceof LlvmPointer )    
        { 
            this.size = 8;

        } else 
        {
            if ( type instanceof LlvmPrimitiveType )
            {
                if ( type.toString().equals( "i32" ) ) 
                {
                    this.size = 4;

                } else 
                {
                    this.size = 1;
                }   
            }
        }
    }

    // Use this constructor to class attributes
    public AttributeData( LlvmType type, LlvmValue value, int offset ) 
    {
        this.type = type;
        this.value = value;
        this.offset = offset;

        if ( type instanceof LlvmPointer )    
        { 
            this.size = 8;

        } else 
        {
            if ( type instanceof LlvmPrimitiveType )
            {
                if ( type.toString().equals( "i32" ) ) 
                {
                    this.size = 4;

                } else 
                {
                    this.size = 1;
                }   
            }
        }
    }

    public void print ()
    {
        System.out.println ( "Type: " + type.toString() );
        System.out.println ( "Size: " + Integer.toString( this.getSize() ) );
    }

    public int getSize() 
    {
        return this.size;
    }
}
