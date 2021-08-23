import java.io.*;
import java.util.*;

public class FileReader 
{
	public static InfoMaster readFile(String file) 
	{
		InfoMaster store = new InfoMaster();
		int section = 1;
		Scanner input = null;
		String l = null;
		int i = 0, j = 0;
		
		try 
		{
			File f = new File(file);
			input = new Scanner(f);			
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("Cannot open" + file);
		}
		
		
		while(input.hasNextLine()) 
		{
			l = input.nextLine();
			l = cleanLine(l);
			if(l.length() == 0) {
				continue;
			}
			
			String[] info = l.split(" ");
			if(section == 1) 
			{
				store.nodeCount = Integer.parseInt(info[0]);
				store.minPerActive = Integer.parseInt(info[1]);
				store.maxPerActive = Integer.parseInt(info[2]);
				store.minSendDelay = Integer.parseInt(info[3]);
				store.snapshotDelay = Integer.parseInt(info[4]);
				store.maxNumber = Integer.parseInt(info[5]);
				
				
				Node[] temp = new Node[store.nodeCount];
				store.nodeArray = temp;

				section = 2;
			} 
			else if (section == 2) 
			{
				store.nodeArray[i] = new Node(Integer.parseInt(info[0]), info[1], Integer.parseInt(info[2]));
				store.nodeArray[i].Clock = new int[store.nodeCount];
				
				if(i == 0)
				{
					store.nodeArray[i].setActive();
				}
				
				if(i == (store.nodeCount-1))	
				{
					section = 3;
				}
				i++;
			} 
			else if (section == 3) 
			{
				ArrayList<Integer> temp1 = new ArrayList<Integer>();
				for(int count = 0; count < info.length; count++)	
				{
					int n = Integer.parseInt(info[count]);
					temp1.add(n);
				}
				store.nodeArray[j].setNeighbors(temp1);
				j++;
			}
		}
		
		TreeMaster.CreateTree(store);
		input.close();
		return store;			
	}
	
	public static String cleanLine(String s) 
	{
		if(s.contains("#")) 
		{
			s = s.substring(0, s.indexOf("#"));
		}
		return s.trim();
	}
}
