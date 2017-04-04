package com.interfactura.firmalocal.controllers;

public class Filter {
	public static int NOTHING = 0;
	public static int WOLE_WORD = 1;
	public static int CASE_SENSITIVE = 2;
	
	private String column;
	private String pattern;
	int options;
	
	public Filter(String pattern, int options) {
		this.pattern = pattern;
		this.options = options;
	}
	
	public Filter(String pattern, String column) {
		this.column = column;
		this.pattern = pattern;
		this.options = this.NOTHING;
	}

	public Filter(String pattern) {
		this(pattern, Filter.NOTHING);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) {
		this.options = options;
	}
	
	public void setColumn(String column) {
		this.column = column;
	}
	
	public String getColumn() {
		return column;
	}
}
