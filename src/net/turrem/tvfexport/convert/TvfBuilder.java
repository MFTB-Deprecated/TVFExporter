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
import net.turrem.tvf.face.TVFFace;
import net.turrem.tvf.layer.TVFLayerFaces;
import net.turrem.tvfexport.TvfExporter;
import net.turrem.tvfexport.frame.FrameLayer;
import net.turrem.tvfexport.frame.FrameLayerDynamic;
import net.turrem.tvfexport.frame.FrameLayerShader;
import net.turrem.tvfexport.frame.FrameTVF;
import net.turrem.utils.ao.AORay;
import net.turrem.utils.ao.Urchin;
import net.turrem.utils.geo.EnumDir;
import net.turrem.utils.geo.VoxelGeoUtils;

public class TvfBuilder
{
	public static final float aoPower = 0.8F;
	
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
		public boolean occludes;

		public VoxHolder(VOXFile vox, short xOffset, short yOffset, short zOffset, boolean occludes)
		{
			this.vox = vox;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zOffset = zOffset;
			this.occludes = occludes;
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
			(new VoxToTvfLayer(faces, vox, this.export.urchin, Boolean.parseBoolean(layer.ao.recive), holder.xOffset, holder.yOffset, holder.zOffset)).make(this);
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
			VoxHolder holder = new VoxHolder(file, (short) x, (short) y, (short) z, Boolean.parseBoolean(layer.ao.occlude));
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
			open &= this.isOpen(x - vox.xOffset, y - vox.yOffset, z - vox.zOffset, vox);
		}
		return open;
	}

	protected boolean voxelOccludes(int x, int y, int z)
	{
		boolean open = true;
		for (VoxHolder vox : this.voxs.values())
		{
			if (vox.occludes)
			{
				open &= this.isOpen(x - vox.xOffset, y - vox.yOffset, z - vox.zOffset, vox);
			}
		}
		return !open;
	}

	protected boolean isOpen(int x, int y, int z, VoxHolder vox)
	{
		return this.getVoxel(x, y, z, vox) == (byte) 0xFF;
	}

	protected boolean isOutside(int x, int y, int z, VoxHolder vox)
	{
		if (x < 0 || y < 0 || z < 0)
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
		return vox.vox.voxels[(x * vox.vox.length + z) * vox.vox.height + (vox.vox.height - y - 1)];
	}

	public void doFaceAo(TVFFace face, int x, int y, int z)
	{
		face.lighting = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
		Urchin urc = this.export.urchin;
		for (int i = 0; i < 4; i++)
		{
			EnumDir dir = EnumDir.values()[face.direction];
			int[][] off = VoxelGeoUtils.getOccludingVoxels(dir, i);
			float occ = 0.0F;
			occ += this.doRayCast(x, y, z, urc, dir);
			occ += this.doRayCast(x + off[0][0], y + off[0][1], z + off[0][2], urc, dir);
			occ += this.doRayCast(x + off[1][0], y + off[1][1], z + off[1][2], urc, dir);
			occ += this.doRayCast(x + off[2][0], y + off[2][1], z + off[2][2], urc, dir);
			occ /= 4.0F;
			face.lighting[i] = this.occludeByte(face.lighting[i], occ, aoPower);
		}
	}
	
	private float doRayCast(int x, int y, int z, Urchin urc, EnumDir dir)
	{
		float mag = 0.0F;
		for (AORay ray : urc.rays)
		{
			boolean hit = false;
			for (int i = 0; i < ray.points.length; i++)
			{
				if (this.voxelOccludes(ray.points[i].x + x, ray.points[i].y + y, ray.points[i].z + z))
				{
					hit = true;
					break;
				}
			}
			if (!hit)
			{
				mag += ray.getMagnitude(dir);
			}
		}
		return mag / urc.getMagnitude(dir);
	}

	private byte occludeByte(byte light, float occ, float mag)
	{
		float lightf = (light & 0xFF) / 255.0F;
		lightf = (1.0F - mag) * lightf + mag * occ * lightf;
		if (lightf < 0.0F)
		{
			lightf = 0.0F;
		}
		if (lightf > 1.0F)
		{
			lightf = 1.0F;
		}
		return (byte) (lightf * 255.0F);
	}
}
