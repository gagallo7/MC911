package llvmast;
public class LlvmLoad extends LlvmInstruction{
    public LlvmValue lhs;
    public LlvmRegister address; // includes its type

    public LlvmLoad(LlvmValue lhs, LlvmRegister address){
	this.lhs=lhs;
	this.address=address;
    }
    
    public String toString(){
	return "  " + lhs + " = load " + address.type + " " + address;
    }
}