import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PostOffice
{
	Thread t;
	Node n;
	
	public PostOffice(Thread t)
	{
		this.t = t;
		if(t instanceof mapThread)
		{
			n = ((mapThread) t).n;
		}
		else
		{
			n = ((clpThread)t).n;
		}
	}
	
	public void mapSend()
	{
		synchronized(n)
		{
			mapThread m = (mapThread) t;
			int msgCount = getRandomMsg(InfoMaster.Access().minPerActive, InfoMaster.Access().maxPerActive);
			int sleep = InfoMaster.Access().minSendDelay;
			Node sender = m.n;
			for(int counter = 0; counter < msgCount; counter++)
			{
				if(sender.isActive() && sender.MsgCount < InfoMaster.Access().maxNumber)
				{
					int recieverID = sender.nList.get(getRandomNeighbor(0, sender.nList.size()));
					//Node reciever = InfoMaster.Access().nodeArray[recieverID];
					System.out.println("Node: " + sender.nID + " Sending to " + recieverID);
					sender.addMsg();
					sender.updateClock();
					//System.out.print("\nNode: " + sender.nID + "\tClock: " + Arrays.toString(sender.Clock) + "\n\n");
					int[] updatedclock = sender.Clock.clone();
					AppMessage mail = new AppMessage(updatedclock, recieverID, sender.nID); 
					Recieve(mail);
					Sleep(sleep);			
				}
			}
			sender.setPassive();
			
			/*for(int i = 0; i < InfoMaster.Access().nodeArray.length; i++)
			{
				int id = InfoMaster.Access().nodeArray[i].nID;
				int count = InfoMaster.Access().nodeArray[i].MsgCount;
				boolean state = InfoMaster.Access().nodeArray[i].isActive();
				String clk = Arrays.toString(sender.Clock);
				
				System.out.println("Node: " + id + "\nTotal Msgs: " + count + "\nState: " + state + "\n" + "Clock: " + clk + "\n");
			}*/
			
		}
	}
	
	public void Recieve(Message msg)
	{
		synchronized(n)
		{
			Node receiver = InfoMaster.Access().nodeArray[msg.Receiver];	
			if(msg instanceof AppMessage)
			{
				AppMessage mail = (AppMessage)msg;
				//System.out.println("Node: " + receiver.nID + " receiving \n");
				
				System.out.print("\nNode: " + receiver.nID + "\n\tOldClock: " + Arrays.toString(receiver.Clock) + "\n");
				System.out.print("\tCompareClock: " + Arrays.toString(mail.Clock) + "\n");
				receiver.compareClock(mail.Clock);
				receiver.updateClock();
				System.out.print("\tNewClock: " + Arrays.toString(receiver.Clock) + "\n");
				
				if(!receiver.isBlue())
				{
					receiver.recordedMsgs[mail.Sender].add(mail);
				}

				if(!receiver.isActive() && receiver.MsgCount < InfoMaster.Access().maxNumber)
				{
					receiver.setActive();
					new mapThread(receiver).run();
				}
			}
			else if(msg instanceof MarkerMessage)
			{
				MarkerMessage mail = (MarkerMessage) msg;
				clpSend(receiver, mail.Sender);
			}
			else if(msg instanceof StateMessage)
			{
				StateMessage mail = (StateMessage) msg;
				if(receiver.nID == 0)
				{
					receiver.updateState(mail.Sender);
					receiver.addState(mail.Sender, mail);
					boolean restart = checkRestart();
					if(restart)
					{
						new clpThread(receiver).run();
					}
					
					if(InfoMaster.Access().endFlag)
					{
						sendTerminate(receiver);
					}
				}
				else
				{
					mail.setReceiver(receiver.getParent());
					sendParent(mail);
				}
				
			}
		}
		
	}
	
	public void sendTerminate(Node n)
	{
		synchronized(n)
		{
			for(int reciever : n.nList)
			{
				TerminateMessage email = new TerminateMessage(reciever, n.nID);
				Recieve(email);
			}
			System.exit(0);
		}
	}
	
	public void clpStart()
	{
		
		synchronized(n)
		{
			clpThread c = (clpThread) t;
			Node n = c.n;
			clpReset();
			InfoMaster.Access().Reset();
			n.updateState(0);
			clpSend(n, 0);
		}
	}
	
	public void clpSend(Node n, int Sender)
	{
		synchronized(n)
		{
			n.neighborMarked.put(Sender, true);
			
			if(n.isBlue())
			{
				n.setRed();
				recordSnapshot(n);
				sendMarkerMsgs(n);
			}
			else
			{
				if(allNeighborsMarked(n))
				{
					n.setBlue();
					
					if(n.nID == 0)
					{
						StateMessage record = new StateMessage(-1, 0, n.nState, n.recordedMsgs);
						n.addState(n.nID, record);
					}
					else
					{
						StateMessage email = new StateMessage(n.getParent(), n.nID, n.nState, n.recordedMsgs);
						sendParent(email);
						clpReset();
					}
				}
			}
		}
	}
	
	public boolean checkRestart()
	{
		synchronized(n)
		{
			boolean allRecieved = true, channelsPopulated = false, activeProcess = false;
			
			for(boolean i : InfoMaster.Access().receivedState.values())
			{
				if(!i)
				{
					allRecieved = false;
					break;
				}
			}
			
			if(allRecieved)
			{
				for(StateMessage i : InfoMaster.Access().stateRecord.values())
				{
					if(i.State == Node.State.ACTIVE)
					{
						activeProcess = true;
						break;
					}
				}
				
				if(!activeProcess)
				{
					for(StateMessage i : InfoMaster.Access().stateRecord.values())
					{
						for(ArrayList<Message> j : i.recordedMsgs)
						{
							if(!j.isEmpty())
							{
								channelsPopulated = true;
								break;
							}
							
						}
						if(channelsPopulated)
						{
							break;
						}
					}
				}
					
				InfoMaster.Access().endFlag = (!channelsPopulated && !activeProcess);
			}
			return (channelsPopulated || activeProcess);
		}	
	}
	
	public void recordSnapshot(Node n)
	{
		synchronized(n)
		{
			int id = n.nID;
			int[] c = n.Clock;
			Node.State s = n.nState;
			
			n.updateState(id, c, s);
			System.out.println("Node: " + n.nID + " Snapshot: " + Arrays.toString(c) + "\n");
		}
	}
	
	public void sendMarkerMsgs(Node n)
	{
		synchronized(n)
		{
			for(int reciever : n.nList)
			{
				MarkerMessage email = new MarkerMessage(reciever, n.nID);
				Recieve(email);
			}
		}
	}
	
	public void clpReset()
	{
		synchronized(n)
		{
			clpThread c = (clpThread) t;
			c.n.reset();
		}
	}
	
	public boolean allNeighborsMarked(Node n)
	{
		synchronized(n)
		{
			for(boolean i : n.neighborMarked.values())
			{
				if(!i)
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	public void sendParent(Message email)
	{
		synchronized(n)
		{
			Recieve(email);
		}
	}
	
	int getRandomMsg(int min, int max)
	{
		synchronized(n)
		{
			int r = ThreadLocalRandom.current().nextInt(min, max + 1);
			if(r == 0)
			{
				r++;
			}
			return r;
		}
	}
	
	int getRandomNeighbor(int min, int max)
	{
		synchronized(n)
		{
			int r = ThreadLocalRandom.current().nextInt(min, max);
			return r;
		}
	}
	
	public void Sleep(int s)
	{
		synchronized(n)
		{
			try
			{
				Thread.sleep(s);
			}
			catch(InterruptedException e)
			{
				System.out.println("Idk why this didn't work...");
			}
		}
	}
}

class mapOffice
{
	public mapThread m;
	
	public mapOffice(mapThread m)
	{
		this.m = m;
	}
	
	public void mapSend()
	{
		synchronized(m)
		{
			int msgCount = getRandomMsg(InfoMaster.Access().minPerActive, InfoMaster.Access().maxPerActive);
			int sleep = InfoMaster.Access().minSendDelay;
			Node sender = m.n;
			for(int counter = 0; counter < msgCount; counter++)
			{
				if(sender.isActive() && sender.MsgCount < InfoMaster.Access().maxNumber)
				{
					int recieverID = sender.nList.get(getRandomNeighbor(0, sender.nList.size()));
					//Node reciever = InfoMaster.Access().nodeArray[recieverID];
					System.out.println("Node: " + sender.nID + " Sending to " + recieverID);
					sender.addMsg();
					sender.updateClock();
					//System.out.print("\nNode: " + sender.nID + "\tClock: " + Arrays.toString(sender.Clock) + "\n\n");
					int[] updatedclock = sender.Clock.clone();
					AppMessage mail = new AppMessage(updatedclock, recieverID, sender.nID); 
					Recieve(mail);
					Sleep(sleep);			
				}
			}
			sender.setPassive();
			
			/*for(int i = 0; i < InfoMaster.Access().nodeArray.length; i++)
			{
				int id = InfoMaster.Access().nodeArray[i].nID;
				int count = InfoMaster.Access().nodeArray[i].MsgCount;
				boolean state = InfoMaster.Access().nodeArray[i].isActive();
				String clk = Arrays.toString(sender.Clock);
				
				System.out.println("Node: " + id + "\nTotal Msgs: " + count + "\nState: " + state + "\n" + "Clock: " + clk + "\n");
			}*/
			
		}
	}
	
	int getRandomMsg(int min, int max)
	{
		synchronized(m)
		{
			int r = ThreadLocalRandom.current().nextInt(min, max + 1);
			if(r == 0)
			{
				r++;
			}
			return r;
		}
	}
	
	int getRandomNeighbor(int min, int max)
	{
		synchronized(m)
		{
			int r = ThreadLocalRandom.current().nextInt(min, max);
			return r;
		}
	}
	
	public void Sleep(int s)
	{
		synchronized(m)
		{
			try
			{
				Thread.sleep(s);
			}
			catch(InterruptedException e)
			{
				System.out.println("Idk why this didn't work...");
			}
		}
	}
	
}

class clpOffice
{
	public clpThread c;
	
	public clpOffice(clpThread c)
	{
		this.c = c;
	}
	
	public void clpStart()
	{
		synchronized(c)
		{
			Node n = c.n;
			clpReset();
			InfoMaster.Access().Reset();
			n.updateState(0);
			clpSend(n, 0);
		}
	}
	
	public void clpSend(Node n, int Sender)
	{
		synchronized(n)
		{
			n.neighborMarked.put(Sender, true);
			
			if(n.isBlue())
			{
				n.setRed();
				recordSnapshot(n);
				sendMarkerMsgs(n);
			}
			else
			{
				if(allNeighborsMarked(n))
				{
					n.setBlue();
					
					if(n.nID == 0)
					{
						StateMessage record = new StateMessage(-1, 0, n.nState, n.recordedMsgs);
						n.addState(n.nID, record);
					}
					else
					{
						StateMessage email = new StateMessage(n.getParent(), n.nID, n.nState, n.recordedMsgs);
						sendParent(email);
						clpReset();
					}
				}
			}
		}
	}
	
	public void sendParent(Message email)
	{
		synchronized(c)
		{
			Recieve(email);
		}
	}

	public void sendTerminate(Node n)
	{
		synchronized(n)
		{
			for(int reciever : n.nList)
			{
				TerminateMessage email = new TerminateMessage(reciever, n.nID);
				Recieve(email);
			}
			System.exit(0);
		}
	}
	
	public void sendMarkerMsgs(Node n)
	{
		synchronized(n)
		{
			for(int reciever : n.nList)
			{
				MarkerMessage email = new MarkerMessage(reciever, n.nID);
				Recieve(email);
			}
		}
	}

	public void clpReset()
	{
		synchronized(c)
		{
			c.n.reset();
		}
	}
	
	public boolean allNeighborsMarked(Node n)
	{
		synchronized(n)
		{
			for(boolean i : n.neighborMarked.values())
			{
				if(!i)
				{
					return false;
				}
			}
			
			return true;
		}
	}

	public boolean checkRestart()
	{
		synchronized(c)
		{
			boolean allRecieved = true, channelsPopulated = false, activeProcess = false;
			
			for(boolean i : InfoMaster.Access().receivedState.values())
			{
				if(!i)
				{
					allRecieved = false;
					break;
				}
			}
			
			if(allRecieved)
			{
				for(StateMessage i : InfoMaster.Access().stateRecord.values())
				{
					if(i.State == Node.State.ACTIVE)
					{
						activeProcess = true;
						break;
					}
				}
				
				if(!activeProcess)
				{
					for(StateMessage i : InfoMaster.Access().stateRecord.values())
					{
						for(ArrayList<Message> j : i.recordedMsgs)
						{
							if(!j.isEmpty())
							{
								channelsPopulated = true;
								break;
							}
							
						}
						if(channelsPopulated)
						{
							break;
						}
					}
				}
					
				InfoMaster.Access().endFlag = (!channelsPopulated && !activeProcess);
			}
			return (channelsPopulated || activeProcess);
		}	
	}
	
	public void recordSnapshot(Node n)
	{
		synchronized(n)
		{
			int id = n.nID;
			int[] c = n.Clock;
			Node.State s = n.nState;
			
			n.updateState(id, c, s);
			System.out.println("Node: " + n.nID + " Snapshot: " + Arrays.toString(c) + "\n");
		}
	}
}

class recieveOffice extends Thread
{
	Node receiver
	
	public void Recieve(Message msg)
	{
		synchronized()
		{
			Node receiver = InfoMaster.Access().nodeArray[msg.Receiver];	
			if(msg instanceof AppMessage)
			{
				AppMessage mail = (AppMessage)msg;
				//System.out.println("Node: " + receiver.nID + " receiving \n");
				
				System.out.print("\nNode: " + receiver.nID + "\n\tOldClock: " + Arrays.toString(receiver.Clock) + "\n");
				System.out.print("\tCompareClock: " + Arrays.toString(mail.Clock) + "\n");
				receiver.compareClock(mail.Clock);
				receiver.updateClock();
				System.out.print("\tNewClock: " + Arrays.toString(receiver.Clock) + "\n");
				
				if(!receiver.isBlue())
				{
					receiver.recordedMsgs[mail.Sender].add(mail);
				}

				if(!receiver.isActive() && receiver.MsgCount < InfoMaster.Access().maxNumber)
				{
					receiver.setActive();
					new mapThread(receiver).run();
				}
			}
			else if(msg instanceof MarkerMessage)
			{
				MarkerMessage mail = (MarkerMessage) msg;
				clpSend(receiver, mail.Sender);
			}
			else if(msg instanceof StateMessage)
			{
				StateMessage mail = (StateMessage) msg;
				if(receiver.nID == 0)
				{
					receiver.updateState(mail.Sender);
					receiver.addState(mail.Sender, mail);
					boolean restart = checkRestart();
					if(restart)
					{
						new clpThread(receiver).run();
					}
					
					if(InfoMaster.Access().endFlag)
					{
						sendTerminate(receiver);
					}
				}
				else
				{
					mail.setReceiver(receiver.getParent());
					sendParent(mail);
				}
				
			}
		}
		
	}
}

class mapThread extends Thread
{
	public Node n;
	public PostOffice p;
	
	public mapThread(Node n)
	{
		this.n = n;
		p = n.initmapPostOffice(this);
	}
	
	public void run()
	{
		p.mapSend();
	}
}

class clpThread extends Thread
{
	public Node n;
	public PostOffice p;
	
	public clpThread(Node n)
	{
		this.n = n;
		p = n.initclpPostOffice(this);
	}
	
	
	public void run()
	{
 		int sleep = InfoMaster.Access().snapshotDelay;
		p.Sleep(sleep);
		p.clpStart();
		
	}
}