class teste
{
    public static void main( String[] a )
    {
        System.out.println( new b().getA().getDoubleInt( new b().getA().getInt() ) );
    }
}

class a 
{
    public int getInt() 
    {
        return 10;
    }

    public int getDoubleInt( int value ) 
    {
        return 2 * value;
    }
}

class b 
{
    public a getA() 
    {
        return new a();
    }
}
