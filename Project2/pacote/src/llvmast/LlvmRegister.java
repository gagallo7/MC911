package llvmast;


public class LlvmRegister extends LlvmValue{
	public String name;
	static int numberReg = 0;
	
	public LlvmRegister(LlvmType type){
		this.type = type;
		this.name = "%tmp"+numberReg++;

	}
	
	public String toString(){
		return name; 
	}
}