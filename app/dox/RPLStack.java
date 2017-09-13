//******************************************************************************
// File     : RPLStack.java
// Author   : jpl
// Created  : 22/09/11 15:43
// Modified : 06/10/11 16:37
//******************************************************************************
package replicant;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Iterator;
import java.util.Stack;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import java.io.Serializable;
import java.text.DecimalFormat;

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

//******************************************************************************
// toJList()
//******************************************************************************
   /**
   Method to populate a <code>JList</code> with all the objects stored in the stack.
   @param jl the <code>JList</code> object to populate.
    */
   public void toJList(JList jl)
   {
      Iterator it = this.iterator();
      jl.setModel(new DefaultListModel());

      int i = this.depth();
      int j = i;
      jl.removeAll();
      while (it.hasNext())
      {
         String iValue = it.next().toString();
         ((DefaultListModel) jl.getModel()).addElement(j + ": " + iValue);
         j--;
      }
      if (i > 1)
      {
         jl.ensureIndexIsVisible(i - 1);
      }
   }

//******************************************************************************
// toFixedDisplay()
//******************************************************************************
   /**
   Method to display the fourth first registers like an HP48 device.
    */
   public void toFixedDisplay()
   {
      ReplicantView.lblReg1.setText("");
      ReplicantView.lblReg2.setText("");
      ReplicantView.lblReg3.setText("");
      ReplicantView.lblReg4.setText("");

      int i = this.depth();
      if (i > 0)
      {
         FormatRegister(ReplicantView.lblReg1, this.get(i - 1));
         if (i > 1)
         {
            FormatRegister(ReplicantView.lblReg2, this.get(i - 2));
            if (i > 2)
            {
               FormatRegister(ReplicantView.lblReg3, this.get(i - 3));
               if (i > 3)
               {
                  FormatRegister(ReplicantView.lblReg4, this.get(i - 4));
               }
            }
         }
      }
   }

//******************************************************************************
// FormatRegister()
//******************************************************************************
   /**
   Method called by <code>toFixedDisplay</code> to format a <code>Double</code> value according
   to the <code>STD</code>, <code>ENG</code>, <code>FIX</code> and <code>SCI</code> commands.
   @param label The <code>JLabel</code> in which the value will be displayed.
   @param value The <code>Object</code> we want to display.
    */
   private void FormatRegister(JLabel label, Object value)
   {
      String s = value.toString();
      Font f = label.getFont();
      FontMetrics fm = label.getFontMetrics(f);
      int PanelWidth = ReplicantView.pnlFixed.getWidth();
      int LabelWidth = PanelWidth - fm.stringWidth(ReplicantView.lblPrompt.getText() + "XX");

      if (value.getClass().getName().equals("java.lang.Double"))
      {
         s = FormatDouble(Double.parseDouble(value.toString()));
      }
      label.setText(s);

      int l = fm.stringWidth(label.getText());
      while (l > LabelWidth && l != 0)
      {
         String sl = label.getText();
         label.setText(sl.substring(0, sl.length() - 1));
         l = fm.stringWidth(label.getText());
      }
   }

//******************************************************************************
// FormatDouble()
//******************************************************************************
   public String FormatDouble(Double number)
   {
      String s = "";
      String fmt = "";
      int digit = ReplicantApp.RPLFixNumber;

      // formatting double number according to STD, ENG, FIX, SCI
      switch (ReplicantApp.RPLDisplayMode)
      {
         case 'F':   // FIX
            fmt = "0.";
            while (digit > 0)
            {
               fmt = fmt + "0";
               digit--;
            }
            s = new DecimalFormat(fmt).format(number);
            break;

         case 'E':   // ENG (same as SCI but power multiple of 3
            fmt = "0.";
            while (digit > 0)
            {
               fmt = fmt + "0";
               digit--;
            }
            fmt = fmt + "E0";
            s = new DecimalFormat(fmt).format(number);
            break;

         case 'S':   // SCI
            fmt = "0.";
            while (digit > 0)
            {
               fmt = fmt + "0";
               digit--;
            }
            fmt = fmt + "E0";
            s = new DecimalFormat(fmt).format(number);
            break;

         default:
         case 'D':   // STD
            fmt = "0.###############"; // 15 digits after decimal point
            s = new DecimalFormat(fmt).format(number);
            break;
      }
      return (s);
   }
}
