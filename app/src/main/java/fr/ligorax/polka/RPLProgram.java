//******************************************************************************
// File     : RPLProgram.java
// Author   : jpl
// Created  : 28/09/11 15:13
// Modified : 22/07/16 12:11
//******************************************************************************
package fr.ligorax.polka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class RPLProgram implements Serializable
{
	PolkaActivity act;
	int pc;
	List step;
	String name;
	ArrayList<Integer> label = new ArrayList<Integer>();

//******************************************************************************
// RPLProgram()
//******************************************************************************
	public RPLProgram()
	{

		this.pc = 0;
		this.name = "tmp_" + UUID.randomUUID().toString();
		this.step = new ArrayList();
	}

	public RPLProgram(PolkaActivity act, String name)
	{
		this.act = act;
		this.pc = 0;
		this.name = name;
	}

//******************************************************************************
// getName()
//******************************************************************************
	public String getName()
	{
		return this.name;
	}

//******************************************************************************
// setName()
//******************************************************************************
	public void setName(String name)
	{
		this.name = name;
	}

//******************************************************************************
// toString()
//******************************************************************************
	@Override
	public String toString()
	{
		String pgm = "{ ";
		Iterator it = step.iterator();

		while (it.hasNext())
		{
			pgm = pgm + it.next().toString() + " ";
		}
		pgm = pgm + "}";

		System.out.println("PGM : " + pgm);
		return pgm;
	}

//******************************************************************************
// Run()
//******************************************************************************
	public PolkaActivity.RPLError run()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("RUNNING");
		this.Compile();
		Iterator it = this.step.iterator();
		String cmd;

		this.pc = 0;
		PolkaActivity.PgmRunning = true;
		while (it.hasNext() && PolkaActivity.PgmRunning == true)
		{
			cmd = it.next().toString();
			rc = act.whatToDoWith(cmd);
			System.out.println(pc + " : " + cmd);
			this.pc++;
			act.displayStack(rc.toString());
		}
		PolkaActivity.PgmRunning = false;
		return rc;
	}

//******************************************************************************
// Compile()
//******************************************************************************
	public PolkaActivity.RPLError Compile()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("RUNNING");
		Iterator it = this.step.iterator();
		String cmd;

		this.pc = 0;
		while (it.hasNext())
		{
			cmd = it.next().toString();
			if (cmd.equals("WHILE") ||
				cmd.equals("DO") ||
				cmd.equals("IF"))
			{
				compileStruct(pc);
			}
			System.out.println(pc + " : " + cmd);
			this.pc++;
			// ReplicantView.Display(rc.toString());
		}

		return rc;
	}

	/*******************************************************************************
	 WHILE cond REPEAT instr END
	 *******************************************************************************
	 cond
	 ?branch end
	 instr
	 branch cond
	 end

	 *******************************************************************************
	 IF cond THEN instr1 ELSE instr2 END
	 *******************************************************************************
	 cond
	 ?branch instr2
	 instr1
	 branch end
	 instr2
	 end

	 *******************************************************************************
	 IF cond THEN instr END
	 *******************************************************************************
	 cond
	 ?branch end
	 instr
	 end

	 *******************************************************************************
	 DO instr UNTIL cond END
	 *******************************************************************************
	 instr
	 cond
	 ?branch instr
	 end

	 *******************************************************************************/

//******************************************************************************
// compileStruct()
//******************************************************************************
	private void compileStruct(int here)
	{
		String cmd = this.step.get(here).toString();
		System.out.println("STEP = " + here + " CMD = " + cmd);
		if (cmd.equals("WHILE"))
		{
			int i = here;
			while (this.step.get(i).equals("REPEAT"))
			{
				this.step.set(i, this.step.get(++i));
			}
			this.step.set(i, "?BRANCH");

		}
	}


}
