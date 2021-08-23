import java.util.*;

public class TreeMaster
{
	public static void CreateTree(InfoMaster info)
	{
		int index;
		int numNeighbors;
		int nIndex;
		
		TreeMap<Integer, Leaf> tree = new TreeMap<Integer, Leaf>();
		Queue<Integer> q = new LinkedList<Integer>();
		Leaf p = new Leaf();	
		p.nodeid  = info.nodeArray[0].nID;
				
		tree.put(0, p);
		q.add(0);
		
		while(!q.isEmpty())
		{
			index = q.remove();
			numNeighbors = info.nodeArray[index].nList.size();
			
			p = tree.get(index);

			for(int i = 0; i < numNeighbors; i++)
			{
				nIndex = info.nodeArray[index].nList.get(i);
				if(!tree.containsKey(nIndex))
				{
					addChild(tree, nIndex, p);
					q.add(nIndex);
				}
			}
		}	
		info.Tree = tree;
	}
	
	public static void addChild(TreeMap<Integer, Leaf> t, int n, Leaf parent)
	{
		Leaf kid = new Leaf(n, parent);
		t.put(n, kid);
		parent.kids.add(n);
	}
}

class Leaf
{
	public int nodeid;
	public Leaf parent;
	public ArrayList<Integer> kids = new ArrayList<Integer>();
	
	Leaf()
	{
		nodeid = 0;
		parent = null;
	}
	
	Leaf(int n, Leaf p)
	{
		nodeid = n;
		parent = p;
	}
	
	public String toString()
	{
		String str = "";
		str += "\nNodeID: " + nodeid + "\n";	
		
		str += "ParentID: ";	
		if(parent != null)
		{
			str += parent.nodeid + "\n";
		}
		else
		{
			str += "Null \n";
		}	
		
		str += "Kids: " + kids.toString() + "\n";
		
		return str;
	}
	
}