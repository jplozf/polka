//******************************************************************************
// File     : ReplicantApp.java
// Author   : jpl
// Created  : 22/09/11 15:43
// Modified : 17/10/11 16:58
//******************************************************************************
package replicant;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ReplicantApp is the main entry point for the Replicant application.
 * It defines some globals variables, constants and so on...
 * <p>
 * Here also you can find the <b>exit manager</b> which will be called on exit
 * event and will be responsible to do some cleanup and saving the calculator
 * memory and stack.
 * </p>
 */
public class ReplicantApp extends SingleFrameApplication
{

   /**
    * Defines Error messages as enum.
    * <p>
    * toString method has been overrided to display nice message according to the enum code.
    * </p>
    */
   public enum RPLError
   {

      OK,
      TOO_FEW_ARGUMENTS,
      INFINITE_RESULT,
      BAD_ARGUMENT_TYPE,
      SYNTAX_ERROR,
      COMPLEX_NUMBER,
      UNDEFINED_NAME,
      INVALID_SYNTAX,
      RUNNING,
      NONE;

      @Override
      public String toString()
      {
         String s = this.name().toLowerCase();
         s = s.replace('_', ' ');

         final StringBuilder result = new StringBuilder(s.length());
         String[] words = s.split("\\s");
         for (int i = 0, l = words.length; i < l; ++i)
         {
            if (i > 0)
            {
               result.append(" ");
            }
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
         }

         return result.toString();
      }

      public static RPLError value(String str)
      {
         try
         {
            return valueOf(str);
         } catch (Exception ex)
         {
            return NONE;
         }
      }
   }
   // Constants
   public static final String RPLSTACK_SAVE = "RPLStack.sav";
   public static final String RPLNAMES_SAVE = "RPLNames.sav";
   public static final String RPLPROPS_FILE = "Replicant.ini";
   // Types
   public static final int TYPE_DOUBLE = 1;
   public static final int TYPE_COMMAND = 2;
   public static final int TYPE_STRING = 3;
   public static final int TYPE_NAME = 4;
   public static final int TYPE_PROGRAM = 5;
   public static final String RPL_BEEP = "";
   // Misc global variables
   public static Properties RPLProps = new Properties();
   public static String RPLStatus = "Welcome";
   public static RPLStack stack = new RPLStack();
   public static char RPLAngularMode = 'R'; // D=Degrees R=Radians G=Grades
   public static char RPLRunMode = 'R'; // R=Run P=Program
   public static int RPLFixNumber = 4;
   public static char RPLDisplayMode = 'D'; // D=STD F=FIX E=ENG S=SCI
   public static String RPLListStackBackgroundColor;
   public static String RPLListStackTextColor;
   public static String RPLPanelFixedBackgroundColor;
   public static String RPLPanelFixedTextColor;
   public static ArrayList<RPLProgram> RPLPrograms = new ArrayList<RPLProgram>();
   public static ArrayList<RPLName> RPLNames = new ArrayList<RPLName>();
   public static ArrayList<String> commands = new ArrayList<String>();
   public static int iCommands = -1;
   public static boolean PgmRunning = false;
   // For the JTree View
   public static DefaultMutableTreeNode ndeRoot = new DefaultMutableTreeNode("Root");
   public static DefaultMutableTreeNode ndePrograms = new DefaultMutableTreeNode("Programs");
   public static DefaultMutableTreeNode ndeDoubles = new DefaultMutableTreeNode("Doubles");
   public static DefaultMutableTreeNode ndeComplex = new DefaultMutableTreeNode("Complex");
   public static DefaultMutableTreeNode ndeMatrix = new DefaultMutableTreeNode("Matrix");

   /**
    * At startup create and show the main frame of the application.
    */
   @Override
   protected void startup()
   {
      // bind an exitlistener
      ExitListener maybeExit = new ExitListener()
      {

         public boolean canExit(EventObject e)
         {
            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit ?");
            return option == JOptionPane.YES_OPTION;
         }

         public void willExit(EventObject e)
         {
            // serialize RPLStack
            SaveStack();
            // serialize RPLNames
            SaveNames();
            // save ini file
            WriteProps();
         }
      };
      addExitListener(maybeExit);

      // set the focus to entry text area
      SwingUtilities.invokeLater(new Runnable()
      {

         public void run()
         {
            ReplicantView.txtEntry.requestFocus();
         }
      });

      // and the show must go on
      ReplicantView ZeView = new ReplicantView(this);
      show(ZeView);
   }

   /**
    * This method is to initialize the specified window by injecting resources.
    * Windows shown in our application come fully initialized from the GUI
    * builder, so this additional configuration is not needed.
    */
   @Override
   protected void configureWindow(java.awt.Window root)
   {
   }

   /**
    * A convenient static getter for the application instance.
    * @return the instance of ReplicantApp
    */
   public static ReplicantApp getApplication()
   {
      return Application.getInstance(ReplicantApp.class);
   }

   /**
    * Main method launching the application.
    */
   public static void main(String[] args)
   {
      launch(ReplicantApp.class, args);
   }

//******************************************************************************
// SaveStack()
//******************************************************************************
   private static void SaveStack()
   {
      try
      {

         FileOutputStream fos = new FileOutputStream(RPLSTACK_SAVE);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         try
         {
            oos.writeObject(stack);
            oos.flush();
         } finally
         {
            try
            {
               oos.close();
            } finally
            {
               fos.close();
            }
         }
      } catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

//******************************************************************************
// RestoreStack()
//******************************************************************************
   public static int RestoreStack()
   {
      File f = new File(RPLSTACK_SAVE);
      int rc = 0;

      if (f.exists())
      {
         try
         {
            FileInputStream fis = new FileInputStream(RPLSTACK_SAVE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try
            {
               stack = (RPLStack) ois.readObject();
            } finally
            {
               try
               {
                  ois.close();
               } finally
               {
                  fis.close();
               }
            }
         } catch (IOException ioe)
         {
            // ioe.printStackTrace();
            rc = 1;
         } catch (ClassNotFoundException cnfe)
         {
            // cnfe.printStackTrace();
            rc = 1;
         }
      }
      return (rc);
   }

//******************************************************************************
// WriteProps()
//******************************************************************************
   private static void WriteProps()
   {
      RPLProps.setProperty("RPLAngularMode", Character.toString(RPLAngularMode));
      RPLProps.setProperty("RPLDisplayMode", Character.toString(RPLDisplayMode));
      RPLProps.setProperty("RPLFixNumber", Integer.toString(RPLFixNumber));
//    RPLProps.setProperty("RPLPanelFixedBackgroundColor", String.valueOf(ReplicantView.pnlFixed.getBackground()));
//    RPLProps.setProperty("RPLPanelFixedTextColor", String.valueOf(ReplicantView.lblReg1.getForeground()));

      try
      {
         RPLProps.store(new FileOutputStream(RPLPROPS_FILE), null);
      } catch (IOException e)
      {
      }

   }

//******************************************************************************
// ReadProps()
//******************************************************************************
   public static int ReadProps()
   {
      int rc = 0;
      try
      {
         RPLProps.load(new FileInputStream(RPLPROPS_FILE));
         RPLAngularMode = RPLProps.getProperty("RPLAngularMode").charAt(0);
         RPLDisplayMode = RPLProps.getProperty("RPLDisplayMode").charAt(0);
         RPLFixNumber = Integer.parseInt(RPLProps.getProperty("RPLFixNumber"));
      } catch (Exception e)
      {
         rc = 1;
      }

      return (rc);
   }
   
//******************************************************************************
// SaveNames()
//******************************************************************************
   private static void SaveNames()
   {
      try
      {

         FileOutputStream fos = new FileOutputStream(RPLNAMES_SAVE);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         try
         {
            oos.writeObject(RPLNames);
            oos.flush();
         } finally
         {
            try
            {
               oos.close();
            } finally
            {
               fos.close();
            }
         }
      } catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

//******************************************************************************
// RestoreNames()
//******************************************************************************
   public static int RestoreNames()
   {
      File f = new File(RPLNAMES_SAVE);
      int rc = 0;

      if (f.exists())
      {
         try
         {
            FileInputStream fis = new FileInputStream(RPLNAMES_SAVE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try
            {
               RPLNames = (ArrayList<RPLName>) ois.readObject();
            } finally
            {
               try
               {
                  ois.close();
               } finally
               {
                  fis.close();
               }
            }
         } catch (IOException ioe)
         {
            // ioe.printStackTrace();
            rc = 1;
         } catch (ClassNotFoundException cnfe)
         {
            // cnfe.printStackTrace();
            rc = 1;
         }
      }
      return (rc);
   }   
}
