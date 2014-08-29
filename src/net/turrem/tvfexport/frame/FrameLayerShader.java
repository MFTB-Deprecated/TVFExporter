package net.turrem.tvfexport.frame;

import java.util.Map;

import org.w3c.dom.Node;

public class FrameLayerShader extends FrameLayer
{
	public String shader;
	public String uniform;
	
	public FrameLayerShader(Node layer, ExportFrame export) throws TVFBuildSetupException
	{
		super(layer, export);
	}
	
	public FrameLayerShader(FrameLayerShader layer)
	{
		super(layer);
		this.shader = layer.shader;
		this.uniform = layer.uniform;
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
				case "shader":
					this.shader = item.getTextContent();
					return true;
				case "uniform":
					this.uniform = item.getTextContent();
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
		this.shader = this.overrideValue(this.shader, overrides);
		this.uniform = this.overrideValue(this.uniform, overrides);
	}

	@Override
	public Frame duplicate()
	{
		return new FrameLayerShader(this);
	}
}
