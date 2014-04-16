package llvm;
import syntaxtree.*;

public class LlvmUtility
{
    public static final java.util.List < VarDecl > getVarList ( util.List < VarDecl > arg )
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
    public static final java.util.List < MethodDecl > getMethodList ( util.List < MethodDecl > arg )
    {
        util.List < MethodDecl > aux = arg;
        java.util.List < MethodDecl > res = new java.util.LinkedList < MethodDecl > ();
        while ( aux != null )
        {
            res.add ( aux.head );
            aux = aux.tail;
        }
        return res;
    }
}
