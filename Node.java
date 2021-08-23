import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;



public class Node 
{
	public int nID;
	public String hName;
	public int lPort;
	public int MsgCount;	
	public ArrayList<Integer> nList;
	
	public int[] Clock;
	public State nState;
	
	public Color nColor;
	public localState lState;
	public TreeMap<Integer,Boolean> neighborMarked; 
	public ArrayList<Message> [] recordedMsgs;
	
	public TreeMap<Integer, ObjectOutputStream> neighborOutput;
	
		
	public Node(int n, String h, int l) 
	{
		nID = n;
		hName = h;
		lPort = l;
		MsgCount = 0;
		//nList defined in FileReader
		
		//Clock defined in FileReader
		nState = State.PASSIVE;
		nColor = Color.BLUE;
		
		//lState defined in reset 	
		//neighborMarked defined in reset
		//recordedMsgs defined in reset
		
		//neighborOutput defined in setStreams()
	}
	
	@SuppressWarnings("unchecked")
	public void reset()
	{
		int totalnodes = InfoMaster.Access().nodeCount;
		nColor = Color.BLUE;
		lState = new localState();	
		
		neighborMarked = new TreeMap<Integer,Boolean>();
		for(int i = 0; i < nList.size(); i++)
		{
			neighborMarked.put(nList.get(i), false);
		}
		
		recordedMsgs = new ArrayList[totalnodes]; 
		for(int i = 0; i < totalnodes; i++)
		{
			recordedMsgs[i] = new ArrayList<Message>();
		}
		
		
	}
	
	public void setStreams()
	{
		for(int count = 0; count < nList.size(); count++)
		{
			int n = nList.get(count);
			String h = InfoMaster.Access().nodeArray[n].hName;
			int p = InfoMaster.Access().nodeArray[n].lPort;
			
			Socket socket;
			OutputStream ostream;
			ObjectOutputStream msgstream;
			try 
			{
				socket = new Socket(h, p);
				ostream = socket.getOutputStream();
				msgstream = new ObjectOutputStream(ostream);
				neighborOutput.put(count, msgstream);				
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	public int getParent()
	{
		return InfoMaster.Access().Tree.get(this.nID).parent.nodeid;
	}
	
	public void addState(int n, StateMessage s)
	{
		InfoMaster.Access().stateRecord.put(n, s);
	}
	
	public void updateState(int n)
	{
		InfoMaster.Access().receivedState.put(n, true);
	}
	
	public void updateState(int lnID, int[] lClock, Node.State lnState)
	{
		lState.lnID = lnID;
		lState.lClock = lClock;
		lState.lnState = lnState;
	}
	
	public enum State 
	{
		ACTIVE, PASSIVE
	};
	
	public enum Color 
	{
		RED, BLUE
	};
	
	public void setNeighbors(ArrayList<Integer> nl)
	{
		nList = nl;
	}
	
	public void setActive()
	{
		this.nState = State.ACTIVE;
	}
	
	public void setPassive()
	{
		this.nState = State.PASSIVE;
	}
	
	public boolean isActive()
	{
		if(nState == State.ACTIVE)
		{
			return true;
		}
		return false;
	}
	
	public boolean isBlue()
	{
		if(nColor == Color.BLUE)
		{
			return true;
		}
		return false;
	}
	
	public void setBlue()
	{
		this.nColor = Color.BLUE;
	}
	
	public void setRed()
	{
		this.nColor = Color.RED;
	}
	
	public synchronized void addMsg()
	{
		this.MsgCount++;
	}
	
	public synchronized void updateClock()
	{
		this.Clock[this.nID]++;
	}
	
	public void compareClock(int[] recieved)
	{
		for(int i = 0; i < InfoMaster.Access().nodeCount; i++)
		{
			this.Clock[i] = Math.max(this.Clock[i], recieved[i]);
		}
	}
	/*
	public PostOffice initmapPostOffice(mapThread m)
	{
		PostOffice p = new PostOffice(m);
		return p;
	}
	
	public PostOffice initclpPostOffice(clpThread c)
	{
		PostOffice p = new PostOffice(c);
		return p;
	}
	*/
	
	public String toString()
	{
		String str = "";
		str += "NodeID: " + this.nID + "\n";
		str += "HostName: " + this.hName + "\n";
		str += "ListenerPort: " + this.lPort + "\n";
		str += "NeighborList: " + this.nList.toString() + "\n";
		str += "NodeState: " + this.nState + "\n";
		str += "Clock: " + Arrays.toString(this.Clock) + "\n\n";

		return str;
	}
}

class localState
{
	public int lnID;
	public int[] lClock;
	public Node.State lnState;
	
	public localState()
	{
		this.lnID = 0;
		this.lClock = null;
		this.lnState = null;
	}
}