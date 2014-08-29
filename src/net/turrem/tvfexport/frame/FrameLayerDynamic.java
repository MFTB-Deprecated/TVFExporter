package net.turrem.tvfexport.frame;

import java.util.Map;

import org.w3c.dom.Node;

public class FrameLayerDynamic extends FrameLayer
{
	public String color;
	public String mix;
	
	public FrameLayerDynamic(Node layer, ExportFrame export) throws TVFBuildSetupException
	{
		super(layer, export);
	}
	
	public FrameLayerDynamic(FrameLayerDynamic layer)
	{
		super(layer);
		this.color = layer.color;
		this.mix = layer.mix;
	}
	
	@Override
	public boolean readNodeItem(Node item, String name, ExportFrame export) throws TVFBuildSetupException
	{
		if (super.readNodeItem(item, name, export))
		{
			return true;
		}
		else
		{
			switch (name)
			{
				case "color":
					this.color = item.getTextContent();
					return true;
				case "mix":
					this.mix = item.getTextContent();
					return true;
				default:
					return false;
			}
		}
	}
	
	@Override
	public void overrideSelf(Map<String, String> overrides)
	{
		super.overrideSelf(overrides);
		this.color = this.overrideValue(this.color, overrides);
		this.mix = this.overrideValue(this.mix, overrides);
	}

	@Override
	public Frame duplicate()
	{
		return new FrameLayerDynamic(this);
	}
}
