package llvmast;

public class LlvmNamedValue extends LlvmRegister
{
	public LlvmNamedValue(String name, LlvmType type)
	{
		super(name, type);
		this.name = name;       // TODO: Why?
	}

	public String toString()
	{
		return name; 
	}
}
