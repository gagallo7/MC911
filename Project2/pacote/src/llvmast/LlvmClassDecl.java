package llvmast;

public  class LlvmClassDecl extends LlvmInstruction
{
    public LlvmClass lhs;
    public LlvmStructure attributes;

    public LlvmClassDecl ( LlvmClass lhs, LlvmStructure attributes )
    {
	    this.lhs = lhs;
        this.attributes = attributes;
    }

    public String toString()
    {
	    return lhs + " = type " + attributes;
    }
}
