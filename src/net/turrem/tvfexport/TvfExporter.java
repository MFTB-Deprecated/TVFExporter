package net.turrem.tvfexport;

import java.util.ArrayDeque;

import net.turrem.tvfexport.frame.ExportFrame;
import net.turrem.tvfexport.frame.FrameAoSettings;
import net.turrem.tvfexport.frame.FrameLayer;
import net.turrem.tvfexport.frame.FrameTVF;
import net.turrem.utils.ao.Urchin;

public class TvfExporter
{
	public ArrayDeque<FrameTVF> frames = new ArrayDeque<FrameTVF>();
	public Urchin urchin = null;
	private int lastCount;
	private int lastLength;
	
	public void addFrames(ExportFrame frame)
	{
		this.frames.addAll(frame.tvfs);
	}

	public void export(String indir, String outdir)
	{
		while (!this.frames.isEmpty())
		{
			this.exportSingle(this.frames.poll(), indir, outdir);
		}
	}
	
	protected void exportSingle(FrameTVF frame, String indir, String outdir)
	{
		for (FrameLayer layer : frame.theLayers)
		{
			this.updateUrchin(layer.ao);
			
		}
	}
	
	public void updateUrchin(FrameAoSettings ao)
	{
		int count = Integer.parseInt(ao.rayCount);
		int length = Integer.parseInt(ao.rayLength);
		if (urchin == null || this.lastCount != count || this.lastLength != length)
		{
			this.urchin = new Urchin(length, count);
			this.lastCount = count;
			this.lastLength = length;
		}
	}
}
