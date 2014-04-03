package llvmast;
import java.util.*;
import syntaxtree.VarDecl;

public class LlvmStructure extends LlvmType{
    public List<LlvmType> typeList;
    
    public LlvmStructure(List<LlvmType> typeList){
	this.typeList = typeList;
    }

     public String toString() {
     	if (typeList.isEmpty())
     		return "{ }";
     	
     	String S = "{ " + typeList.get(0);
 		for (int i = 1; i < typeList.size(); i++){
 			S += ", "+typeList.get(i).toString();
 		}
     	S += " }";
     	return S;
     }



}
