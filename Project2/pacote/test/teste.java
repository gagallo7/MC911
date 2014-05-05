class teste
{
    public static void main( String[] a )
    {
	    System.out.println( new b().get( 10 ) + 5 * 2 );
    }
}

class a 
{
    int a_int;

    public int set( int value ) 
    {
        return value;
    }
}

class b extends a
{
    public int get( int value ) 
    {
        a_int = value;

        return a_int;
    }
}

