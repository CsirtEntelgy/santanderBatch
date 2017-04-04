package com.interfactura.firmalocal.xml.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class ListOfFiles 
	implements Enumeration<InputStream>
{

	private File[] listOfFiles;
	private int current = 0;

	public ListOfFiles(File[] listOfFiles) 
	{	this.listOfFiles = listOfFiles;	}
	
	public boolean hasMoreElements() 
	{
		if (current < listOfFiles.length)
		{	return true;	}
		else
		{	return false;	}
	}

	public InputStream nextElement() 
	{
		InputStream in = null;

		if (!hasMoreElements())
		{	throw new NoSuchElementException("No more files.");		}
		else 
		{
			File nextElement = listOfFiles[current];
			current++;
			try 
			{	in = new FileInputStream(nextElement);		} 
			catch (FileNotFoundException e) 
			{	System.err.println("ListOfFiles: Can't open " + nextElement);	}
		}
		return in;
	}

}
