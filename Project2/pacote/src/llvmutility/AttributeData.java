package llvmutility;
import llvmast.*;

public class AttributeData extends Data
{
    LlvmType type;
    LlvmValue value;

    // Use this constructor to local attributes
    public AttributeData( LlvmType type, LlvmValue value ) 
    {
        this.type = type;
        this.value = value;
        this.offset = -1;
    }

    // Use this constructor to class attributes
    public AttributeData( LlvmType type, LlvmValue value, int offset ) 
    {
        this.type = type;
        this.value = value;
        this.offset = offset;
    }
}
