package net.turrem.tvfexport.frame;

import java.util.Map;

public abstract class Frame
{
	public abstract void overrideSelf(Map<String, String> overrides);
	
	public Frame overrideNew(Map<String, String> overrides)
	{
		Frame f = this.duplicate();
		f.overrideSelf(overrides);
		return f;
	}
	
	public abstract Frame duplicate();
	
	public String overrideValue(String value, Map<String, String> overrides)
	{
		if (value == null)
		{
			return null;
		}
		if (value.startsWith("$"))
		{
			String key = value.substring(1);
			if (overrides.containsKey(key))
			{
				return overrides.get(key);
			}
		}
		return value;
	}
}
