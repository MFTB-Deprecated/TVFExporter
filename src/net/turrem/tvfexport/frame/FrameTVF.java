package net.turrem.tvfexport.frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FrameTVF extends Frame
{
	public HashMap<String, String> theOverrides = new HashMap<String, String>();
	public ArrayList<FrameLayer> theLayers = new ArrayList<FrameLayer>();
	public String file = null;

	public FrameTVF(FrameTVF tvf)
	{
		this.theOverrides = tvf.theOverrides;
		for (FrameLayer layer : tvf.theLayers)
		{
			this.theLayers.add((FrameLayer) layer.duplicate());
		}
		this.file = tvf.file;
	}

	public FrameTVF(Node node, FrameTVF parent, ExportFrame export) throws TVFBuildSetupException
	{
		this.read(node, export);
		if (parent != null)
		{
			this.extend(parent);
		}
	}

	public void read(Node tvf, ExportFrame export) throws TVFBuildSetupException
	{
		if (tvf.getNodeName().equalsIgnoreCase("overridetvf"))
		{
			this.readOverrideOnly(tvf, export);
		}
		else
		{
			NodeList list = tvf.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				Node item = list.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE)
				{
					switch (item.getNodeName().toLowerCase())
					{
						case "file":
							this.file = item.getTextContent();
							break;
						case "layer":
							this.theLayers.add(this.readLayer(item, export));
							break;
						case "overrides":
							this.readOverrideOnly(item, export);
							break;
						default:
							throw new TVFBuildSetupException("Unknown node: " + item.getNodeName());
					}
				}
			}
		}
	}

	public void readOverrideOnly(Node tvf, ExportFrame export)
	{
		NodeList list = tvf.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			Node item = list.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				this.theOverrides.put(item.getNodeName(), item.getTextContent());
			}
		}
	}

	public FrameLayer readLayer(Node layer, ExportFrame export) throws TVFBuildSetupException
	{
		Node typen = layer.getAttributes().getNamedItem("type");
		if (typen == null)
		{
			throw new TVFBuildSetupException("Layer does not have type");
		}
		String type = typen.getTextContent().toUpperCase();
		switch (type)
		{
			case "COLOR":
				return new FrameLayer(layer, export);
			case "DYNAMIC":
				return new FrameLayerDynamic(layer, export);
			case "SHADER":
				return new FrameLayerShader(layer, export);
			default:
				throw new TVFBuildSetupException("Layer does not have valid type");
		}
	}

	public void extend(FrameTVF parent)
	{
		FrameTVF newpar = (FrameTVF) parent.overrideNew(this.theOverrides);
		if (this.file == null)
		{
			this.file = newpar.file;
		}
		ArrayList<FrameLayer> nl = newpar.theLayers;
		nl.addAll(this.theLayers);
		this.theLayers = nl;
	}

	@Override
	public void overrideSelf(Map<String, String> overrides)
	{
		this.file = this.overrideValue(this.file, overrides);
		for (FrameLayer layer : this.theLayers)
		{
			layer.overrideSelf(overrides);
		}
	}

	@Override
	public Frame duplicate()
	{
		return new FrameTVF(this);
	}
}
