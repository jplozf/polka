//******************************************************************************
// File     : PolkaActivity.java
// Author   : jpl
// Created  : 10/07/16 15:43
// Modified : 30/07/16 21:55
//******************************************************************************
package fr.ligorax.polka;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnFocusChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolkaActivity extends Activity
{
   // Constants
   public static final String RPL_FOLDER = "/sdcard/download/";
   public static final String RPLSTACK_SAVE = "RPLStack.sav";
   public static final String RPLNAMES_SAVE = "RPLNames.sav";
   public static final String RPLPROPS_FILE = "Polka.ini";
   // Types
   public static final int TYPE_DOUBLE = 1;
   public static final int TYPE_COMMAND = 2;
   public static final int TYPE_STRING = 3;
   public static final int TYPE_NAME = 4;
   public static final int TYPE_PROGRAM = 5;
   // Menus ID
   public static final int MENU_SETTINGS_ID = 0;
   public static final int MENU_EXIT_ID = 1;
   //
   public final int INPUT_CONTEXT_COMMAND_LINE = 0;
   public final int INPUT_CONTEXT_KEYS = 1;
   public final int INPUT_CONTEXT_PROGRAM = 2;
   //
   public static Properties RPLProps = new Properties();
   public static String RPLStatus = "Welcome";
   public static RPLStack stack = new RPLStack();
   public static char RPLAngularMode = 'R'; // D=Degrees R=Radians G=Grades
   public static char RPLRunMode = 'R'; // R=Run P=Program
   public static int RPLFixNumber = 4;
   public static char RPLDisplayMode = 'F'; // D=STD F=FIX E=ENG S=SCI
   public static ArrayList<RPLProgram> RPLPrograms = new ArrayList<RPLProgram>();
   public static ArrayList<RPLName> RPLNames = new ArrayList<RPLName>();
   public static ArrayList<String> commands = new ArrayList<String>();
   public static int iCommands = -1;
   public static boolean PgmRunning = false;
   private Menu actionBarMenu;
   //
   TextView txtInput;
   //
   public static boolean inNewInput = false;
   Vibrator vib;
   Hashtable tableAlias = new Hashtable();
   LocationManager locationManager;
   public boolean keyFunction = false;
   public static boolean inCommandLine = false;

   @Override
   //***************************************************************************
   // onCreate()
   //***************************************************************************
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      txtInput = (TextView) findViewById(R.id.txtInput);
      txtInput.setOnKeyListener(new OnKeyListener()
      {
         public boolean onKey(View v, int keyCode, KeyEvent event)
         {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
               (keyCode == KeyEvent.KEYCODE_ENTER))
            {
               parseEntry();
               txtInput.setText("");
               txtInput.requestFocus();
               return true;
            }
            return false;
         }
      });
      txtInput.setOnFocusChangeListener(new OnFocusChangeListener() {
         @Override
         public void onFocusChange(View v, boolean hasFocus) {
               inCommandLine = hasFocus;
         }
      });
      String locationContext = Context.LOCATION_SERVICE;
      locationManager = (LocationManager) getSystemService(locationContext);
   }

   @Override
   //***************************************************************************
   // onResume()
   //***************************************************************************
   protected void onResume()
   {
      super.onResume();
      String msg = "Ok";
      if (readProps() != 0)
      {
         msg = "Prefs Lost";
      }
      if (restoreNames() != 0)
      {
         msg = "Names Lost";
      }
      if (restoreStack() != 0)
      {
         msg = "Memory Lost";
      }
      displayStack(msg);
   }

   @Override
   //***************************************************************************
   // onPause()
   //***************************************************************************
   protected void onPause()
   {
      super.onPause();
      Toast.makeText(this, "Polka's state saved", Toast.LENGTH_SHORT).show();
      saveStack();
      saveNames();
      writeProps();
   }

   //***********************************************************************
   // onCreateOptionsMenu()
   //***********************************************************************
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.actionbar, menu);
      actionBarMenu = menu;
      return super.onCreateOptionsMenu(menu);
   }

   //***********************************************************************
   // onOptionsItemSelected()
   //***********************************************************************
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      // Manage the action bar
      switch (item.getItemId())
      {
         case R.id.overflow:
            Intent intent = new Intent(this, PolkaPrefs.class);
            startActivity(intent);
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }

   //***************************************************************************
   // onClickEnter()
   //***************************************************************************
   public void onClickEnter(View v)
   {
      if (vib.hasVibrator())
      {
         vib.vibrate(80);
      }
      parseEntry();
   }

   //***************************************************************************
   // onClickButtons()
   //***************************************************************************
   public void onClickButtons(View v)
   {
      int context = INPUT_CONTEXT_KEYS;
      RPLCommand cmd;
      RPLError rc;
      txtInput = (TextView) findViewById(R.id.txtInput);
      if (vib.hasVibrator())
      {
         vib.vibrate(80);
      }
      switch (v.getId())
      {
         case (R.id.btnFN):
            rc = RPLError.value("OK");
            keyFunction = !keyFunction;
            displayStack(rc.toString());
            break;

         case (R.id.btnDROP):
            if(inCommandLine==true)
               appendToCL("DROP ");
            else
            {
               cmd = new RPLCommand(this, "DROP", context);
               rc = cmd.xeq();
               displayStack(rc.toString());
            }
            break;

         case (R.id.btnSWAP):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "RAND", context);
            else
               cmd = new RPLCommand(this, "SWAP", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnADD):
            if (keyFunction == true)
               appendToCL(" ");
            else
            {
               cmd = new RPLCommand(this, "PLUS", context);
               rc = cmd.xeq();
               displayStack(rc.toString());
            }
            keyFunction = false;
            break;

         case (R.id.btnSUB):
            cmd = new RPLCommand(this, "MINUS", context);
            rc = cmd.xeq();
            displayStack(rc.toString());
            break;

         case (R.id.btnMUL):
            cmd = new RPLCommand(this, "TIMES", context);
            rc = cmd.xeq();
            displayStack(rc.toString());
            break;

         case (R.id.btnDIV):
            cmd = new RPLCommand(this, "DIVIDE", context);
            rc = cmd.xeq();
            displayStack(rc.toString());
            break;

         case (R.id.btnSTO):
            if (keyFunction == true)
            cmd = new RPLCommand(this, "RCL", context);
            else
            cmd = new RPLCommand(this, "STO", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnSIN):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "ASIN", context);
            else
               cmd = new RPLCommand(this, "SIN", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnCOS):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "ACOS", context);
            else
               cmd = new RPLCommand(this, "COS", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnTAN):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "ATAN", context);
            else
               cmd = new RPLCommand(this, "TAN", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnRAD):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "DEG", context);
            else
               cmd = new RPLCommand(this, "RAD", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnINT):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "FRAC", context);
            else
               cmd = new RPLCommand(this, "INT", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnBEEP):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "TONE", context);
            else
               cmd = new RPLCommand(this, "BEEP", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnTIME):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "DATE", context);
            else
               cmd = new RPLCommand(this, "TIME", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnLAT):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "LON", context);
            else
               cmd = new RPLCommand(this, "LAT", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnCHS):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "ABS", context);
            else
               cmd = new RPLCommand(this, "CHS", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnSQRT):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "SQ", context);
            else
               cmd = new RPLCommand(this, "SQRT", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnINV):
            if (keyFunction == true)
               cmd = new RPLCommand(this, "POW", context);
            else
               cmd = new RPLCommand(this, "INV", context);
            rc = cmd.xeq();
            keyFunction = false;
            displayStack(rc.toString());
            break;

         case (R.id.btnCURLY):
            if (keyFunction == true)
               appendToCL("[  ]");
            else
               appendToCL("{  }");
            keyFunction = false;
            break;

         case (R.id.btnNUM0):
            appendToX("0");
            break;

         case (R.id.btnNUM1):
            appendToX("1");
            break;

         case (R.id.btnNUM2):
            appendToX("2");
            break;

         case (R.id.btnNUM3):
            appendToX("3");
            break;

         case (R.id.btnNUM4):
            appendToX("4");
            break;

         case (R.id.btnNUM5):
            appendToX("5");
            break;

         case (R.id.btnNUM6):
            appendToX("6");
            break;

         case (R.id.btnNUM7):
            appendToX("7");
            break;

         case (R.id.btnNUM8):
            appendToX("8");
            break;

         case (R.id.btnNUM9):
            if (keyFunction == true)
            {
               cmd = new RPLCommand(this, "PI", context);
               rc = cmd.xeq();
               displayStack(rc.toString());
            }
            else
            {
               appendToX("9");
            }
            keyFunction = false;
            break;

         case (R.id.btnDOT):
            appendToX(".");
            break;

      }
   }

   //***************************************************************************
   // appendToX()
   //***************************************************************************
   private void appendToX(String c)
   {
      RPLError rc = RPLError.value("OK");

      txtInput = (TextView) findViewById(R.id.txtInput);
      if (inNewInput == false)
      {
         stack.push(0.0);
         inNewInput = true;
      }
      txtInput.append(c);

      stack.set(stack.size() - 1, (Double) Double.parseDouble(txtInput.getText().toString()));
      displayStack(rc.toString());
   }

   //***************************************************************************
   // appendToCL()
   //***************************************************************************
   private void appendToCL(String c)
   {
      RPLError rc = RPLError.value("OK");
      txtInput = (TextView) findViewById(R.id.txtInput);
      txtInput.append(c);
   }

   //***************************************************************************
   // checkArgs()
   //***************************************************************************
   public boolean checkArgs(int args)
   {
      boolean rc = true;
      if (stack.depth() < args)
         rc = false;
      return (rc);
   }

   //***************************************************************************
   // beep()
   //***************************************************************************
   public void beep()
   {
      ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
      toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
   }
   
   //******************************************************************************
   // displayStack()
   //******************************************************************************
   public void displayStack(String msg)
   {
      TextView txtStatusBar = (TextView) findViewById(R.id.txtStatusBar);
      TextView txtFunction = (TextView) findViewById(R.id.txtFunction);
      TextView txtInput = (TextView) findViewById(R.id.txtInput);
      TextView txtLCD1 = (TextView) findViewById(R.id.txtLCD1);
      TextView txtLCD2 = (TextView) findViewById(R.id.txtLCD2);
      TextView txtLCD3 = (TextView) findViewById(R.id.txtLCD3);
      TextView txtLCD4 = (TextView) findViewById(R.id.txtLCD4);

      txtLCD1.setText("");
      txtLCD2.setText("");
      txtLCD3.setText("");
      txtLCD4.setText("");

      int i = stack.depth();
      String statusBar = " ";
      switch (RPLAngularMode)
      {
         case 'R':
            statusBar += "RAD";
            break;

         case 'D':
            statusBar += "DEG";
            break;

         case 'G':
            statusBar += "GRD";
            break;
      }

      statusBar = statusBar + " " + Integer.toString(i) + " " + msg + "\n";
      txtStatusBar.setText(statusBar);
      if (keyFunction == true)
         txtFunction.setText("⇩");
      else
         txtFunction.setText("");

      if (i > 0)
      {
         formatRegister(txtLCD1, stack.get(i - 1));
         if (i > 1)
         {
            formatRegister(txtLCD2, stack.get(i - 2));
            if (i > 2)
            {
               formatRegister(txtLCD3, stack.get(i - 3));
               if (i > 3)
               {
                  formatRegister(txtLCD4, stack.get(i - 4));
               }
            }
         }
      }

      if (!msg.equals("Ok"))
      {
         beep();
      }
   }

   //******************************************************************************
   // formatRegister()
   //******************************************************************************
   private void formatRegister(TextView label, Object value)
   {
      String s = value.toString();
      if (value.getClass().getName().equals("java.lang.Double"))
      {
         s = formatDouble(Double.parseDouble(value.toString()));
      }
      label.setText(s);
   }

   //******************************************************************************
   // formatDouble()
   //******************************************************************************
   public String formatDouble(Double number)
   {
      String s = "";
      String fmt = "";
      int digit = PolkaActivity.RPLFixNumber;

      // formatting double number according to STD, ENG, FIX, SCI
      switch (PolkaActivity.RPLDisplayMode)
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

   //******************************************************************************
   // parseEntry()
   //******************************************************************************
   private void parseEntry()
   {
      txtInput = (TextView) findViewById(R.id.txtInput);

      PolkaActivity.RPLError rc = PolkaActivity.RPLError.value("Ok");
      String e = txtInput.getText().toString();
      PolkaActivity.commands.add(e);
      PolkaActivity.iCommands++;

      if (e.isEmpty())
      {
         rc = whatToDoWith("DUP");
      }
      else
      {
         // split token between spaces, taking care of simple and double quotes
         List<String> matchList = new ArrayList<String>();
         Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
         Matcher regexMatcher = regex.matcher(e);
         while (regexMatcher.find())
         {
            matchList.add(regexMatcher.group());
         }

         // browse the splitted tokens
         Iterator i = matchList.iterator();
         while (i.hasNext())
         {
            rc = whatToDoWith(i.next().toString());
         }
      }

      // cleanup and display
      txtInput.setText("");
      displayStack(rc.toString());
   }

   //******************************************************************************
   // whatToDoWith()
   //******************************************************************************
   public PolkaActivity.RPLError whatToDoWith(String token)
   {
      PolkaActivity.RPLError r = PolkaActivity.RPLError.value("OK");
      System.out.println("TOKEN : " + token);

      if (PolkaActivity.RPLRunMode == 'R')
      {
         if (isString(token))
         {
            PolkaActivity.stack.push(token);
         }
         else if (isName(token))
         {
            RPLName rn = new RPLName(token.substring(1, token.length() - 1));
            PolkaActivity.stack.push(rn);
         }
         else if (isComplexPolar(token))
         {
            Double u = getReal(token);
            Double v = getImaginary(token);
            RPLComplex rc = new RPLComplex(u, v);
            PolkaActivity.stack.push(rc);
         }
         else if (isComplexRectangular(token))
         {
            Double u = getReal(token);
            Double v = getImaginary(token);
            RPLComplex rc = new RPLComplex(u, v);
            PolkaActivity.stack.push(rc);
         }
         else if (isDouble(token))
         {
            PolkaActivity.stack.push(Double.parseDouble(token));
         }
         else // command
         {
            RPLCommand rc = new RPLCommand(this, token, INPUT_CONTEXT_COMMAND_LINE);
            r = rc.xeq();
         }
      }
      else
      {
         if (token.equals("}"))
         {
            RPLCommand rc = new RPLCommand(this, token, INPUT_CONTEXT_COMMAND_LINE);
            r = rc.xeq();
         }
         else
         {
            RPLProgram p = (RPLProgram) PolkaActivity.RPLPrograms.get(PolkaActivity.RPLPrograms.size() - 1);
            p.step.add(token);
         }

      }

      if (r == PolkaActivity.RPLError.value("OK"))
      {
         TextView txtInput = (TextView) findViewById(R.id.txtInput);
         txtInput.setText("");
      }

      return r;
   }

   //******************************************************************************
   // RPLError()
   //******************************************************************************
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
   }

   //***************************************************************************
   // toast()
   //***************************************************************************
   private void toast(String msg)
   {
      Toast.makeText(getApplication().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
   }
   
   //***********************************************************************
   // messageBox()
   //***********************************************************************
   private void messageBox(String msg)
   {
      messageBox("MessageBox", msg);
   }

   //***********************************************************************
   // messageBox()
   //***********************************************************************
   private void messageBox(String title, String msg)
   {
      AlertDialog.Builder adb = new AlertDialog.Builder(PolkaActivity.this);
      adb.setTitle(title);
      adb.setMessage(msg);
      adb.setPositiveButton("Ok", null);
      adb.show();
   }

   //******************************************************************************
   // isString()
   //******************************************************************************
   private static boolean isString(Object o)
   {
      Matcher m;
      boolean rc = false;
      // REGEX "[^"\r\n]*"
      // "hello from JP"
      m = Pattern.compile("\"[^\"\r\n]*\"").matcher(o.toString());
      if (m.find())
      {
         rc = true;
         System.out.println("MATCH STRING");
      }
      return rc;
   }

   //******************************************************************************
   // isName()
   //******************************************************************************
   private static boolean isName(Object o)
   {
      Matcher m;
      boolean rc = false;
      // 'variable'
      m = Pattern.compile("'[^ '\n\r\t]*'").matcher(o.toString());
      if (m.find())
      {
         rc = true;
         System.out.println("MATCH NAME");
      }

      return rc;
   }

   //******************************************************************************
   // isComplexRectangular()
   //******************************************************************************
   private static boolean isComplexRectangular(Object o)
   {
      Matcher m;
      boolean rc = false;
      // REGEX \([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?,[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?\)
      // (5.2e3,17.2)
      m = Pattern.compile("\\([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?,[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?\\)").matcher(o.toString());
      if (m.find())
      {
         rc = true;
         System.out.println("MATCH COMPLEX RECTANGULAR");
      }

      return rc;
   }

   //******************************************************************************
   // isComplexPolar()
   //******************************************************************************
   private static boolean isComplexPolar(Object o)
   {
      Matcher m;
      boolean rc = false;
      // REGEX \([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?@[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?\)
      // (5.2e3@185.5)
      m = Pattern.compile("\\([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?@[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?\\)").matcher(o.toString());
      if (m.find())
      {
         rc = true;
         System.out.println("MATCH COMPLEX POLAR");
      }

      return rc;
   }

   //******************************************************************************
   // isDouble()
   //******************************************************************************
   private static boolean isDouble(Object o)
   {
      Matcher m;
      boolean rc = false;
      // REGEX [-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+
      // m = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").matcher(o.toString());
      m = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$").matcher(o.toString());
      if (m.find())
      {
         rc = true;
         System.out.println("MATCH DOUBLE");
      }

      return (rc);
   }

   //******************************************************************************
   // getReal()
   //******************************************************************************
   private static Double getReal(String Complex)
   {
      Matcher m = Pattern.compile("\\((.*?),").matcher(Complex);
      if (m.find())
      {
         return (Double.parseDouble(m.group(1)));
      }
      else
      {
         return (0.0);
      }
   }

   //******************************************************************************
   // getImaginary()
   //******************************************************************************
   private static Double getImaginary(String Complex)
   {
      Matcher m = Pattern.compile(",(.*?)\\)").matcher(Complex);
      if (m.find())
      {
         return (Double.parseDouble(m.group(1)));
      }
      else
      {
         return (0.0);
      }
   }

   //******************************************************************************
   // restoreStack()
   //******************************************************************************
   public static int restoreStack()
   {
      File f = new File(RPL_FOLDER + RPLSTACK_SAVE);
      int rc = 0;

      if (f.exists())
      {
         try
         {
            FileInputStream fis = new FileInputStream(RPL_FOLDER + RPLSTACK_SAVE);
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
   // writeProps()
   //******************************************************************************
   private static void writeProps()
   {
      RPLProps.setProperty("RPLAngularMode", Character.toString(RPLAngularMode));
      RPLProps.setProperty("RPLDisplayMode", Character.toString(RPLDisplayMode));
      RPLProps.setProperty("RPLFixNumber", Integer.toString(RPLFixNumber));
//    RPLProps.setProperty("RPLPanelFixedBackgroundColor", String.valueOf(ReplicantView.pnlFixed.getBackground()));
//    RPLProps.setProperty("RPLPanelFixedTextColor", String.valueOf(ReplicantView.lblReg1.getForeground()));

      try
      {
         RPLProps.store(new FileOutputStream(RPL_FOLDER + RPLPROPS_FILE), null);
      } catch (IOException e)
      {
      }

   }

   //******************************************************************************
   // readProps()
   //******************************************************************************
   public static int readProps()
   {
      int rc = 0;
      try
      {
         RPLProps.load(new FileInputStream(RPL_FOLDER + RPLPROPS_FILE));
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
   // saveNames()
   //******************************************************************************
   private static void saveNames()
   {
      try
      {

         FileOutputStream fos = new FileOutputStream(RPL_FOLDER + RPLNAMES_SAVE);
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
   // saveStack()
   //******************************************************************************
   private void saveStack()
   {
      try
      {

         FileOutputStream fos = new FileOutputStream(RPL_FOLDER + RPLSTACK_SAVE);
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
   // restoreNames()
   //******************************************************************************
   public static int restoreNames()
   {
      File f = new File(RPL_FOLDER + RPLNAMES_SAVE);
      int rc = 0;

      if (f.exists())
      {
         try
         {
            FileInputStream fis = new FileInputStream(RPL_FOLDER + RPLNAMES_SAVE);
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

   //***************************************************************************
   // LatLon()
   //***************************************************************************
   public static class LatLon
   {
      static double Latitude;
      static double Longitude;

      public LatLon()
      {
         this.Latitude = 0.0;
         this.Longitude = 0.0;
      }

      public LatLon(double Latitude, double Longitude)
      {
         this.Latitude = Latitude;
         this.Longitude = Longitude;
      }
   }

   //***************************************************************************
   // getLocation()
   //***************************************************************************
   public LatLon getLocation()
   {
      // Get the location manager
      LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
      Criteria criteria = new Criteria();
      String bestProvider = locationManager.getBestProvider(criteria, false);
      Location location = locationManager.getLastKnownLocation(bestProvider);
      Double lat, lon;
      try
      {
         lat = location.getLatitude();
         lon = location.getLongitude();
         return new LatLon(lat, lon);
      } catch (NullPointerException e)
      {
         e.printStackTrace();
         return new LatLon(0.0, 0.0);
      }
   }
}
