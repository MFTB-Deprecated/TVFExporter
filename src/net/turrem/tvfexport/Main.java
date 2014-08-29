package net.turrem.tvfexport;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.turrem.tvfexport.frame.ExportFrame;
import net.turrem.tvfexport.frame.FrameTVF;
import net.turrem.tvfexport.frame.TVFBuildSetupException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main
{
	public static void main(String[] args)
	{
		File file = new File(args[0]);
		DocumentBuilder dBuilder;
		try
		{
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			ExportFrame frame = new ExportFrame(doc.getDocumentElement());
			for (FrameTVF tvf : frame.tvfs)
			{
				System.out.println("File: " + tvf.file);
			}
			System.out.println("...break..");
		}
		catch (TVFBuildSetupException | ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
