package llvmast;

public class LlvmStore extends LlvmInstruction
{
    public LlvmValue content; 
    public LlvmValue address; 

    public LlvmStore(LlvmValue content, LlvmValue address)
    {
	    this.content=content;
	    this.address=address;
    }

    public String toString()
    {
	    return "\t" + "store " + content.type + " " + content + ", " + address.type + " " + address;
    }
}
