package net.turrem.tvfexport.frame;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExportFrame
{
	public FrameAoSettings defaultao = new FrameAoSettings("true", "true", "256", "64");
	public ArrayList<FrameTVF> tvfs = new ArrayList<FrameTVF>();
	public HashMap<String, FrameTVF> abstracts = new HashMap<String, FrameTVF>();

	public ExportFrame(Element export) throws TVFBuildSetupException
	{
		this.read(export);
		System.out.println("Done Reading Export Forms");
	}

	public void read(Element export) throws TVFBuildSetupException
	{
		NodeList items = export.getChildNodes();
		for (int i = 0; i < items.getLength(); i++)
		{
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				if (item.getNodeName().equalsIgnoreCase("aosettings"))
				{
					this.defaultao = new FrameAoSettings(item, this);
					break;
				}
			}
		}
		while (this.readAvalibleAbstracts(items))
		{
		}
		this.readTVFs(items);
	}

	public void readTVFs(NodeList items) throws TVFBuildSetupException
	{
		for (int i = 0; i < items.getLength(); i++)
		{
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				if (item.getNodeName().equalsIgnoreCase("overridetvf") || item.getNodeName().equalsIgnoreCase("tvf"))
				{
					FrameTVF parent = null;
					Node extend = item.getAttributes().getNamedItem("extends");
					if (extend != null)
					{
						if (this.abstracts.containsKey(extend.getTextContent()))
						{
							parent = this.abstracts.get(extend.getTextContent());
						}
						else
						{
							throw new TVFBuildSetupException("Missing abstract tvf export form(s)");
						}
					}
					this.tvfs.add(new FrameTVF(item, parent, this));
				}
			}
		}
	}

	public boolean readAvalibleAbstracts(NodeList items) throws TVFBuildSetupException
	{
		boolean flag = false;
		boolean hasunread = false;
		for (int i = 0; i < items.getLength(); i++)
		{
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				if (item.getNodeName().equalsIgnoreCase("abstracttvf"))
				{
					Node namen = item.getAttributes().getNamedItem("name");
					if (namen == null)
					{
						throw new TVFBuildSetupException("Unnamed abstract tvf export form");
					}
					String name = namen.getTextContent();
					if (!this.abstracts.containsKey(name))
					{
						hasunread = true;
						if (this.readAvalibleAbstract(item, name))
						{
							flag = true;
						}
					}
				}
			}
		}
		if (!flag && hasunread)
		{
			throw new TVFBuildSetupException("Missing abstract tvf export form(s)");
		}
		return flag;
	}

	public boolean readAvalibleAbstract(Node item, String name) throws TVFBuildSetupException
	{
		boolean flag = false;
		FrameTVF parent = null;
		Node extend = item.getAttributes().getNamedItem("extends");
		if (extend != null)
		{
			if (this.abstracts.containsKey(extend.getTextContent()))
			{
				parent = this.abstracts.get(extend.getTextContent());
				flag = true;
			}
			else
			{
				flag = false;
			}
		}
		else
		{
			flag = true;
		}
		if (flag)
		{
			this.abstracts.put(name, new FrameTVF(item, parent, this));
			return true;
		}
		else
		{
			return false;
		}
	}
}
