package net.turrem.tvfexport.frame;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FrameLayer extends Frame
{
	public FrameAoSettings ao;
	public String show;
	public String vox;

	public FrameLayer(Node layer, ExportFrame export) throws TVFBuildSetupException
	{
		this.read(layer, export);
	}

	public FrameLayer(FrameLayer layer)
	{
		this.ao = (FrameAoSettings) layer.ao.duplicate();
		this.show = layer.show;
		this.vox = layer.vox;
	}

	public boolean readNodeItem(Node item, String name, ExportFrame export) throws TVFBuildSetupException
	{
		switch (name)
		{
			case "aosettings":
				this.ao = new FrameAoSettings(this.ao, item, export);
				return true;
			case "show":
				this.show = item.getTextContent();
				return true;
			case "vox":
				this.vox = item.getTextContent();
				return true;
			default:
				return false;
		}
	}

	private void read(Node layer, ExportFrame export) throws TVFBuildSetupException
	{
		this.ao = export.defaultao;
		NodeList list = layer.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			Node item = list.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				if (!this.readNodeItem(item, item.getNodeName().toLowerCase(), export))
				{
					throw new TVFBuildSetupException("Unknown node: " + item.getNodeName());
				}
			}
		}
	}

	@Override
	public void overrideSelf(Map<String, String> overrides)
	{
		this.show = this.overrideValue(this.show, overrides);
		this.vox = this.overrideValue(this.vox, overrides);
		this.ao.overrideSelf(overrides);
	}

	@Override
	public Frame duplicate()
	{
		return new FrameLayer(this);
	}
}
