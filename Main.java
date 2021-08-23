import java.util.Arrays;

public class Main 
{
	public static void main(String[] args)
	{
		String file = "hi.txt";
		InfoMaster info = FileReader.readFile(file);
		info.curProcess = Integer.parseInt(args[0]);
		InfoMaster.Save(info);
		System.out.println("Process " + info.curProcess + " started.\n");
		
		//System.out.println(InfoMaster.Access().toString());
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		mapThread m = new mapThread(InfoMaster.Access().nodeArray[0]);
		clpThread c = new clpThread(InfoMaster.Access().nodeArray[0]);
		c.start();
		m.start();


		System.out.print("help\n");
		for(int i = 0; i < InfoMaster.Access().nodeArray.length; i++)
		{
			int id = InfoMaster.Access().nodeArray[i].nID;
			int count = InfoMaster.Access().nodeArray[i].MsgCount;
			boolean state = InfoMaster.Access().nodeArray[i].isActive();
			String clk = Arrays.toString(InfoMaster.Access().nodeArray[i].Clock);
			
			System.out.println("Node: " + id + "\nTotal Msgs: " + count + "\nState: " + state + "\nClock: " + clk + "\n\n");
		}
		*/
	}
	
	
}