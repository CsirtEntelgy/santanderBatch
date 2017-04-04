package com.interfactura.firmalocal.crypto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.ssl.PKCS8Key;



public class App 
{
	final static String FS = File.separator;
	
    public static void main( String[] args ) throws Exception
    {
		String basePath = "Cer_Sellos" ;
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
		
		if (pk8.isRSA()){
			pk = (RSAPrivateKey) pk8.getPrivateKey();
			//puk = (RSAPublicKey) pk8.getPublicKey();
			
			//System.out.println("Private Key " + pk);
			//System.out.println("Public Key " + puk);
			keySize = pk.getPrivateExponent().toByteArray().length;
			System.out.println("RSA Key Size (bytes) " + keySize );
			System.out.println("RSA Key Size (bits) " + keySize * 8);
			System.out.println("RSA Key Size (Base64) " + (keySize * 4.0 / 3.0));
		}
		
		
		File c = new File(basePath + FS + "aaa010101aaa_csd_05.cer");
		//c = new File(basePath + FS + "EmpresaPrueba.cer");
		
		 fis = new FileInputStream(c);
		 BufferedInputStream bis = new BufferedInputStream(fis);

		 CertificateFactory cf = CertificateFactory.getInstance("X.509");

		 while (bis.available() > 0) {
		    Certificate cert = cf.generateCertificate(bis);
		    //System.out.println(cert.toString());
		    if (cert instanceof X509Certificate){
		    	X509Certificate cert2 = (X509Certificate) cert;
		    	if (cert.getPublicKey() instanceof RSAPublicKey){
		    		puk = (RSAPublicKey)cert2.getPublicKey();
		    	}

		    	Map <String,String> oidMap = new HashMap<String,String>();
		    	oidMap.put("2.5.4.5", "serialNumber");
		    	oidMap.put("2.5.4.41", "name");
		    	oidMap.put("2.5.4.17", "postalCode");
		    	oidMap.put("2.5.4.45", "uniqueIdentifier");
		    	oidMap.put("1.2.840.113549.1.9.2", "unstructuredName");
		    	oidMap.put("1.2.840.113549.1.9.1", "emailAddress");

		    	X500Principal subject = cert2.getSubjectX500Principal();
		    	X500Principal issuer = cert2.getIssuerX500Principal();
		    	System.out.println("subject 1779 : "+subject.getName(X500Principal.RFC1779,oidMap));
		    	System.out.println("issuer 1779  : "+issuer.getName(X500Principal.RFC1779,oidMap));
		    	
		    	String dn = subject.getName(X500Principal.RFC1779,oidMap);
		    	
		    	//dn = dn.replace("Empresa", "Empresa\\=");
		    	System.out.println("DN      --> " + dn);

		    	Map<String, String> dnames = getDNMembers(dn);

		    	for(Map.Entry<String, String> e : dnames.entrySet())
		    	    System.out.println("|"+e.getKey()+"|"+e.getValue()+"|");
		    	
		    	BigInteger sn = cert2.getSerialNumber();
		    	System.out.println("SN   --> " + sn);
		    	System.out.println("SNHEX--> " + sn.toString(16));
		    	
		    	String newSN = serialNumberIES(sn);
		    	System.out.println("SNIES--> "+newSN);
		    	
		    	//System.exit(0);
		    	
		    	
		    	SecureRandom random = new SecureRandom();
		    	// 100 bytes is ok for 1024 bits keys
		    	byte bytes[] = new byte[100];
			        random.nextBytes(bytes);
		        
		    	Cipher c1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		    	c1.init(Cipher.ENCRYPT_MODE, pk);
		    	byte[] encryptedBytes = c1.doFinal(bytes);
		        
		        Cipher c2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		    	c2.init(Cipher.DECRYPT_MODE, puk);
		    	byte[] decryptedBytes = c2.doFinal(encryptedBytes);
		    	
		    	boolean iguales = Arrays.equals(bytes, decryptedBytes);
		    	System.out.println("iguales? "+ iguales);
		    	System.out.println(Arrays.toString(bytes));
		    	System.out.println(Arrays.toString(encryptedBytes));
		    	System.out.println(Arrays.toString(decryptedBytes));
		    }
		 }
		

		
		System.out.println();
		
		System.gc();
		System.exit(0);
    }

	private static String serialNumberIES(BigInteger sn) {
		return sn.toString(16).replaceAll("(.{1})(.{1})", "$2");
	}

	private static Map<String, String> getDNMembers(String dn) {
		Map <String,String> dnames = new HashMap<String,String>();

		dn = dn.replace("\\=", "~INEQUALS~");
		dn = dn.replace("\\\"", "~INQUOTE~");
		dn = dn.replace("\"", "");
		dn = dn.replace("~INQUOTE~", "\"");
		String[] list = dn.split("=");

		boolean isKey = false;
		String tempKey = null;

		for (String m: list){
			String s = m.replace("~INEQUALS~", "=")
			.replaceAll("(.*)(,) (.*)", "$1----$3");

			if (s.contains("----")){
				String[] commaList = s.split("----");
				if (2 ==commaList.length){
					dnames.put(tempKey, commaList[0]);
					tempKey = commaList[1];
				}
			} else{
				if (!isKey){
					tempKey = s;
					isKey = true;
				} else {
					dnames.put(tempKey, s);
					isKey = false;
				}
			}
		}
		return dnames;
	}
}
