package llvmast;
public  class LlvmIcmp extends LlvmInstruction
{
    public LlvmRegister lhs;
    public int conditionCode;
    public LlvmType type;
    public LlvmValue op1, op2;
    
//    public LlvmIcmp(LlvmRegister lhs,  int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
    public LlvmIcmp(LlvmRegister lhs,  int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
        this.lhs = lhs;
        this.conditionCode = conditionCode;
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString(){
        return " " + lhs + " = icmp " + conditionCode + " " + type + " " + op1 + ", " + op2;
    }
}
