package com.interfactura.firmalocal.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.ssl.PKCS8Key;

import sun.misc.BASE64Encoder;

public class GenerarSello {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			String cadenaOriginal = "||2.0|AB|32|2010-02-18T12:30:03|434|2006|" +
			"ingreso|EFECTIVO|3000.00|45029.85|02934INFOSOFT|Infosoft & Xgress|arenas|" +
			"98|Coyoacan|DF|México|54832|4503495WXGF4|México|2|Impresoras|1500.00|" +
			"3000.00|2|PC|2000.00|4000.00|IVA|15.00|2900.85|2900.85||";
			
			// Saca la clave privada
			String basePath = "Cer_Sellos" ;
			String FS = File.separator;
			String pwd = "a0123456789";
			
			FileInputStream fis = null;
			
			RSAPrivateKey pk = null;
			RSAPublicKey puk = null;
			File f = new File(basePath + FS + "aaa010101aaa_csd_05.key");
			fis = new FileInputStream(f);
			
			PKCS8Key pk8 = new PKCS8Key(fis, pwd.toCharArray());
			System.out.println("DSA? " + pk8.isDSA());
			System.out.println("RSA? " + pk8.isRSA());
			System.out.println("Size (Bytes) " + pk8.getKeySize());
			System.out.println("Size (Bits) " + pk8.getKeySize() * 8);
			System.out.println("Transformation " + pk8.getTransformation());
			
			int keySize = 1;
			
			if (pk8.isRSA())
			{
				pk = (RSAPrivateKey) pk8.getPrivateKey();
				keySize = pk.getPrivateExponent().toByteArray().length;
				System.out.println("RSA Key Size (bytes) " + keySize );
				System.out.println("RSA Key Size (bits) " + keySize * 8);
				System.out.println("RSA Key Size (Base64) " + (keySize * 4.0 / 3.0));
			}
			
			Signature firma = Signature.getInstance("MD5withRSA"); 
			firma.initSign(pk); 

			firma.update(cadenaOriginal.getBytes("UTF-8")); 

			byte[] cadenaFirmada = firma.sign(); 

			BASE64Encoder b64 = new BASE64Encoder(); 
			String selloDigital = b64.encode(cadenaFirmada); 
			System.out.println("Sello Digital: " + selloDigital);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
