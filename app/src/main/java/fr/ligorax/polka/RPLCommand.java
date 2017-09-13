//******************************************************************************
// File     : RPLCommand.java
// Author   : jpl
// Created  : 22/09/11 15:43
// Modified : 30/07/16 21:56
//******************************************************************************
package fr.ligorax.polka;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

// LOG LN E EE ACOS ASIN ATAN ATAN2 ROT TONE FACT RAND DEG RAD GRAD INV
// DMS DDEC D2R D2G R2D R2G G2R G2D IP FP CEIL FLOOR TIME DATE FIX ENG SCI STD
// MAN HELP

public class RPLCommand
{
	PolkaActivity act;
	String cmd;
	String alias;
	int context;
	Hashtable tableAlias = new Hashtable();

	//******************************************************************************
	// RPLCommand()
	//******************************************************************************
	public RPLCommand(PolkaActivity act, String cmd, int context)
	{
		this.act = act;
		this.cmd = cmd;
		this.context = context;
		initAlias();
		// find the alias if any
		this.alias = (tableAlias.get(cmd.toUpperCase()) != null ? tableAlias.get(cmd.toUpperCase()).toString() : cmd);
	}

	//******************************************************************************
	// xeq()
	//******************************************************************************
	public PolkaActivity.RPLError xeq()
	{
		String x = this.alias;
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("SYNTAX_ERROR");
		String MethodToCall = "do" + x.substring(0, 1).toUpperCase() + x.substring(1).toLowerCase();

		try
		{
			System.out.println("Trying to execute " + MethodToCall);
			Method method = getClass().getDeclaredMethod(MethodToCall);
			rc = (PolkaActivity.RPLError) method.invoke(this);
		}
		catch (Exception e)
		{
			System.out.println("Not found... Is it a name ?");
			rc = xeqName(x);
		}

		if (rc != PolkaActivity.RPLError.value("OK"))
		{
			TextView txtInput = (TextView) act.findViewById(R.id.txtInput);
			txtInput.setText(this.cmd);
			// txtInput.selectAll();
			act.beep();
         System.out.println("Beep Error");
		}
		return rc;
	}

	//******************************************************************************
	// initAlias()
	//******************************************************************************
	private void initAlias()
	{
		tableAlias.put("QUIT", "EXIT");
		tableAlias.put("+", "PLUS");
		tableAlias.put("-", "MINUS");
		tableAlias.put("*", "TIMES");
		tableAlias.put("/", "DIVIDE");
		tableAlias.put("^", "POW");
		tableAlias.put("**", "POW");
		tableAlias.put("²", "SQ");
		tableAlias.put("{", "BEGP");
		tableAlias.put("}", "ENDP");
		tableAlias.put("!", "FACT");
		tableAlias.put("INT", "IP");
		tableAlias.put("FRAC", "FP");
		tableAlias.put("LAT", "LATITUDE");
		tableAlias.put("LON", "LONGITUDE");
	}

	//******************************************************************************
	// doPlus()
	//******************************************************************************
	private PolkaActivity.RPLError doPlus()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			double d2 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			double d1 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			PolkaActivity.stack.push(d1 + d2);
			// return PolkaActivity.ERR_OK;
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doMinus()
	//******************************************************************************
	private PolkaActivity.RPLError doMinus()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			double d2 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			double d1 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			PolkaActivity.stack.push(d1 - d2);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doTimes()
	//******************************************************************************
	private PolkaActivity.RPLError doTimes()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			double d2 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			double d1 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			PolkaActivity.stack.push(d1 * d2);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doDivide()
	//******************************************************************************
	private PolkaActivity.RPLError doDivide()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			double d2 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			double d1 = Double.parseDouble(PolkaActivity.stack.pop().toString());
			if (d2 != 0.0)
			{
				PolkaActivity.stack.push(d1 / d2);
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(d1);
				PolkaActivity.stack.push(d2);
				return PolkaActivity.RPLError.value("INFINITE_RESULT");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doExit()
	//******************************************************************************
	private PolkaActivity.RPLError doExit()
	{
		act.moveTaskToBack(true);
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doDrop()
	//******************************************************************************
	private PolkaActivity.RPLError doDrop()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object o = PolkaActivity.stack.pop();
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doDup()
	//******************************************************************************
	private PolkaActivity.RPLError doDup()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object o = PolkaActivity.stack.pop();
			PolkaActivity.stack.push(o);
			PolkaActivity.stack.push(o);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doSwap()
	//******************************************************************************
	private PolkaActivity.RPLError doSwap()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			Object o1 = PolkaActivity.stack.pop();
			Object o2 = PolkaActivity.stack.pop();
			PolkaActivity.stack.push(o1);
			PolkaActivity.stack.push(o2);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doRot()
	//******************************************************************************
	private PolkaActivity.RPLError doRot()
	{
		if (PolkaActivity.stack.depth() >= 3)
		{
			Object o3 = PolkaActivity.stack.pop();
			Object o2 = PolkaActivity.stack.pop();
			Object o1 = PolkaActivity.stack.pop();
			PolkaActivity.stack.push(o3);
			PolkaActivity.stack.push(o1);
			PolkaActivity.stack.push(o2);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doClear()
	//******************************************************************************
	private PolkaActivity.RPLError doClear()
	{
		PolkaActivity.stack.clear();
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doDepth()
	//******************************************************************************
	private PolkaActivity.RPLError doDepth()
	{
		Integer d = PolkaActivity.stack.depth();
		PolkaActivity.stack.push(d.doubleValue());
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doSin()
	//******************************************************************************
	private PolkaActivity.RPLError doSin()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				switch (PolkaActivity.RPLAngularMode)
				{
					case 'R':
						PolkaActivity.stack.push(Math.sin(d));
						break;
					case 'D':
						PolkaActivity.stack.push(Math.sin(D2R(d)));
						break;
					case 'G':
						PolkaActivity.stack.push(Math.sin(G2R(d)));
						break;
				}
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doCos()
	//******************************************************************************
	private PolkaActivity.RPLError doCos()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			switch (PolkaActivity.RPLAngularMode)
			{
				case 'R':
					PolkaActivity.stack.push(Math.cos(d));
					break;
				case 'D':
					PolkaActivity.stack.push(Math.cos(D2R(d)));
					break;
				case 'G':
					PolkaActivity.stack.push(Math.cos(G2R(d)));
					break;
			}
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doTan()
	//******************************************************************************
	private PolkaActivity.RPLError doTan()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			switch (PolkaActivity.RPLAngularMode)
			{
				case 'R':
					PolkaActivity.stack.push(Math.tan(d));
					break;
				case 'D':
					PolkaActivity.stack.push(Math.tan(D2R(d)));
					break;
				case 'G':
					PolkaActivity.stack.push(Math.tan(G2R(d)));
					break;
			}
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doAsin()
	//******************************************************************************
	private PolkaActivity.RPLError doAsin()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			switch (PolkaActivity.RPLAngularMode)
			{
				case 'R':
					PolkaActivity.stack.push(Math.asin(d));
					break;
				case 'D':
					PolkaActivity.stack.push(R2D(Math.asin(d)));
					break;
				case 'G':
					PolkaActivity.stack.push(R2G(Math.asin(d)));
					break;
			}
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doAcos()
	//******************************************************************************
	private PolkaActivity.RPLError doAcos()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			switch (PolkaActivity.RPLAngularMode)
			{
				case 'R':
					PolkaActivity.stack.push(Math.acos(d));
					break;
				case 'D':
					PolkaActivity.stack.push(R2D(Math.acos(d)));
					break;
				case 'G':
					PolkaActivity.stack.push(R2G(Math.acos(d)));
					break;
			}
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doAtan()
	//******************************************************************************
	private PolkaActivity.RPLError doAtan()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			switch (PolkaActivity.RPLAngularMode)
			{
				case 'R':
					PolkaActivity.stack.push(Math.atan(d));
					break;
				case 'D':
					PolkaActivity.stack.push(R2D(Math.atan(d)));
					break;
				case 'G':
					PolkaActivity.stack.push(R2G(Math.atan(d)));
					break;
			}
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doPi()
	//******************************************************************************
	private PolkaActivity.RPLError doPi()
	{
		PolkaActivity.stack.push(Math.PI);
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doE()
	//******************************************************************************
	private PolkaActivity.RPLError doE()
	{
		PolkaActivity.stack.push(Math.E);
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doChs()
	//******************************************************************************
	private PolkaActivity.RPLError doChs()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			PolkaActivity.stack.push(d * -1.0);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doAbs()
	//******************************************************************************
	private PolkaActivity.RPLError doAbs()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			if (d >= 0.0)
				PolkaActivity.stack.push(d);
			else
				PolkaActivity.stack.push(d * -1.0);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doRand()
	//******************************************************************************
	private PolkaActivity.RPLError doRand()
	{
		PolkaActivity.stack.push(Math.random());
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doDeg()
	//******************************************************************************
	private PolkaActivity.RPLError doDeg()
	{
		PolkaActivity.RPLAngularMode = 'D';
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doGrad()
	//******************************************************************************
	private PolkaActivity.RPLError doGrad()
	{
		PolkaActivity.RPLAngularMode = 'G';
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doRad()
	//******************************************************************************
	private PolkaActivity.RPLError doRad()
	{
		PolkaActivity.RPLAngularMode = 'R';
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// D2R()
	//******************************************************************************
	private double D2R(double d)
	{
		return (d * Math.PI / 180.0);
	}

	//******************************************************************************
	// R2D()
	//******************************************************************************
	private double R2D(double r)
	{
		return (r * 180.0 / Math.PI);
	}

	//******************************************************************************
	// G2R()
	//******************************************************************************
	private double G2R(double g)
	{
		return (g * Math.PI / 200.0);
	}

	//******************************************************************************
	// R2G()
	//******************************************************************************
	private double R2G(double r)
	{
		return (r * 200.0 / Math.PI);
	}

	//******************************************************************************
	// doInv()
	//******************************************************************************
	private PolkaActivity.RPLError doInv()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			if (d != 0.0)
			{
				PolkaActivity.stack.push(1.0 / d);
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(d);
				return PolkaActivity.RPLError.value("INFINITE_RESULT");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

   //******************************************************************************
   // doPow()
   //******************************************************************************
   private PolkaActivity.RPLError doPow()
   {
      if (PolkaActivity.stack.depth() >= 2)
      {
         double d1 = Double.parseDouble(PolkaActivity.stack.pop().toString());
         double d2 = Double.parseDouble(PolkaActivity.stack.pop().toString());
            PolkaActivity.stack.push(Math.pow(d1, d2));
            return PolkaActivity.RPLError.value("OK");
      }
      else
      {
         return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

	//******************************************************************************
	// doSq()
	//******************************************************************************
	private PolkaActivity.RPLError doSq()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			PolkaActivity.stack.push(d * d);
			return PolkaActivity.RPLError.value("OK");
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doSqrt()
	//******************************************************************************
	private PolkaActivity.RPLError doSqrt()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			double d = Double.parseDouble(PolkaActivity.stack.pop().toString());
			if (d >= 0.0)
			{
				PolkaActivity.stack.push(Math.sqrt(d));
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(d);
				return PolkaActivity.RPLError.value("COMPLEX_NUMBER");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}
	// SQ, SQRT, POW, XROOT, LOG, ALOG, LN, EXP, INV,

	//******************************************************************************
	// doBegp()
	//******************************************************************************
	private PolkaActivity.RPLError doBegp()
	{
		PolkaActivity.RPLRunMode = 'P';

		PolkaActivity.RPLPrograms.add(new RPLProgram());
		RPLProgram p = (RPLProgram) PolkaActivity.RPLPrograms.get(PolkaActivity.RPLPrograms.size() - 1);
		System.out.println(p.getName());
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doEndp()
	//******************************************************************************
	private PolkaActivity.RPLError doEndp()
	{
		PolkaActivity.RPLRunMode = 'R';
		RPLProgram p = (RPLProgram) PolkaActivity.RPLPrograms.get(PolkaActivity.RPLPrograms.size() - 1);
		PolkaActivity.stack.push(p);
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doSto()
	//******************************************************************************
	private PolkaActivity.RPLError doSto()
	{
		if (PolkaActivity.stack.depth() >= 2)
		{
			Object o1 = PolkaActivity.stack.pop();
			System.out.println("Obj1 : " + o1.getClass().getName());
			if (!o1.getClass().getName().equals("fr.ligorax.polka.RPLName"))
			{
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
			else
			{
				Object o2 = PolkaActivity.stack.pop();
				System.out.println("Obj2 : " + o2.getClass().getName());
				RPLName n = (RPLName) o1;
				//******************************************************************
				// TODO test if not a reserved name
				//******************************************************************
				// test if this name is not already used
				for (Iterator<RPLName> itr = PolkaActivity.RPLNames.iterator(); itr.hasNext();)
				{
					RPLName t = itr.next();
					if (n.getName().equals(t.getName()))
					{
						// yes, then drop it
						itr.remove();
						break;
					}
				}
				PolkaActivity.RPLNames.add(new RPLName(n.getName(), o2));
				return PolkaActivity.RPLError.value("OK");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doRcl()
	//******************************************************************************
	private PolkaActivity.RPLError doRcl()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("UNDEFINED_NAME");
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object o1 = PolkaActivity.stack.pop();
			System.out.println("Obj1 : " + o1.getClass().getName());
			if (!o1.getClass().getName().equals("fr.ligorax.polka.RPLName"))
			{
				PolkaActivity.stack.push(o1);
				rc = PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
			else
			{
				// loop to find this name (if any)
				RPLName o = (RPLName) o1;
				for (Iterator<RPLName> itr = PolkaActivity.RPLNames.iterator(); itr.hasNext();)
				{
					RPLName n = itr.next();
					if (o.getName().equals(n.getName()))
					{
						// yes, then push it
						PolkaActivity.stack.push(n.obj);
						rc = PolkaActivity.RPLError.value("OK");
					}
				}
				if (rc != PolkaActivity.RPLError.value("OK"))
				{
					PolkaActivity.stack.push(o1);
				}
			}
			return rc;
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// xeqName()
	//******************************************************************************
	private PolkaActivity.RPLError xeqName(String x)
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("SYNTAX_ERROR");
		// loop to find this name (if any)
		for (Iterator<RPLName> itr = PolkaActivity.RPLNames.iterator(); itr.hasNext();)
		{
			RPLName n = itr.next();
			if (x.equals(n.getName()))
			{            
				if (!n.obj.getClass().getName().equals("fr.ligorax.polka.RPLProgram"))
				{  // yes, then push it
					PolkaActivity.stack.push(n.obj);
					rc = PolkaActivity.RPLError.value("OK");
				}
				else
				{ // or run it
					RPLProgram pgm = (RPLProgram)n.obj;
					pgm.act = this.act;
					rc = pgm.run();
				}
				break;
			}
		}
		return rc;
	}

	//******************************************************************************
	// doArg()
	//******************************************************************************
	private PolkaActivity.RPLError doArg()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("fr.ligorax.polka.RPLComplex"))
			{
				RPLComplex c = (RPLComplex) p;
				switch (PolkaActivity.RPLAngularMode)
				{
					case 'R':
						PolkaActivity.stack.push(c.arg());
						break;
					case 'D':
						PolkaActivity.stack.push(R2D(c.arg()));
						break;
					case 'G':
						PolkaActivity.stack.push(R2G(c.arg()));
						break;
				}
				return PolkaActivity.RPLError.value("OK");
			}
			else if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				if (d >= 0.0)
				{
					PolkaActivity.stack.push(0.0);
				}
				else
				{
					switch (PolkaActivity.RPLAngularMode)
					{
						case 'R':
							PolkaActivity.stack.push(Math.PI);
							break;
						case 'D':
							PolkaActivity.stack.push(180.0);
							break;
						case 'G':
							PolkaActivity.stack.push(200.0);
							break;
					}
				}
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doIp()
	//******************************************************************************
	private PolkaActivity.RPLError doIp()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				PolkaActivity.stack.push(new Double((long) d));
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doFp()
	//******************************************************************************
	private PolkaActivity.RPLError doFp()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				PolkaActivity.stack.push(d - (long) d);
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doCeil()
	//******************************************************************************
	private PolkaActivity.RPLError doCeil()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				PolkaActivity.stack.push(Math.ceil(d));
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doFloor()
	//******************************************************************************
	private PolkaActivity.RPLError doFloor()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				PolkaActivity.stack.push(Math.floor(d));
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doTime()
	//******************************************************************************
	private PolkaActivity.RPLError doTime()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("H.mmssSSS");
		String t = sdf.format(cal.getTime());

		PolkaActivity.stack.push(Double.parseDouble(t));
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doDate()
	//******************************************************************************
	private PolkaActivity.RPLError doDate()
	{
		Calendar cal = Calendar.getInstance();
		// SimpleDateFormat sdf = new SimpleDateFormat("MM.ddyyyy");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMdd");
		String d = sdf.format(cal.getTime());

		PolkaActivity.stack.push(Double.parseDouble(d));
		return PolkaActivity.RPLError.value("OK");
	}

	//******************************************************************************
	// doFix()
	//******************************************************************************
	private PolkaActivity.RPLError doFix()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				int f = (int)(d + 0.5);
				if (f < 0)
					f = 0;
				if (f > 15)
					f = 15;
				PolkaActivity.RPLFixNumber = f;
				PolkaActivity.RPLDisplayMode = 'F';
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doEng()
	//******************************************************************************
	private PolkaActivity.RPLError doEng()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				int f = (int)(d + 0.5);
				if (f < 0)
					f = 0;
				if (f > 15)
					f = 15;
				PolkaActivity.RPLFixNumber = f;
				PolkaActivity.RPLDisplayMode = 'E';
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doSci()
	//******************************************************************************
	private PolkaActivity.RPLError doSci()
	{
		if (PolkaActivity.stack.depth() >= 1)
		{
			Object p = PolkaActivity.stack.pop();
			if (p.getClass().getName().equals("java.lang.Double"))
			{
				double d = Double.parseDouble(p.toString());
				int f = (int)(d + 0.5);
				if (f < 0)
					f = 0;
				if (f > 15)
					f = 15;
				PolkaActivity.RPLFixNumber = f;
				PolkaActivity.RPLDisplayMode = 'S';
				return PolkaActivity.RPLError.value("OK");
			}
			else
			{
				PolkaActivity.stack.push(p);
				return PolkaActivity.RPLError.value("BAD_ARGUMENT_TYPE");
			}
		}
		else
		{
			return PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
	}

	//******************************************************************************
	// doStd()
	//******************************************************************************
	private PolkaActivity.RPLError doStd()
	{
		PolkaActivity.RPLFixNumber = 0;
		PolkaActivity.RPLDisplayMode = 'D';
		return PolkaActivity.RPLError.value("OK");
	}

	//***************************************************************************
	// doVersion()
	//***************************************************************************
	private PolkaActivity.RPLError doVersion()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		try
		{
			PackageInfo pInfo = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
			Integer v = pInfo.versionCode;
			PolkaActivity.stack.push(v.doubleValue());
		}
		catch (PackageManager.NameNotFoundException e)
		{
			PolkaActivity.stack.push(0.404);
		}
		return rc;
	}

	//***************************************************************************
	// doLatitude()
	//***************************************************************************
	private PolkaActivity.RPLError doLatitude()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		PolkaActivity.LatLon latlon = act.getLocation();
		PolkaActivity.stack.push(latlon.Latitude);
		return rc;
	}

	//***************************************************************************
	// doLongitude()
	//***************************************************************************
	private PolkaActivity.RPLError doLongitude()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		PolkaActivity.LatLon latlon = act.getLocation();
		PolkaActivity.stack.push(latlon.Longitude);
		return rc;
	}

	//***************************************************************************
	// doLatlon()
	//***************************************************************************
	private PolkaActivity.RPLError doLatlon()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		PolkaActivity.LatLon latlon = act.getLocation();
		PolkaActivity.stack.push(latlon.Latitude);
		PolkaActivity.stack.push(latlon.Longitude);
		return rc;
	}

	//***************************************************************************
	// doTone()
	//***************************************************************************
	private PolkaActivity.RPLError doTone()
	{
		Double x, y;
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		if (!act.checkArgs(2))
		{
			rc = PolkaActivity.RPLError.value("TOO_FEW_ARGUMENTS");
		}
		else
		{
			x = (Double)PolkaActivity.stack.pop();
			y = (Double)PolkaActivity.stack.pop();
			if (y < 1.0)
				y = 1.0;
			PlaySound sound = new PlaySound(x.intValue() , y, true);
		}
		return rc;
	}

	//***************************************************************************
	// doBeep()
	//***************************************************************************
	private PolkaActivity.RPLError doBeep()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");
		PlaySound sound = new PlaySound(1, 440, true);
		return rc;
	}

	//***************************************************************************
	// doEval()
	//***************************************************************************
	private PolkaActivity.RPLError doEval()
	{
		PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("OK");

		return rc;
	}
}
