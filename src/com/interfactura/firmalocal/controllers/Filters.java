package com.interfactura.firmalocal.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class Filters<E> extends ArrayList<E> implements List<E>,
		RandomAccess, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = 7487504435996789898L;
}
