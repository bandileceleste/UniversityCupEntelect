import entelect.university.cup.models

public ZooFileHandler

{
	public static Zoo readZooContents(File handle)
	{
		Zoo megaZoo;
		try(Scanner txtin = new Scanner(handle);			
		{
			while (txtin.hasNe
		}
		catch(FileNotFoundException fnfex)
		{
			fnfex.printStackTrace();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		return megaZoo;
	}
	
	public static void writeZootoTextFile(
}