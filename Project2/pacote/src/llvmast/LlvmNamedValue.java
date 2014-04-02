package llvmast;
public class LlvmNamedValue extends LlvmRegister{
	public String name;

	public LlvmNamedValue(String name, LlvmType type){
		super(type);
		this.name = name;
	}
	
	public String toString(){
		return name; 
	}
}