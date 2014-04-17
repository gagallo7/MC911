package llvmast;

public  class LlvmBranch extends LlvmInstruction
{
    public LlvmRegister lhs;
    public LlvmLabelValue l1, l2;
    public LlvmValue cond;

    public LlvmBranch(LlvmLabelValue label)
    {
        this.l1 = label;
        this.l2 = null;
    }

    public LlvmBranch(LlvmValue cond,  LlvmLabelValue brTrue, LlvmLabelValue brFalse)
    {
        this.l1 = brTrue;
        this.l2 = brFalse;
    }

    public String toString()
    {
        if ( this.l2 == null )
        {
            return "br label " + this.l1;
        }

        return "br i1 " + cond + ", label " + l1 + ", label " + l2;
    }
}
