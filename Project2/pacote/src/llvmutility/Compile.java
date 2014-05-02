package llvmutility;
import llvmast.*;

public class Compile extends LlvmInstruction
{
    public String cmd;

    public Compile(String cmd)
    {
	    this.cmd = cmd;
    }

    public String toString()
    {
	    return cmd;
    }
}
