import java.util.ArrayList;

public class Message
{
	public int Receiver;
	public int Sender;
}

class AppMessage extends Message
{
	public int[] Clock;
	public AppMessage(int[] c, int r, int s)
	{
		Clock = c;
		Receiver = r;
		Sender = s;
	}
	
}

class MarkerMessage extends Message
{
	public MarkerMessage(int r, int s)
	{
		Receiver = r;
		Sender = s;
	}
}

class StateMessage extends Message
{
	public Node.State State;
	public ArrayList<Message> [] recordedMsgs;
	public StateMessage(int r, int s, Node.State st, ArrayList<Message> [] m)
	{
		Receiver = r;
		Sender = s;
		State = st;
		recordedMsgs = m;
	}

	public void setReceiver(int r) 
	{
		this.Receiver = r;
	}
}

class TerminateMessage extends Message
{
	public TerminateMessage(int r, int s)
	{
		Receiver = r;
		Sender = s;
	}
}
