//******************************************************************************
// File     : RPLCommand.java
// Author   : jpl
// Created  : 22/09/11 15:43
// Modified : 07/10/11 17:22
//******************************************************************************
package replicant;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class RPLCommand
{
   String cmd;
   String alias;
   Hashtable tableAlias = new Hashtable();

//******************************************************************************
// RPLCommand()
//******************************************************************************
   public RPLCommand(String cmd)
   {
      this.cmd = cmd;
      initAlias();
      // find the alias if any
      this.alias = (tableAlias.get(cmd.toUpperCase()) != null ? tableAlias.get(cmd.toUpperCase()).toString() : cmd);
   }

//******************************************************************************
// Xeq()
//******************************************************************************
   public ReplicantApp.RPLError Xeq()
   {
      String x = this.alias;
      ReplicantApp.RPLError rc = ReplicantApp.RPLError.value("SYNTAX_ERROR");
      String MethodToCall = "Do" + x.substring(0, 1).toUpperCase() + x.substring(1).toLowerCase();

      try
      {
         System.out.println("Trying to execute " + MethodToCall);
         Method method = getClass().getDeclaredMethod(MethodToCall);
         rc = (ReplicantApp.RPLError) method.invoke(this);
      }
      catch (Exception e)
      {
         System.out.println("Not found... Is it a name ?");
         rc = XeqName(x);
      }

      if (rc != ReplicantApp.RPLError.value("OK"))
      {
         ReplicantView.txtEntry.setText(this.cmd);
         ReplicantView.txtEntry.selectAll();
         Beep();
      }
      return rc;
   }

//******************************************************************************
// Beep()
//******************************************************************************
   public void Beep()
   {
      try
      {
         InputStream in = getClass().getResourceAsStream("resources/beep.wav");
         Clip clip = AudioSystem.getClip();
         AudioInputStream ais = AudioSystem.getAudioInputStream(in);
         clip.open(ais);
         clip.start();
         System.out.println("Beep !!!");
      }
      catch (Exception ex)
      {
         System.out.println("Beep Error !!! " + ex.toString());
      }
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
      tableAlias.put("<<", "BEGP");
      tableAlias.put(">>", "ENDP");
      tableAlias.put("!", "FACT");
   }

//******************************************************************************
// DoPlus()
//******************************************************************************
   private ReplicantApp.RPLError DoPlus()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         double d2 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         double d1 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         ReplicantApp.stack.push(d1 + d2);
         // return ReplicantApp.ERR_OK;
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoMinus()
//******************************************************************************
   private ReplicantApp.RPLError DoMinus()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         double d2 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         double d1 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         ReplicantApp.stack.push(d1 - d2);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoTimes()
//******************************************************************************
   private ReplicantApp.RPLError DoTimes()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         double d2 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         double d1 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         ReplicantApp.stack.push(d1 * d2);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoDivide()
//******************************************************************************
   private ReplicantApp.RPLError DoDivide()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         double d2 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         double d1 = Double.parseDouble(ReplicantApp.stack.pop().toString());
         if (d2 != 0.0)
         {
            ReplicantApp.stack.push(d1 / d2);
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(d1);
            ReplicantApp.stack.push(d2);
            return ReplicantApp.RPLError.value("INFINITE_RESULT");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoExit()
//******************************************************************************
   private ReplicantApp.RPLError DoExit()
   {
      ReplicantApp.getApplication().exit();
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoDrop()
//******************************************************************************
   private ReplicantApp.RPLError DoDrop()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object o = ReplicantApp.stack.pop();
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoDup()
//******************************************************************************
   private ReplicantApp.RPLError DoDup()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object o = ReplicantApp.stack.pop();
         ReplicantApp.stack.push(o);
         ReplicantApp.stack.push(o);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoSwap()
//******************************************************************************
   private ReplicantApp.RPLError DoSwap()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         Object o1 = ReplicantApp.stack.pop();
         Object o2 = ReplicantApp.stack.pop();
         ReplicantApp.stack.push(o1);
         ReplicantApp.stack.push(o2);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoRot()
//******************************************************************************
   private ReplicantApp.RPLError DoRot()
   {
      if (ReplicantApp.stack.depth() >= 3)
      {
         Object o3 = ReplicantApp.stack.pop();
         Object o2 = ReplicantApp.stack.pop();
         Object o1 = ReplicantApp.stack.pop();
         ReplicantApp.stack.push(o3);
         ReplicantApp.stack.push(o1);
         ReplicantApp.stack.push(o2);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoClear()
//******************************************************************************
   private ReplicantApp.RPLError DoClear()
   {
      ReplicantApp.stack.clear();
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoDepth()
//******************************************************************************
   private ReplicantApp.RPLError DoDepth()
   {
      Integer d = ReplicantApp.stack.depth();
      ReplicantApp.stack.push(d.doubleValue());
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoSin()
//******************************************************************************
   private ReplicantApp.RPLError DoSin()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            switch (ReplicantApp.RPLAngularMode)
            {
               case 'R':
                  ReplicantApp.stack.push(Math.sin(d));
                  break;
               case 'D':
                  ReplicantApp.stack.push(Math.sin(D2R(d)));
                  break;
               case 'G':
                  ReplicantApp.stack.push(Math.sin(G2R(d)));
                  break;
            }
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoCos()
//******************************************************************************
   private ReplicantApp.RPLError DoCos()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         switch (ReplicantApp.RPLAngularMode)
         {
            case 'R':
               ReplicantApp.stack.push(Math.cos(d));
               break;
            case 'D':
               ReplicantApp.stack.push(Math.cos(D2R(d)));
               break;
            case 'G':
               ReplicantApp.stack.push(Math.cos(G2R(d)));
               break;
         }
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoTan()
//******************************************************************************
   private ReplicantApp.RPLError DoTan()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         switch (ReplicantApp.RPLAngularMode)
         {
            case 'R':
               ReplicantApp.stack.push(Math.tan(d));
               break;
            case 'D':
               ReplicantApp.stack.push(Math.tan(D2R(d)));
               break;
            case 'G':
               ReplicantApp.stack.push(Math.tan(G2R(d)));
               break;
         }
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoAsin()
//******************************************************************************
   private ReplicantApp.RPLError DoAsin()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         switch (ReplicantApp.RPLAngularMode)
         {
            case 'R':
               ReplicantApp.stack.push(Math.asin(d));
               break;
            case 'D':
               ReplicantApp.stack.push(R2D(Math.asin(d)));
               break;
            case 'G':
               ReplicantApp.stack.push(R2G(Math.asin(d)));
               break;
         }
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoAcos()
//******************************************************************************
   private ReplicantApp.RPLError DoAcos()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         switch (ReplicantApp.RPLAngularMode)
         {
            case 'R':
               ReplicantApp.stack.push(Math.acos(d));
               break;
            case 'D':
               ReplicantApp.stack.push(R2D(Math.acos(d)));
               break;
            case 'G':
               ReplicantApp.stack.push(R2G(Math.acos(d)));
               break;
         }
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoAtan()
//******************************************************************************
   private ReplicantApp.RPLError DoAtan()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         switch (ReplicantApp.RPLAngularMode)
         {
            case 'R':
               ReplicantApp.stack.push(Math.atan(d));
               break;
            case 'D':
               ReplicantApp.stack.push(R2D(Math.atan(d)));
               break;
            case 'G':
               ReplicantApp.stack.push(R2G(Math.atan(d)));
               break;
         }
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoPi()
//******************************************************************************
   private ReplicantApp.RPLError DoPi()
   {
      ReplicantApp.stack.push(Math.PI);
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoE()
//******************************************************************************
   private ReplicantApp.RPLError DoE()
   {
      ReplicantApp.stack.push(Math.E);
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoRand()
//******************************************************************************
   private ReplicantApp.RPLError DoRand()
   {
      ReplicantApp.stack.push(Math.random());
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoDeg()
//******************************************************************************
   private ReplicantApp.RPLError DoDeg()
   {
      ReplicantApp.RPLAngularMode = 'D';
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoGrad()
//******************************************************************************
   private ReplicantApp.RPLError DoGrad()
   {
      ReplicantApp.RPLAngularMode = 'G';
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoRad()
//******************************************************************************
   private ReplicantApp.RPLError DoRad()
   {
      ReplicantApp.RPLAngularMode = 'R';
      return ReplicantApp.RPLError.value("OK");
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
// DoInv()
//******************************************************************************
   private ReplicantApp.RPLError DoInv()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         if (d != 0.0)
         {
            ReplicantApp.stack.push(1.0 / d);
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(d);
            return ReplicantApp.RPLError.value("INFINITE_RESULT");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoSq()
//******************************************************************************
   private ReplicantApp.RPLError DoSq()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         ReplicantApp.stack.push(d * d);
         return ReplicantApp.RPLError.value("OK");
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoSqrt()
//******************************************************************************
   private ReplicantApp.RPLError DoSqrt()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         double d = Double.parseDouble(ReplicantApp.stack.pop().toString());
         if (d >= 0.0)
         {
            ReplicantApp.stack.push(Math.sqrt(d));
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(d);
            return ReplicantApp.RPLError.value("COMPLEX_NUMBER");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }
// SQ, SQRT, POW, XROOT, LOG, ALOG, LN, EXP, INV,

//******************************************************************************
// DoBegp()
//******************************************************************************
   private ReplicantApp.RPLError DoBegp()
   {
      ReplicantApp.RPLRunMode = 'P';

      ReplicantApp.RPLPrograms.add(new RPLProgram());
      RPLProgram p = (RPLProgram) ReplicantApp.RPLPrograms.get(ReplicantApp.RPLPrograms.size() - 1);
      System.out.println(p.getName());
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoEndp()
//******************************************************************************
   private ReplicantApp.RPLError DoEndp()
   {
      ReplicantApp.RPLRunMode = 'R';
      RPLProgram p = (RPLProgram) ReplicantApp.RPLPrograms.get(ReplicantApp.RPLPrograms.size() - 1);
      ReplicantApp.stack.push(p);
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoSto()
//******************************************************************************
   private ReplicantApp.RPLError DoSto()
   {
      if (ReplicantApp.stack.depth() >= 2)
      {
         Object o1 = ReplicantApp.stack.pop();
         System.out.println("Obj1 : " + o1.getClass().getName());
         if (!o1.getClass().getName().equals("replicant.RPLName"))
         {
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
         else
         {
            Object o2 = ReplicantApp.stack.pop();
            System.out.println("Obj2 : " + o2.getClass().getName());
            RPLName n = (RPLName) o1;
            //******************************************************************
            // TODO test if not a reserved name
            //******************************************************************
            // test if this name is not already used
            for (Iterator<RPLName> itr = ReplicantApp.RPLNames.iterator(); itr.hasNext();)
            {
               RPLName t = itr.next();
               if (n.getName().equals(t.getName()))
               {
                  // yes, then drop it
                  itr.remove();
                  break;
               }
            }
            ReplicantApp.RPLNames.add(new RPLName(n.getName(), o2));
            return ReplicantApp.RPLError.value("OK");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoRcl()
//******************************************************************************
   private ReplicantApp.RPLError DoRcl()
   {
      ReplicantApp.RPLError rc = ReplicantApp.RPLError.value("UNDEFINED_NAME");
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object o1 = ReplicantApp.stack.pop();
         System.out.println("Obj1 : " + o1.getClass().getName());
         if (!o1.getClass().getName().equals("replicant.RPLName"))
         {
            ReplicantApp.stack.push(o1);
            rc = ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
         else
         {
            // loop to find this name (if any)
            RPLName o = (RPLName) o1;
            for (Iterator<RPLName> itr = ReplicantApp.RPLNames.iterator(); itr.hasNext();)
            {
               RPLName n = itr.next();
               if (o.getName().equals(n.getName()))
               {
                  // yes, then push it
                  ReplicantApp.stack.push(n.obj);
                  rc = ReplicantApp.RPLError.value("OK");
               }
            }
            if (rc != ReplicantApp.RPLError.value("OK"))
            {
               ReplicantApp.stack.push(o1);
            }
         }
         return rc;
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// XeqName()
//******************************************************************************
   private ReplicantApp.RPLError XeqName(String x)
   {
      ReplicantApp.RPLError rc = ReplicantApp.RPLError.value("SYNTAX_ERROR");
      // loop to find this name (if any)
      for (Iterator<RPLName> itr = ReplicantApp.RPLNames.iterator(); itr.hasNext();)
      {
         RPLName n = itr.next();
         if (x.equals(n.getName()))
         {            
            if (!n.obj.getClass().getName().equals("replicant.RPLProgram"))
            {  // yes, then push it
               ReplicantApp.stack.push(n.obj);
               rc = ReplicantApp.RPLError.value("OK");
            }
            else
            { // or run it
               RPLProgram pgm = (RPLProgram)n.obj;
               rc = pgm.Run();
            }
            break;
         }
      }
      return rc;
   }

//******************************************************************************
// DoArg()
//******************************************************************************
   private ReplicantApp.RPLError DoArg()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("replicant.RPLComplex"))
         {
            RPLComplex c = (RPLComplex) p;
            switch (ReplicantApp.RPLAngularMode)
            {
               case 'R':
                  ReplicantApp.stack.push(c.arg());
                  break;
               case 'D':
                  ReplicantApp.stack.push(R2D(c.arg()));
                  break;
               case 'G':
                  ReplicantApp.stack.push(R2G(c.arg()));
                  break;
            }
            return ReplicantApp.RPLError.value("OK");
         }
         else if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            if (d >= 0.0)
            {
               ReplicantApp.stack.push(0.0);
            }
            else
            {
               switch (ReplicantApp.RPLAngularMode)
               {
                  case 'R':
                     ReplicantApp.stack.push(Math.PI);
                     break;
                  case 'D':
                     ReplicantApp.stack.push(180.0);
                     break;
                  case 'G':
                     ReplicantApp.stack.push(200.0);
                     break;
               }
            }
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoIp()
//******************************************************************************
   private ReplicantApp.RPLError DoIp()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            ReplicantApp.stack.push(new Double((long) d));
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoFp()
//******************************************************************************
   private ReplicantApp.RPLError DoFp()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            ReplicantApp.stack.push(d - (long) d);
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoCeil()
//******************************************************************************
   private ReplicantApp.RPLError DoCeil()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            ReplicantApp.stack.push(Math.ceil(d));
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoFloor()
//******************************************************************************
   private ReplicantApp.RPLError DoFloor()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            ReplicantApp.stack.push(Math.floor(d));
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoTime()
//******************************************************************************
   private ReplicantApp.RPLError DoTime()
   {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("H.mmssSSS");
      String t = sdf.format(cal.getTime());

      ReplicantApp.stack.push(Double.parseDouble(t));
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoDate()
//******************************************************************************
   private ReplicantApp.RPLError DoDate()
   {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("MM.ddyyyy");
      String d = sdf.format(cal.getTime());

      ReplicantApp.stack.push(Double.parseDouble(d));
      return ReplicantApp.RPLError.value("OK");
   }

//******************************************************************************
// DoFix()
//******************************************************************************
   private ReplicantApp.RPLError DoFix()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            int f = (int)(d + 0.5);
            if(f<0)
               f=0;
            if(f>15)
               f=15;
            ReplicantApp.RPLFixNumber = f;
            ReplicantApp.RPLDisplayMode = 'F';
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoEng()
//******************************************************************************
   private ReplicantApp.RPLError DoEng()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            int f = (int)(d + 0.5);
            if(f<0)
               f=0;
            if(f>15)
               f=15;
            ReplicantApp.RPLFixNumber = f;
            ReplicantApp.RPLDisplayMode = 'E';
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoSci()
//******************************************************************************
   private ReplicantApp.RPLError DoSci()
   {
      if (ReplicantApp.stack.depth() >= 1)
      {
         Object p = ReplicantApp.stack.pop();
         if (p.getClass().getName().equals("java.lang.Double"))
         {
            double d = Double.parseDouble(p.toString());
            int f = (int)(d + 0.5);
            if(f<0)
               f=0;
            if(f>15)
               f=15;
            ReplicantApp.RPLFixNumber = f;
            ReplicantApp.RPLDisplayMode = 'S';
            return ReplicantApp.RPLError.value("OK");
         }
         else
         {
            ReplicantApp.stack.push(p);
            return ReplicantApp.RPLError.value("BAD_ARGUMENT_TYPE");
         }
      }
      else
      {
         return ReplicantApp.RPLError.value("TOO_FEW_ARGUMENTS");
      }
   }

//******************************************************************************
// DoStd()
//******************************************************************************
   private ReplicantApp.RPLError DoStd()
   {
      ReplicantApp.RPLFixNumber = 0;
      ReplicantApp.RPLDisplayMode = 'D';
      return ReplicantApp.RPLError.value("OK");
   }
}
