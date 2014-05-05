package llvmutility;
import llvmast.*;

public class ClassType extends LlvmType
{
    public String name;

    public ClassType( String name )
    {
	    this.name = name;
    }

    public String toString()
    {
	    return "%class." + name;
    }

    public String getName() 
    {
        return name;
    }
}
