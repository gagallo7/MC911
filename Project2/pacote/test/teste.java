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
        aux = value;
        a_int = aux * 2;

        return a_int + 1 - 1;
    }
}

