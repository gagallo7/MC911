package llvm;
import syntaxtree.*;

public class LlvmUtility
{
    public static final java.util.List < VarDecl > getList ( util.List < VarDecl > arg )
    {
        util.List < VarDecl > aux = arg;
        java.util.List < VarDecl > res = new java.util.LinkedList < VarDecl > ();
        while ( aux != null )
        {
            res.add ( aux.head );
            aux = aux.tail;
        }
        return res;
    }
}
