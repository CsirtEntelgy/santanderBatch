package com.interfactura.firmalocal.ondemand.util;

import java.io.File;
import java.util.List;

public class FilesOndemand {
	private List<File> files;
	private String [] valuesCfd;
	private String strPath;
	public List<File> getFiles() {
		return files;
	}
	public void setFiles(List<File> files) {
		this.files = files;
	}
	public String[] getValuesCfd() {
		return valuesCfd;
	}
	public void setValuesCfd(String[] valuesCfd) {
		this.valuesCfd = valuesCfd;
	}
	public String getStrPath() {
		return strPath;
	}
	public void setStrPath(String strPath) {
		this.strPath = strPath;
	}
	
	
}
