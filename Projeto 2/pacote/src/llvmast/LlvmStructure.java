package llvmast;
import java.util.*;
public class LlvmStructure extends LlvmType{
    public List<LlvmType> typeList;
    
    public LlvmStructure(List<LlvmType> typeList){
	this.typeList = typeList;
    }
}