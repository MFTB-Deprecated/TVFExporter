package net.turrem.tvfexport;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.turrem.tvfexport.frame.ExportFrame;
import net.turrem.tvfexport.frame.TVFBuildSetupException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main
{
	public static void main(String[] args)
	{
		TvfExporter export = new TvfExporter();
		
		String dir = System.getProperty("user.dir");
		
		boolean compress = true;
		
		if (args.length >= 4)
		{
			compress = Boolean.parseBoolean(args[3]);
		}
		
		try
		{
			File file = new File(dir + args[2]);
			DocumentBuilder dBuilder;
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			ExportFrame frame = new ExportFrame(doc.getDocumentElement());
			export.addFrames(frame);
		}
		catch (TVFBuildSetupException | ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
		}
		
		export.export(dir + args[0], dir + args[1], compress);
	}
}
