//******************************************************************************
// File     : RPLStack.java
// Author   : jpl
// Created  : 22/09/11 15:43
// Modified : 22/07/16 11:39
//******************************************************************************
package fr.ligorax.polka;

import java.util.Iterator;
import java.util.Stack;
import java.io.Serializable;

/**
 Class to manage all the objects stored in the stack.
 @author jpliguori
 */
public class RPLStack extends Stack implements Serializable
{
//******************************************************************************
// depth()
//******************************************************************************
	/**
	 Method to get the number of objects stored in the stack.
	 @return The number of objects as an <code>int</code>.
	 */
	public int depth()
	{
		return this.size();
	}

//******************************************************************************
// display()
//******************************************************************************
	/**
	 Method to display all the objects stored in the stack.<br>
	 The display is sent to the <code>System.out</code> channel.
	 */
	public void display()
	{
		Iterator it = this.iterator();

		int i = 0;
		while (it.hasNext())
		{
			i++;
			String iValue = it.next().toString();
			System.out.println(i + ": " + iValue);
		}
	}

}
