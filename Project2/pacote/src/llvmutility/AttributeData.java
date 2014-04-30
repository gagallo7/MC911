package llvmutility;
import llvmast.*;

public class AttributeData extends Data
{
    LlvmType type;
    LlvmValue value;

    public AttributeData( LlvmType type, LlvmValue value, int offset ) 
    {
        this.type = type;
        this.value = value;
        this.offset = offset;
    }
}
