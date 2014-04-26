package llvmutility;

public class ListConverter < T >
{
    public ListConverter() {}

    public final java.util.List < T > getTList ( util.List < T > arg )
    {
        util.List < T > aux = arg;
        java.util.List < T > res = new java.util.LinkedList < T > ();
        while ( aux != null )
        {
            res.add ( aux.head );
            aux = aux.tail;
        }
        return res;
    }
}
