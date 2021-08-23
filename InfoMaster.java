import java.util.HashMap;
import java.util.TreeMap;

@SuppressWarnings("unused")
public class InfoMaster 
{
	static InfoMaster info = new InfoMaster();
	
	public int nodeCount;
	public int minPerActive;
	public int maxPerActive;
	public int minSendDelay;
	public int snapshotDelay;
	public int maxNumber;
	public Node[] nodeArray;
	public TreeMap<Integer, Leaf> Tree;
	public TreeMap<Integer,StateMessage> stateRecord;
	public TreeMap<Integer, Boolean> receivedState; 
	public boolean endFlag;
	public int curProcess;
	
	public static InfoMaster Access()
	{
		return info;
	}
	
	public static void Save(InfoMaster i)
	{
		info = i;
	}
	
	public void Reset()
	{
		stateRecord = new TreeMap<Integer,StateMessage>();
		receivedState = new TreeMap<Integer, Boolean>();
		
		for(int i = 0; i < nodeCount; i++)
		{
			receivedState.put(i, false);
		}
	}
	
	public String toString()
	{
		String str = "";
		
		str += "Current Process/Node: " + curProcess + "\n";
		
		str += "nodeCount: " + nodeCount + "\n";
		str += "minPerActive: " + minPerActive + "\n";
		str += "maxPerActive: " + maxPerActive + "\n";
		str += "minSendDelay: " + minSendDelay + "\n";
		str += "snapshotDelay: " + snapshotDelay + "\n";
		str += "maxNumber: " + maxNumber + "\n\n";
			
		for(int count = 0; count < nodeArray.length; count++)
		{
			str += nodeArray[count].toString();
		}
		
		return str;
	}

}
