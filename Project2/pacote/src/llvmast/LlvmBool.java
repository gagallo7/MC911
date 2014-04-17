package llvmast;

public class LlvmBool extends LlvmValue
{
	public int val;

    public LlvmBool(int B)
    {
    	type = LlvmPrimitiveType.I1;
    	this.val = B;
    }

    public String toString()
    {
    	switch (this.val){
    	    case FALSE : {return "false";}
    	    case TRUE  : {return "true";}
    	}

		return "ERROR: Boolean isn't TRUE nor FALSE";   // TODO: It was 'return null' 
    }

    public static final int FALSE  = 0;
    public static final int TRUE  = 1;
}
