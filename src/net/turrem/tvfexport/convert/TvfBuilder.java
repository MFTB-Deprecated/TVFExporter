package net.turrem.tvfexport.convert;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import net.turrem.tvf.TVFFile;
import net.turrem.tvf.color.EnumDynamicColorMode;
import net.turrem.tvf.color.TVFPaletteColor;
import net.turrem.tvf.color.TVFPaletteDynamic;
import net.turrem.tvf.color.TVFPaletteShader;
import net.turrem.tvf.layer.TVFLayerFaces;
import net.turrem.tvfexport.TvfExporter;
import net.turrem.tvfexport.frame.FrameLayer;
import net.turrem.tvfexport.frame.FrameLayerDynamic;
import net.turrem.tvfexport.frame.FrameLayerShader;
import net.turrem.tvfexport.frame.FrameTVF;
import net.turrem.utils.geo.EnumDir;

public class TvfBuilder
{
	public FrameTVF frame;
	public TVFFile tvf;
	public File indir;
	public File outdir;
	public TvfExporter export;
	
	public HashMap<String, VoxHolder> voxs = new HashMap<String, VoxHolder>();
	
	protected static class VoxHolder
	{
		public VOXFile vox;
		public short xOffset;
		public short yOffset;
		public short zOffset;
		
		public VoxHolder(VOXFile vox, short xOffset, short yOffset, short zOffset)
		{
			super();
			this.vox = vox;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zOffset = zOffset;
		}
	}
	
	public TvfBuilder(FrameTVF frame, String indir, String outdir, TvfExporter export) throws FileNotFoundException
	{
		this.frame = frame;
		this.indir = new File(indir);
		this.outdir = new File(outdir);
		if (!this.indir.isDirectory() || !this.indir.exists())
		{
			throw new FileNotFoundException();
		}
		this.export = export;
		this.tvf = new TVFFile();
	}
	
	public TVFFile convert() throws NumberFormatException, IOException
	{
		this.loadVox();
		this.convertLayers();
		this.tvf.width = Short.parseShort(this.frame.width);
		this.tvf.height = Short.parseShort(this.frame.height);
		this.tvf.length = Short.parseShort(this.frame.length);
		return this.tvf;
	}
	
	protected void convertLayers()
	{
		for (int i = 0; i < this.frame.theLayers.size(); i++)
		{
			FrameLayer layer = this.frame.theLayers.get(i);
			this.export.updateUrchin(layer.ao);
			VoxHolder holder = this.voxs.get(layer.vox);
			VOXFile vox = holder.vox;
			TVFLayerFaces faces = new TVFLayerFaces();
			this.convertPalette(layer, faces);
			(new VoxToTvfLayer(faces, vox, this.export.urchin)).make(this);
			faces.xOffset = holder.xOffset;
			faces.yOffset = holder.yOffset;
			faces.zOffset = holder.zOffset;
			this.tvf.layers.add(faces);
		}
	}
	
	protected void convertPalette(FrameLayer layer, TVFLayerFaces faces)
	{
		if (layer instanceof FrameLayerDynamic)
		{
			FrameLayerDynamic frame = (FrameLayerDynamic) layer;
			TVFPaletteDynamic pal = new TVFPaletteDynamic();
			pal.mode = EnumDynamicColorMode.valueOf(frame.mix);
			pal.colorChannel = (byte) Integer.parseInt(frame.color);
			faces.palette = pal;
		}
		else if (layer instanceof FrameLayerShader)
		{
			FrameLayerShader frame = (FrameLayerShader) layer;
			TVFPaletteShader pal = new TVFPaletteShader();
			pal.shader = frame.shader;
			pal.uniformChannel = (byte) Integer.parseInt(frame.uniform);
			faces.palette = pal;
		}
		else
		{
			faces.palette = new TVFPaletteColor();
		}
	}
	
	protected void loadVox() throws FileNotFoundException, IOException
	{
		for (FrameLayer layer : this.frame.theLayers)
		{
			String name = layer.vox;
			String vox = layer.vox;
			vox = vox.replaceAll("\\.", "/");
			vox += ".vox";
			vox = this.indir.getAbsolutePath() + "/" + vox;
			DataInputStream input = new DataInputStream(new FileInputStream(vox));
			VOXFile file = VOXFile.read(input);
			input.close();
			int x = Short.parseShort(layer.xOffset) + Short.parseShort(this.frame.xOffset);
			int y = Short.parseShort(layer.yOffset) + Short.parseShort(this.frame.yOffset);
			int z = Short.parseShort(layer.zOffset) + Short.parseShort(this.frame.zOffset);
			VoxHolder holder = new VoxHolder(file, (short) x, (short) y, (short) z);
			this.voxs.put(name, holder);
		}
	}
	
	protected boolean isOpen(int x, int y, int z, EnumDir facedir)
	{
		return this.isOpen(x + facedir.xoff, y + facedir.yoff, z + facedir.zoff);
	}
	
	protected boolean isOpen(int x, int y, int z)
	{
		boolean open = true;
		for (VoxHolder vox : this.voxs.values())
		{
			open &= this.isOpen(x, y, z, vox);
		}
		return open;
	}
	
	protected boolean isOpen(int x, int y, int z, VoxHolder vox)
	{
		return this.getVoxel(x, y, z, vox) == (byte) 0xFF;
	}
	
	protected boolean isOutside(int x, int y, int z, VoxHolder vox)
	{
		x -= vox.xOffset;
		y -= vox.yOffset;
		z -= vox.zOffset;
		if (x < 0 || y < 0|| z < 0)
		{
			return true;
		}
		if (x >= vox.vox.width || y >= vox.vox.height || z >= vox.vox.length)
		{
			return true;
		}
		return false;
	}
	
	protected byte getVoxel(int x, int y, int z, VoxHolder vox)
	{
		if (this.isOutside(x, y, z, vox))
		{
			return (byte) 0xFF;
		}
		x -= vox.xOffset;
		y -= vox.yOffset;
		z -= vox.zOffset;
		return vox.vox.voxels[(x * vox.vox.length + z) * vox.vox.height + (vox.vox.height - y - 1)];
	}
}
