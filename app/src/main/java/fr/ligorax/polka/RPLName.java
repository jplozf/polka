//******************************************************************************
// File     : RPLName.java
// Author   : jpl
// Created  : 28/09/11 16:46
// Modified : 23/07/16 18:06
//******************************************************************************
package fr.ligorax.polka;

import java.io.Serializable;

public class RPLName implements Serializable
{
	String name;
	Object obj;

//******************************************************************************
// RPLName()
//******************************************************************************
	public RPLName(String name)
	{
		this.name = name;
	}

	public RPLName(String name, Object obj)
	{
		this.name = name;
		this.obj = obj;
		/*
		 if (obj.getClass().getName().equals("replicant.RPLProgram"))
		 {
         DefaultMutableTreeNode pgm = new DefaultMutableTreeNode(name);
         ReplicantApp.ndePrograms.add(pgm);
         ReplicantView.jtrCatalog.setSelectionPath( new TreePath(pgm.getPath()) );
		 }
		 if (obj.getClass().getName().equals("java.lang.Double"))
		 {
         DefaultMutableTreeNode dbl = new DefaultMutableTreeNode(name);
         ReplicantApp.ndeDoubles.add(dbl);
         ReplicantView.jtrCatalog.setSelectionPath( new TreePath(dbl.getPath()) );
		 }
		 */
	}

//******************************************************************************
// set()
//******************************************************************************
	public void set(Object obj)
	{
		this.obj = obj;
	}

//******************************************************************************
// getName()
//******************************************************************************
	public String getName()
	{
		return this.name;
	}

//******************************************************************************
// toString()
//******************************************************************************
	@Override
	public String toString()
	{
		return "'" + this.name + "'";
		// return this.name;
	}

}
