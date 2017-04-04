package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReportExcelDivisasController {

	@Autowired
	Properties properties;
	
	String reportesDivisasEntrada=MassiveReportReadDivisasController.reportesDivisasEntrada;
	String reportesDivisasProceso=MassiveReportReadDivisasController.reportesDivisasProceso;
	String reportesDivisasSalida=MassiveReportReadDivisasController.reportesDivisasSalida;

	public void readIdReportProcess(String nProceso)
	{
		try
		{
			FileInputStream fsIdReportProcess = new FileInputStream(reportesDivisasEntrada + "IDREPORTPROCESS_" + nProceso + ".TXT");
			DataInputStream in = new DataInputStream(fsIdReportProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(reportesDivisasProceso + "reportExcelDivisas_" + nProceso + ".txt");
			fileStatus.write("Status del proceso bash reportExcelDivisas.sh\n".getBytes("UTF-8"));
			
			String strID;
			int counter = 0;
			while((strID = br.readLine()) != null)
			{
				File fileDirectory = new File(reportesDivisasProceso + strID + "/");
				if(fileDirectory.exists())
				{
					File fileCFD = new File(reportesDivisasProceso + strID + "/" + strID + "REPORT.TXT");
					if(fileCFD.exists())
					{
						buildReportXlsx(strID);
						compressZip(strID);
						fileStatus.write(("El archivo " + strID + "REPORT.ZIP ha sido generado exitosamente\n").getBytes("UTF-8"));
					}
					else
					{
						File fileDirectoryelse = new File(reportesDivisasSalida + strID);
						if(!fileDirectoryelse.exists())
						{	fileDirectoryelse.mkdir();	}
						fileStatus.write(("No se encontro el archivo " + strID + "REPORT.TXT en la ruta " + reportesDivisasProceso + strID + "/\n").getBytes("UTF-8"));
						userlog =  new FileOutputStream(reportesDivisasSalida + strID + "/STATUS.txt");
						userlog.write(("El reporte " + strID + " no fue generado.\n").getBytes("UTF-8"));
						compressZip(strID);
					}				
				}
				else
				{
					File fileDirectoryelse = new File(reportesDivisasSalida + strID);
					if(!fileDirectoryelse.exists())
					{	fileDirectoryelse.mkdir();	}
					fileStatus.write(("No se encontro el directorio " + reportesDivisasProceso + strID + "/\n").getBytes("UTF-8"));
					userlog =  new FileOutputStream(reportesDivisasSalida + strID + "/STATUS.txt");
					userlog.write(("El reporte " + strID + " no fue generado.\n").getBytes("UTF-8"));
					compressZip(strID);
				}			
				counter++;
			}
			br.close();
			in.close();
			fsIdReportProcess.close();
			if(counter == 0)
			{	fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));		}
			fileStatus.close();
			if(userlog != null)
			{	userlog.close();	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception reportExcel:" + e.getMessage());
			try 
			{
				FileOutputStream fileError = new FileOutputStream(reportesDivisasProceso + "reportExcelDivisasError_" + nProceso + ".TXT");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
				System.out.println("Exception al crear reportExcelDivisasError_" + nProceso + ".TXT:" + e.getMessage());
			}			
		}		
	}
	
	public void compressZip(String ID) 
			throws Exception
	{
		String zipFile = reportesDivisasSalida + ID + "REPORT.ZIP";		
		String srcDir = reportesDivisasSalida  + ID + "/";	
		FileOutputStream fOutput = new FileOutputStream(zipFile);
		ZipOutputStream zOutput = new ZipOutputStream(fOutput);		
		File srcFiles = new File(srcDir);		
		buildZip(zOutput, srcFiles);
		zOutput.close();
	}
	
	public void buildZip(ZipOutputStream zOutput, File srcFiles) 
			throws Exception
	{
		File[] files = srcFiles.listFiles();
		System.out.println("Adding directory: " + srcFiles.getName());
		for (int i = 0; i < files.length; i++) 
		{			
			if (files[i].isDirectory()) 
			{
				buildZip(zOutput, files[i]);
				continue;
			}			
			System.out.println("tAdding file: " + files[i].getName());
			byte[] buffer = new byte[1024];
			FileInputStream fis = new FileInputStream(files[i]);
			zOutput.putNextEntry(new ZipEntry(files[i].getName()));
			int length;
			while ((length = fis.read(buffer)) > 0) 
			{	zOutput.write(buffer, 0, length);	}
			zOutput.closeEntry();
			// close the InputStream
			fis.close();		
		}
	}
	
	public void buildReportXlsx(String ID) 
			throws Exception
	{			
		String [] arrayHeads = new String [] {"FOLIO","FOLIO INTERNO","FOLIO SAT","TIPO DE FORMATO","STATUS","FECHA DE EMISION","FECHA DE CANCELACION","RFC EMISOR",
				"ENTIDAD","SERIE","TIPO DE COMPROBANTE","TIPO DE OPERACION","MONEDA","TIPO DE CAMBIO","RFC CLIENTE","ID EXTRANJERO","NOMBRE DEL CLIENTE","METODO DE PAGO",
				"REGIMEN FISCAL","LUGAR DE EXPEDICION","FORMA DE PAGO","NUM CTA PAGO","CALLE","NUM INTERIOR","NUM EXTERIOR","COLONIA","LOCALIDAD","REFERENCIA",
				"MUNICIPIO","ESTADO","PAIS","CODIGO POSTAL","CODIGO DE CLIENTE","NUM CONTRATO","PERIODO","CENTRO DE COSTOS","DESCRIPCION CONCEPTO","TASA IVA","SUBTOTAL","IVA","TOTAL",
				"TIPO DE ADDENDA","EMAIL","CODIGO ISO","ORDEN COMPRA","POSICION COMPRA","CUENTA CONTABLE","CENTRO DE COSTOS","NUM CONTRATO","FECHA VENCIMIENTO",
				"NOMBRE DE BENEFICIARIO","INSTITUCION RECEPTORA","NUM CTA","NUM DE PROVEEDOR", "MOTIVO DESCUENTO", "DESCUENTO", "NOMBRE USUARIO", "NOMBRE DE AREA", "SUBTOTAL ORIGEN", "IVA ORIGEN", "TOTAL ORIGEN"};
			//MAke directory
			File fileDirectory = new File(reportesDivisasSalida + ID);
			if(!fileDirectory.exists())
			{	fileDirectory.mkdir();	}						
		    /***********************************************************************/			
			RandomAccessFile aFile = new RandomAccessFile(reportesDivisasProceso + ID + "/" + ID + "REPORT.TXT", "r");
		    FileChannel inChannel = aFile.getChannel();
		    ByteBuffer buffer = ByteBuffer.allocate(1024);		    		    
		    FileOutputStream salidaExcel = null;
		    String strFields = "";
		    int counterTotal = 0;
		    int counter =8;		    
		    int numberExcel =0;
		    boolean thereIsRows = false;
		    int counterByte = 0;
		    //Creat WorkBook Zero
		    SXSSFWorkbook wb = new SXSSFWorkbook(); // keep 100 rows in memory, exceeding rows will be flushed to disk
			// wb.setCompressTempFiles(true);   
			Sheet sh = wb.createSheet("CFD Emitido Divisas");			
			//build details			
			//font
			Font font = wb.createFont();
		    font.setFontHeightInPoints((short)9);
		    font.setFontName("Arial");
		    font.setBoldweight(font.BOLDWEIGHT_BOLD);		    
		    font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
		    int nColHeads = 15;
		    //font			
			String strDate;			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			strDate = sdf.format(new Date());						
			CellStyle styleDate = wb.createCellStyle();
			styleDate.setFont(font);					
			Row rowDate = sh.createRow(1);			
			Cell cellD0 = rowDate.createCell(0);	                
	        cellD0.setCellValue("Fecha de Elaboración " + strDate);
	        cellD0.setCellStyle(styleDate);
			sh.addMergedRegion(new CellRangeAddress(
		            1, //first row (0-based)
		            1, //last row  (0-based)
		            0, //first column (0-based)
		            nColHeads  //last column  (0-based)
		    ));			
			CellStyle styleTitleInfo = wb.createCellStyle();
			styleTitleInfo.setFont(font);					
			Row rowTitle = sh.createRow(3);			
			Cell cellT0 = rowTitle.createCell(0);	                
	        cellT0.setCellValue("FACTURACIÓN DIRECCIÓN CONTABILIDAD GENERAL");
	        cellT0.setCellStyle(styleTitleInfo);
			sh.addMergedRegion(new CellRangeAddress(
		            3, //first row (0-based)
		            3, //last row  (0-based)
		            0, //first column (0-based)
		            nColHeads  //last column  (0-based)
		    ));
			
			Row rowInformation = sh.createRow(6);			
			Cell cellI0 = rowInformation.createCell(0);	                
	        cellI0.setCellValue("Información de " + "CFD Emitido Divisas" + " al " + strDate);        
	        cellI0.setCellStyle(styleTitleInfo);
			sh.addMergedRegion(new CellRangeAddress(
		            6, //first row (0-based)
		            6, //last row  (0-based)
		            0, //first column (0-based)
		            nColHeads  //last column  (0-based)
		    ));			
			//build Headers						
			int maxConcepts0 = getMaxConcepts(ID);
			Row row = buildHeader(sh.createRow(8), arrayHeads, maxConcepts0, wb.createCellStyle(), wb.createFont());			
			//styles cells
			//Cells Styles
    		Font fontData = wb.createFont();
            fontData.setFontHeightInPoints((short)9);
            fontData.setFontName("Arial");            		    
            fontData.setColor(IndexedColors.GREY_80_PERCENT.getIndex());    	    
            CellStyle styleDataP = wb.createCellStyle();            
            styleDataP.setFont(fontData);
            styleDataP.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            styleDataP.setFillPattern(CellStyle.SOLID_FOREGROUND);
                        
            CellStyle styleData = wb.createCellStyle();            
            styleData.setFont(fontData);	
			
    		while(inChannel.read(buffer) > 0)
		    {		    	
		        buffer.flip();
		        for (int i = 0; i < buffer.limit(); i++)
		        {
		        	if(counter>49000)
		        	{
		        		int totalCells = arrayHeads.length + maxConcepts0; 
		        		for(int indexCol=0; indexCol<totalCells; indexCol++)
		        		{	sh.autoSizeColumn((short)indexCol);	}		        		
			    		salidaExcel = new FileOutputStream(reportesDivisasSalida + ID + "/" + ID + "REPORT_" + numberExcel + ".XLSX");
			    		wb.write(salidaExcel);
			    		wb.dispose();
			    		salidaExcel.close();			    		
			    		wb = new SXSSFWorkbook(); // keep 100 rows in memory, exceeding rows will be flushed to disk
			    		// wb.setCompressTempFiles(true);   
			    		sh = wb.createSheet();			    		
			    		row = buildHeader(sh.createRow(8), arrayHeads, maxConcepts0, wb.createCellStyle(), wb.createFont());			    					    		 
			    		fontData = wb.createFont();
			            fontData.setFontHeightInPoints((short)9);
			            fontData.setFontName("Arial");            		    
			            fontData.setColor(IndexedColors.GREY_80_PERCENT.getIndex());			            
			            styleDataP = wb.createCellStyle();            
			            styleDataP.setFont(fontData);
			            styleDataP.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			            styleDataP.setFillPattern(CellStyle.SOLID_FOREGROUND);			                        
			            styleData = wb.createCellStyle();            
			            styleData.setFont(fontData);		        				        		
			    		counter = 8;
			    		numberExcel++;
			    		thereIsRows = false;
			    	}
		        	
		        	byte valor = buffer.get(); 
		        	System.out.print((char) valor);
		        	if(valor != '\n')
		        	{	
		        		strFields+=(char)valor;
		        		counterByte++;
		        	}
		        	else
		        	{		        			        			
	        			Row rowF = sh.createRow(counter+1);	    	            	    	            
	        			double tipoCambio = 0.0;
	        			double subTotal = 0.0;
	        			double iva = 0.0;
	        			double total = 0.0;
	        			boolean ultimaCelda = false;	        			
	    	            //String arrayValues[] = strFields.split("<<delimiter>>");
	        			//El limite del split se coloca en -1 para que respete los campos vacios
	        			String arrayValues[] = strFields.split(">");
	    	            int nCell = 1;
	    	            for(int cellnum = 1; cellnum < arrayValues.length; cellnum++){
	    	                Cell cell = rowF.createCell(nCell-1);
	    	                //String address = new CellReference(cell).formatAsString();
	    	                org.apache.commons.lang3.text.translate.UnicodeUnescaper unescaper = new org.apache.commons.lang3.text.translate.UnicodeUnescaper();

	    	                DecimalFormat df_ = new DecimalFormat("0.00");
	    	                
	    	                String strUnescape = "";	    	                
	    	                if(cellnum == 4)
	    	                {
	    	                	strUnescape = unescaper.translate(getFormatType(arrayValues[cellnum]));
	    	                	cell.setCellValue(strUnescape);
	    	                }
	    	                else if(cellnum == 5)
	    	                {
	    	                	strUnescape = unescaper.translate(getStatus(arrayValues[cellnum]));
	    	                	cell.setCellValue(strUnescape);
	    	                }
	    	                else if(cellnum == 11)
	    	                {
	    	                	String [] arrayTipos = arrayValues[cellnum].split("\\|");	    	                	
	    	                	cell.setCellValue(arrayTipos[0]);	    	                	
	    	                	nCell++;
	    	                	Cell cellTipo = rowF.createCell(nCell-1);
	    	                	if(arrayTipos.length>1)
	    	                		cellTipo.setCellValue(arrayTipos[1]);
	    	                	else
	    	                		cellTipo.setCellValue("");	    	                
	    	                	if(counter % 2 == 0)
	    	                	{	cellTipo.setCellStyle(styleDataP);	}
	    	                	else
	    	                	{	cellTipo.setCellStyle(styleData);	}	    	                	
	    	                }
	    	                else if(cellnum == 13)
	    	                {	    	                		    	                	
	    	                	System.out.println("TipoCambio:" + arrayValues[cellnum]);
	    	                	if(!arrayValues[cellnum].equals(""))
	    	                	{	tipoCambio = Double.parseDouble(arrayValues[cellnum]);	}
	    	                	strUnescape = df_.format(tipoCambio);
	    	                	cell.setCellValue(strUnescape);
	    	                }
	    	                else if(cellnum == 38)
	    	                {
	    	                	System.out.println("SubTotal:" + arrayValues[cellnum]);
	    	                	if(!arrayValues[cellnum].equals(""))
	    	                		subTotal = Double.parseDouble(arrayValues[cellnum]);		
	    	                	
	    	                	if(tipoCambio != 0)
	    	                	  	strUnescape = df_.format(subTotal*tipoCambio);
	    	                	else
	    	                		strUnescape = df_.format(subTotal);
	    	                	
	    	                	cell.setCellValue(strUnescape);	    	                	
	    	                }
	    	                else if(cellnum == 39)
	    	                {
	    	                	System.out.println("Iva:" + arrayValues[cellnum]);
	    	                	if(!arrayValues[cellnum].equals(""))
	    	                		iva = Double.parseDouble(arrayValues[cellnum]);	
	    	                	
	    	                	if(tipoCambio != 0)
	    	                		strUnescape = df_.format(iva*tipoCambio);
	    	                	else
	    	                		strUnescape = df_.format(iva);
	    	                	     	                	
	    	                	cell.setCellValue(strUnescape);	
	    	                }
	    	                else if(cellnum == 40)
	    	                {
	    	                	System.out.println("Total:" + arrayValues[cellnum]);
	    	                	if(!arrayValues[cellnum].equals(""))
	    	                		total = Double.parseDouble(arrayValues[cellnum]);	
	    	                	
	    	                	if(tipoCambio != 0)
	    	                		strUnescape = df_.format(total*tipoCambio);
	    	                	else
	    	                		strUnescape = df_.format(total);
	    	                	     	                	
	    	                	cell.setCellValue(strUnescape);	
	    	                }
	    	                else if(cellnum == 41)
	    	                {
	    	                	strUnescape = unescaper.translate(getTipoAddenda(arrayValues[cellnum]));
	    	                	cell.setCellValue(strUnescape);
	    	                }
	    	                else if(cellnum == 57)
	    	                {
	    	                	//Ultima celda
	    	                	strUnescape = unescaper.translate(arrayValues[cellnum].trim());
	    	                	cell.setCellValue(strUnescape);
	    	                	ultimaCelda = true;               	
	    	                }
	    	                else
	    	                {
	    	                	strUnescape = unescaper.translate(arrayValues[cellnum].trim());
	    	                	cell.setCellValue(strUnescape);
	    	                }
	    	                
	    	                //cell.setCellValue(arrayValues[cellnum]);	    	                	    	                
	    	                if(counter % 2 == 0)
	    	                {	cell.setCellStyle(styleDataP);	}
	    	                else
	    	                {	cell.setCellStyle(styleData);	}   	
	    	                	    	                
	    	                if(ultimaCelda)
	    	                {
	    	                	nCell++;
	    	                	Cell cellSubTotal = rowF.createCell(nCell-1);
	    	                	nCell++;
	    	                	Cell cellIva = rowF.createCell(nCell-1);
	    	                	nCell++;
	    	                	Cell cellTotal = rowF.createCell(nCell-1);
	    	                	
	    	                	cellSubTotal.setCellValue(df_.format(subTotal));
    	                		cellIva.setCellValue(df_.format(iva));
    	                		cellTotal.setCellValue(df_.format(total));	    	                	
	    	                	
	    	                	if(counter % 2 == 0)
	    	                	{
	    	                		cellSubTotal.setCellStyle(styleDataP);
	    	                		cellIva.setCellStyle(styleDataP);
	    	                		cellTotal.setCellStyle(styleDataP);
		    	                }
	    	                	else
	    	                	{
		    	                	cellSubTotal.setCellStyle(styleData);
		    	                	cellIva.setCellStyle(styleData);
		    	                	cellTotal.setCellStyle(styleData);
		    	                }
	    	                	//para que ya no repita
	    	                	ultimaCelda = false;
	    	                }   	
	    	                nCell++;
	    	            }  			    	            
		        		strFields="";
		        		counter++;
		        		counterTotal++;
		        		thereIsRows = true;
		        		counterByte = 0;		        			        		
		        	}
		        }
		        buffer.clear(); // do something with the data and clear/compact it.
		    }
		    inChannel.close();
		    aFile.close();
		    System.out.println("maxConceptos:" + maxConcepts0);
		    
		    if(thereIsRows)
		    {		    	
        		int totalCells = arrayHeads.length + maxConcepts0; 
        		for(int indexCol=0; indexCol<totalCells; indexCol++)
        		{	sh.autoSizeColumn((short)indexCol);	}        		
		    	salidaExcel = new FileOutputStream(reportesDivisasSalida + ID + "/" + ID + "REPORT_" + numberExcel + ".XLSX");
	    		wb.write(salidaExcel);
	    		wb.dispose();
	    		salidaExcel.close();	    		
		    }
		    
		  //Si el archivo está vacío
		    System.out.println("countertotal: " + counterTotal);
		    if(counterTotal == 0)
		    {
		    	Row rowF = sh.createRow(0);		    	
		    	Cell cell = rowF.createCell(0);
                cell.setCellValue("No se encontraron datos para generar el Reporte.");                
                cell.setCellStyle(styleData);                
                sh.autoSizeColumn((short)0);                
                salidaExcel = new FileOutputStream(reportesDivisasSalida + ID + "/" + ID + "REPORT_" + numberExcel + ".XLSX");                
	    		wb.write(salidaExcel);
	    		wb.dispose();
	    		salidaExcel.close();
		    }
		
	}
	
	public double round(double value, int places) 
	{
	    if (places < 0) throw new IllegalArgumentException();
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
    
	public String getFormatType(String strValue)
	{
		if(strValue.trim().equals("1"))
		{	return "Formato Único";		}
		else if(strValue.trim().equals("2"))
		{	return "Factoraje";		}
		else if(strValue.trim().equals("3"))
		{	return "Donataria";		}
		else if(strValue.trim().equals("4"))
		{	return "Divisas";	}
		else
		{	return strValue;	}
	}
	
	public String getStatus(String strValue)
	{
		if(strValue.trim().equals("1"))
		{	return "Vigente";	}
		else if(strValue.trim().equals("0"))
		{	return "Cancelado";	}	
		else if(strValue.trim().equals("2"))
		{	return "Cancelación en proceso";		}
		else
		{	return strValue;	}
	}
	
	public String getTipoAddenda(String strValue)
	{
		if(strValue.trim().equals("0"))
		{	return "Addenda Santander";		}
		else if(strValue.trim().equals("1"))
		{	return "Addenda Logística";		}
		else if(strValue.trim().equals("2"))
		{	return "Addenda Financiera";	}
		else if(strValue.trim().equals("3"))
		{	return "Addenda Arrendamiento";	}
		else
		{	return strValue;	}
	}
	
	public Row buildHeader(Row row, String [] arrayHeads, int maxConcepts0, CellStyle styleHead, Font font)
			throws Exception
	{		
	    font.setFontHeightInPoints((short)9);
	    font.setFontName("Arial");
	    font.setBoldweight(font.BOLDWEIGHT_BOLD);		    
	    font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
	    
		styleHead.setFont(font);
				
		for(int iHead=0; iHead<arrayHeads.length; iHead++)
		{		        			
    		Cell cell = row.createCell(iHead);cell.setCellValue(arrayHeads[iHead]);
    		cell.setCellStyle(styleHead);
		}
		//int totalConcepts = maxConcepts0/5;
		int totalConcepts = maxConcepts0;
		int iCell = arrayHeads.length;
		for(int iCon=0; iCon<totalConcepts; iCon++)
		{		        			
    		Cell cellQty = row.createCell(iCell);cellQty.setCellValue("CANTIDAD");
    		cellQty.setCellStyle(styleHead);
    		Cell cellUM = row.createCell(iCell+1);cellUM.setCellValue("UM");
    		cellUM.setCellStyle(styleHead);
    		Cell cellDes = row.createCell(iCell+2);cellDes.setCellValue("DESCRIPCION");
    		cellDes.setCellStyle(styleHead);
    		Cell cellUPr = row.createCell(iCell+3);cellUPr.setCellValue("PRECIO UNITARIO");
    		cellUPr.setCellStyle(styleHead);
    		Cell cellAmount = row.createCell(iCell+4);cellAmount.setCellValue("IMPORTE");
    		cellAmount.setCellStyle(styleHead);
    		iCell = iCell + 5;
		}
		return row;
	}
	
	public int getMaxConcepts(String ID)
			throws Exception
	{
		FileInputStream fs = new FileInputStream(reportesDivisasProceso + ID + "/" + ID + "MAXCONCEPTS.TXT");
		DataInputStream in = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String strLine;
		int counter = 0;
		String strMaxConcepts = "";
		while((strLine = br.readLine()) != null)
		{
			if(!strLine.trim().equals(""))
			{
				strMaxConcepts = strLine;
				System.out.println("MaxConcepts:" + strMaxConcepts + "-Counter:" + counter);
				counter++;
			}
			
		}		
		br.close();
		in.close();
		fs.close();
		if(strMaxConcepts.trim().equals(""))
			strMaxConcepts = "0";
		return Integer.parseInt(strMaxConcepts.trim());
	}
	
	
	/*public int getMaxConcepts(String ID) throws Exception{
		RandomAccessFile aFile = new RandomAccessFile(reportesDivisasProceso + ID + "/" + ID + "REPORT.TXT", "r");
	    FileChannel inChannel = aFile.getChannel();
	    ByteBuffer buffer = ByteBuffer.allocate(1024);
	    
	    String strFields ="";
	    int nConcepts = 0;
	    int maxConcepts = 0;
	    
	    while(inChannel.read(buffer) > 0)
	    {
	    	
	        buffer.flip();
	        for (int i = 0; i < buffer.limit(); i++)
	        {
	        	
	        	byte valor = buffer.get(); 
	        	System.out.print((char) valor);
	        	if(valor != '\n'){		        		
	        		strFields+=(char)valor;  		
	        	}else{	        			
        					    	            	    	            
    	            String arrayValues[] = strFields.split("<<delimiter>>");
    	            if(arrayValues.length>51){
    	            	//51
		        		nConcepts = 0;
	    	            
    	            	for(int cellnum = 52; cellnum < arrayValues.length; cellnum++){   	                
	    	                nConcepts++;
	    	            }  		
	    	            if(nConcepts>maxConcepts){
	    	            	maxConcepts = nConcepts;
	    	            }
    	            }
    	            
	        		strFields="";
	        	}
	        }
	        buffer.clear(); // do something with the data and clear/compact it.
	    }
	    inChannel.close();
	    aFile.close();
	    
	    return maxConcepts;
	}
*/

}
