package llvmast;

public  class LlvmMalloc extends LlvmInstruction
{
    public LlvmValue lhs;
    public LlvmType type;
    public LlvmValue nElements;

    // Armazena o tamanho do último objeto. 
    // Útil para pegar o Lenght do vetor 
    public static LlvmValue lastArraySize = null;


    private int size; 
    private LlvmRegister lhsTimes;
    private LlvmRegister lhsCall;

	String times, call, bitcast;

	/**
	 * 
	 * Construtor Malloc: recebe apenas o tamanho em bytes que se deseja alocar
	 * Cabe a você calcular qual será esse tamanho
	 * 
	 * @param lhs
	 * @param size
	 */
	public LlvmMalloc(LlvmValue lhs, LlvmValue size){
		call = new String();
		bitcast = new String();

		LlvmValue lhsCall = new  LlvmRegister(LlvmPrimitiveType.I8);

		// Malloc de <size> bytes
		call = "\t" + lhsCall + " = call i8* @malloc ( i32 "+ size + " )\n";  
		bitcast = "\t" + lhs + " = bitcast i8* " + lhsCall + " to i32*";
	}

	public LlvmMalloc(LlvmValue lhs, LlvmValue size, LlvmType type){
		call = new String();
		bitcast = new String();

		LlvmValue lhsCall = new  LlvmRegister(LlvmPrimitiveType.I8);

        lastArraySize = size;

		// Malloc de <size> bytes
		call = "\t" + lhsCall + " = call i8* @malloc ( i32 "+ size + " )" ;  
		bitcast = "\t" + lhs + " = bitcast i8* " + lhsCall + " to " + type + " *\n";
	}

	/**
	 * Construtor para Alocar objetos de Classe: recebe o tipo (que deve ser LlvmStructure)
	 * e o nome da Classe (objName)
	 *  
	 * @param lhs
	 * @param type
	 * @param className
	 */
	public LlvmMalloc(LlvmValue lhs, LlvmType type, String className)
	{
		MallocImpl(lhs, type, new LlvmIntegerLiteral(1), className);
	}



	/**
	 *  Implementação
	 *  
	 */
	private void MallocImpl(LlvmValue lhs, LlvmType type, LlvmValue nElements, String className)
	{
		this.lhs = lhs;
		this.type = type;
		this.nElements = nElements;
		this.size = 0;
		lastArraySize = null;

		// calculando o tamanho do malloc (em Bytes)
		if ( type instanceof LlvmStructure )
		{
			size = ((LlvmStructure) type).sizeByte;

        } else 
        {
			this.nElements = null;

			if ( type == LlvmPrimitiveType.I32 )
			{
				size = 4;
				lastArraySize = nElements;

			} else 
			{ 
				// Se é um bool
				size = 1;
			}
		}		

		lhsTimes = new LlvmRegister(LlvmPrimitiveType.I32);
		lhsCall = new  LlvmRegister(LlvmPrimitiveType.I8);

		times = new String("\t" + lhsTimes + " = mul i32 " + size + ", " + nElements + "\n");
		call = new String("\t" + lhsCall + " = call i8* @malloc ( i32 "+ lhsTimes + " )");

		if (className == null)
			bitcast = new String("\t" + lhs + " = bitcast i8* " + lhsCall + " to " + type + "iiiiiiiii*");
		else
			bitcast = new String("\t" + lhs + " = bitcast i8* " + lhsCall + " to " + className + "iiiiiiii*");
	}    

    public String toString()
    {
		//return times + call  + bitcast;   // TODO: Check this, why times?
		return call + System.getProperty("line.separator") + bitcast;
    }
}

