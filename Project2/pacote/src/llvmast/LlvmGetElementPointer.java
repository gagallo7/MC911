package llvmast;
import  java.util.*;
public class LlvmGetElementPointer extends LlvmInstruction{
    public LlvmRegister lhs;
    public LlvmRegister source;
    public List<LlvmValue> offsets;

    public LlvmGetElementPointer(LlvmRegister lhs, LlvmRegister source, List<LlvmValue> offsets){
	this.lhs = lhs;
	this.source = source;
	this.offsets = offsets;
    }
    
    public String toString(){
	String ps = "";
	for(int i = 0; i<offsets.size(); i++){
	    ps = ps + offsets.get(i).type + " " + offsets.get(i);
	    if(i+1<offsets.size()) 
		ps = ps + ", ";

	}
	return "  " + lhs + " = getelementptr " + source.type + " " + source +", " + ps;
    }

}
