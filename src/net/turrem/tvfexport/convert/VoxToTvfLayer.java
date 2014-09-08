package net.turrem.tvfexport.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import net.turrem.tvf.color.TVFColor;
import net.turrem.tvf.color.TVFPaletteColor;
import net.turrem.tvf.face.TVFFace;
import net.turrem.tvf.layer.TVFLayerFaces;
import net.turrem.utils.ao.Urchin;
import net.turrem.utils.geo.EnumDir;

public class VoxToTvfLayer
{
	protected static final byte[] defaultLight = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
	
	protected TVFLayerFaces tvf;
	protected VOXFile vox;
	protected Urchin urchin;

	private HashSet<Byte> usedColors = new HashSet<Byte>();
	private ArrayList<TVFFace> faces = new ArrayList<TVFFace>();
	
	protected boolean ao;
	public short xOffset;
	public short yOffset;
	public short zOffset;

	public VoxToTvfLayer(TVFLayerFaces tvf, VOXFile vox, Urchin urchin, boolean hasAo, short x, short y, short z)
	{
		this.tvf = tvf;
		this.vox = vox;
		this.urchin = urchin;
		this.ao = hasAo;
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}

	public TVFLayerFaces make(TvfBuilder builder)
	{
		this.build(builder);
		this.convert();
		return this.tvf;
	}

	private void build(TvfBuilder builder)
	{
		EnumDir[] dirs = EnumDir.values();

		for (int i = 0; i < this.vox.width; i++)
		{
			for (int j = 0; j < this.vox.height; j++)
			{
				for (int k = 0; k < this.vox.length; k++)
				{
					byte v = this.getVox(i, j, k);
					if (v != (byte) 0xFF)
					{
						for (int d = 0; d < dirs.length; d++)
						{
							EnumDir dir = dirs[d];
							
							if (builder.isOpen(i + this.xOffset, j + this.yOffset, k + this.zOffset, dir))
							{
								TVFFace f = new TVFFace();
								f.x = (byte) (i & 0xFF);
								f.y = (byte) (j & 0xFF);
								f.z = (byte) (k & 0xFF);
								f.direction = dir.ind;
								f.color = v;
								if (this.ao)
								{
									builder.doFaceAo(f, i + this.xOffset, j + this.yOffset, k + this.zOffset);
								}
								else
								{
									f.lighting = defaultLight;
								}
								this.faces.add(f);
								this.usedColors.add(v);
							}
						}
					}
				}
			}
		}
	}

	private void convert()
	{
		if (this.tvf.palette instanceof TVFPaletteColor)
		{
			TVFPaletteColor pal = (TVFPaletteColor) this.tvf.palette;
			Iterator<Byte> it = this.usedColors.iterator();

			while (it.hasNext())
			{
				int id = it.next() & 0xFF;
				VOXFile.VOXColor c = this.vox.colors[id];
				TVFColor C = new TVFColor(c.r << 2, c.g << 2, c.b << 2);
				pal.palette[id] = C;
			}
		}

		this.tvf.faces.addAll(this.faces);
	}

	private byte getVox(int x, int y, int z)
	{
		return this.vox.voxels[(x * this.vox.length + z) * this.vox.height + (this.vox.height - y - 1)];
	}
}
