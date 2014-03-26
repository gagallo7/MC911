package llvmast;
public class LlvmBool extends LlvmValue{
	int B;
    public LlvmBool(int B){
    	type = LlvmPrimitiveType.I1;
    	this.B = B;
    }
    
    public String toString(){
    	switch (B){
    	case FALSE : {return "0";}
    	case TRUE  : {return "1";}
    		
    	}
		return null;
    }
    
    public static final int FALSE  = 0;
    public static final int TRUE  = 1;
    
}