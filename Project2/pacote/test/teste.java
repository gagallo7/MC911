class teste
{
    public static void main( String[] a )
    {
	    System.out.println( new b().get( 9 ) + 5 * 2 );
    }
}

class a 
{
    int a_int;
}

class b extends a
{
    public int get( int value ) 
    {
        int aux;
        int aux2;
        aux = value;
        aux2 = 2 * value;
        
        a_int = 7;

        return aux + a_int + aux2;
    }
}

