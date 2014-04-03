package llvmast;
import java.util.*;
public  class LlvmMalloc extends LlvmInstruction{
    public LlvmValue lhs;
    public LlvmValue size;

    private LlvmRegister resultTimes;
    private LlvmRegister resultCall;
    

	String call, bitcast;

	// Malloc de <size> bytes
    public LlvmMalloc(LlvmValue lhs, LlvmValue size){
	this.lhs = lhs;
	this.size = size;

	call = new String();
	bitcast = new String();

	resultCall = new  LlvmRegister(LlvmPrimitiveType.I8);

	// Malloc de <size> bytes
	call = "  " + resultCall + " = call i8* @malloc ( i32 "+ size + ")\n";  
	bitcast = "  " + lhs + " = bitcast i8* " + resultCall + " to i32*\n";
    }
    

    public String toString(){
	return call  + bitcast;
    }
}
	
