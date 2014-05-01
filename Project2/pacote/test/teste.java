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

class b 
{
    int b_int;

    public int get( int value ) 
    {
        int aux;
        aux = value;
        
        b_int = 7;

        return aux + b_int;
    }
}

