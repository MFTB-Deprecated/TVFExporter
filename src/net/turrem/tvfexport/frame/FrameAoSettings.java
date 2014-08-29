package net.turrem.tvfexport.frame;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FrameAoSettings
{
	public boolean recive;
	public boolean occlude;
	public int rayCount;
	public int rayLength;
	
	public FrameAoSettings(boolean recive, boolean occlude, int rayCount, int rayLength)
	{
		this.recive = recive;
		this.occlude = occlude;
		this.rayCount = rayCount;
		this.rayLength = rayLength;
	}
	
	public FrameAoSettings(FrameAoSettings parent)
	{
		this(parent.recive, parent.occlude, parent.rayCount, parent.rayLength);
	}
	
	public FrameAoSettings(Node node)
	{
		this.read(node);
	}
	
	public FrameAoSettings(FrameAoSettings parent, Node node)
	{
		this(parent);
		this.read(node);
	}
	
	public void read(Node aosettingsNode)
	{
		NodeList list = aosettingsNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			Node item = list.item(i);
			switch (item.getNodeName().toLowerCase())
			{
				case "recive":
					this.recive = Boolean.parseBoolean(item.getNodeValue());
					break;
				case "occlude":
					this.occlude = Boolean.parseBoolean(item.getNodeValue());
					break;
				case "raycount":
					this.rayCount = Integer.parseInt(item.getNodeValue());
					break;
				case "raylength":
					this.rayLength = Integer.parseInt(item.getNodeValue());
					break;
			}
		}
	}
}
