package com.interfactura.firmalocal.persistence;

public class UtilManager {
	
	
	public static String in(String ids){
		if(ids==null || ids.equals(""))
		{
			String consulta = " WHERE x.id = 0 ";
			return consulta;
		}
		String[] idsList = ids.split(",");
		String consulta = " WHERE ( ";
		for(int i = 0; i < idsList.length; i++){
			consulta += " x.id = "+idsList[i]; 
			if(i < idsList.length-1) 
			{  consulta += " OR";  } 
		}
		consulta += ")" ;
		return consulta;
	}
	
	public static String inListar(String ids){
		if(ids==null || ids.equals(""))
		{
			String consulta = " WHERE x.fiscalEntity.id = 0 ";
			return consulta;
		}
		String[] idsList = ids.split(",");
		String consulta = " WHERE ( ";
		for(int i = 0; i < idsList.length; i++){
			consulta += " x.fiscalEntity.id = "+idsList[i]; 
			if(i < idsList.length-1) 
			{  consulta += " OR";  } 
		}
		consulta += ")" ;
		return consulta;
	}
	
	public static String in(String campo, String ids){
		if(ids==null || ids.equals(""))
		{
			String consulta = " WHERE "+campo+" = 0 ";
			return consulta;
		}
		String[] idsList = ids.split(",");
		String consulta = " WHERE ( ";
		for(int i = 0; i < idsList.length; i++){
			consulta += " " +campo+" = "+idsList[i]; 
			if(i < idsList.length-1) 
			{  consulta += " OR";  } 
		}
		consulta += ")" ;
		return consulta;
	}
	
}
