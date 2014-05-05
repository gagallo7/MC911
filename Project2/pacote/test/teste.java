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
    boolean tst;

    public int set( int value ) 
    {
        return value;
    }
}

class b extends a
{
    public boolean troll ( int amun )
    {
        return false;
    }

    public int get( int value ) 
    {
        tst = this.troll ( 24 );

        return 0;
    }
}
