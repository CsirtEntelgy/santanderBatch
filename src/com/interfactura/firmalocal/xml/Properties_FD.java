package com.interfactura.firmalocal.xml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration("classpath:configuration_FD.properties")
public class Properties_FD {

	@Value("${url.webservice.timbradoUnicoDivisas}")
	private String serviceUnicoDivisas;

	public String getServiceUnicoDivisas() {
		return serviceUnicoDivisas;
	}

	public void setServiceUnicoDivisas(String serviceUnicoDivisas) {
		this.serviceUnicoDivisas = serviceUnicoDivisas;
	}	
}
