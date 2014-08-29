package net.turrem.tvfexport.frame;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FrameAoSettings extends Frame
{
	public String recive;
	public String occlude;
	public String rayCount;
	public String rayLength;

	public FrameAoSettings(String recive, String occlude, String rayCount, String rayLength)
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

	public FrameAoSettings(Node node, ExportFrame export) throws TVFBuildSetupException
	{
		this.read(node, export);
	}

	public FrameAoSettings(FrameAoSettings parent, Node node, ExportFrame export) throws TVFBuildSetupException
	{
		this(parent);
		this.read(node, export);
	}

	private void read(Node aosettingsNode, ExportFrame export) throws TVFBuildSetupException
	{
		NodeList list = aosettingsNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			Node item = list.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				switch (item.getNodeName().toLowerCase())
				{
					case "recive":
						this.recive = item.getTextContent();
						break;
					case "occlude":
						this.occlude = item.getTextContent();
						break;
					case "raycount":
						this.rayCount = item.getTextContent();
						break;
					case "raylength":
						this.rayLength = item.getTextContent();
						break;
					default:
						throw new TVFBuildSetupException("Unknown node: " + item.getNodeName());
				}
			}
		}
	}

	@Override
	public void overrideSelf(Map<String, String> overrides)
	{
		this.occlude = this.overrideValue(this.occlude, overrides);
		this.rayCount = this.overrideValue(this.rayCount, overrides);
		this.rayLength = this.overrideValue(this.rayLength, overrides);
		this.recive = this.overrideValue(this.recive, overrides);
	}

	@Override
	public Frame duplicate()
	{
		return new FrameAoSettings(this);
	}
}
