//******************************************************************************
// File     : RPLComplex.java
// Author   : jpl
// Created  : 01/10/11 13:18
// Modified : 24/07/16 12:14
//******************************************************************************
package fr.ligorax.polka;

import java.io.Serializable;

public class RPLComplex extends Object implements Serializable
{

   private double x, y;

   /**
   Constructs the complex number z = u + i*v
   @param u Real part
   @param v Imaginary part
    */
   public RPLComplex(double u, double v)
   {
      x = u;
      y = v;
      System.out.println("U=" + u + " V =" + v);
   }

   /**
   Real part of this RPLComplex number 
   (the x-coordinate in rectangular coordinates).
   @return Re[z] where z is this RPLComplex number.
    */
   public double real()
   {
      return x;
   }

   /**
   Imaginary part of this RPLComplex number 
   (the y-coordinate in rectangular coordinates).
   @return Im[z] where z is this RPLComplex number.
    */
   public double imag()
   {
      return y;
   }

   /**
   Modulus of this RPLComplex number
   (the distance from the origin in polar coordinates).
   @return |z| where z is this RPLComplex number.
    */
   public double mod()
   {
      if (x != 0 || y != 0)
      {
         return Math.sqrt(x * x + y * y);
      } else
      {
         return 0d;
      }
   }

   /**
   Argument of this RPLComplex number 
   (the angle in radians with the x-axis in polar coordinates).
   @return arg(z) where z is this RPLComplex number.
    */
   public double arg()
   {
      return Math.atan2(y, x);
   }

   /**
   RPLComplex conjugate of this RPLComplex number
   (the conjugate of x+i*y is x-i*y).
   @return z-bar where z is this RPLComplex number.
    */
   public RPLComplex conj()
   {
      return new RPLComplex(x, -y);
   }

   /**
   Addition of RPLComplex numbers (doesn't change this RPLComplex number).
   <br>(x+i*y) + (s+i*t) = (x+s)+i*(y+t).
   @param w is the number to add.
   @return z+w where z is this RPLComplex number.
    */
   public RPLComplex plus(RPLComplex w)
   {
      return new RPLComplex(x + w.real(), y + w.imag());
   }

   /**
   Subtraction of RPLComplex numbers (doesn't change this RPLComplex number).
   <br>(x+i*y) - (s+i*t) = (x-s)+i*(y-t).
   @param w is the number to subtract.
   @return z-w where z is this RPLComplex number.
    */
   public RPLComplex minus(RPLComplex w)
   {
      return new RPLComplex(x - w.real(), y - w.imag());
   }

   /**
   RPLComplex multiplication (doesn't change this RPLComplex number).
   @param w is the number to multiply by.
   @return z*w where z is this RPLComplex number.
    */
   public RPLComplex times(RPLComplex w)
   {
      return new RPLComplex(x * w.real() - y * w.imag(), x * w.imag() + y * w.real());
   }

   /**
   Division of RPLComplex numbers (doesn't change this RPLComplex number).
   <br>(x+i*y)/(s+i*t) = ((x*s+y*t) + i*(y*s-y*t)) / (s^2+t^2)
   @param w is the number to divide by
   @return new RPLComplex number z/w where z is this RPLComplex number  
    */
   public RPLComplex div(RPLComplex w)
   {
      double den = Math.pow(w.mod(), 2);
      return new RPLComplex((x * w.real() + y * w.imag()) / den, (y * w.real() - x * w.imag()) / den);
   }

   /**
   RPLComplex exponential (doesn't change this RPLComplex number).
   @return exp(z) where z is this RPLComplex number.
    */
   public RPLComplex exp()
   {
      return new RPLComplex(Math.exp(x) * Math.cos(y), Math.exp(x) * Math.sin(y));
   }

   /**
   Principal branch of the RPLComplex logarithm of this RPLComplex number.
   (doesn't change this RPLComplex number).
   The principal branch is the branch with -pi < arg <= pi.
   @return log(z) where z is this RPLComplex number.
    */
   public RPLComplex log()
   {
      return new RPLComplex(Math.log(this.mod()), this.arg());
   }

   /**
   RPLComplex square root (doesn't change this complex number).
   Computes the principal branch of the square root, which 
   is the value with 0 <= arg < pi.
   @return sqrt(z) where z is this RPLComplex number.
    */
   public RPLComplex sqrt()
   {
      double r = Math.sqrt(this.mod());
      double theta = this.arg() / 2;
      return new RPLComplex(r * Math.cos(theta), r * Math.sin(theta));
   }

   // Real cosh function (used to compute complex trig functions)
   private double cosh(double theta)
   {
      return (Math.exp(theta) + Math.exp(-theta)) / 2;
   }

   // Real sinh function (used to compute complex trig functions)
   private double sinh(double theta)
   {
      return (Math.exp(theta) - Math.exp(-theta)) / 2;
   }

   /**
   Sine of this RPLComplex number (doesn't change this RPLComplex number).
   <br>sin(z) = (exp(i*z)-exp(-i*z))/(2*i).
   @return sin(z) where z is this RPLComplex number.
    */
   public RPLComplex sin()
   {
      return new RPLComplex(cosh(y) * Math.sin(x), sinh(y) * Math.cos(x));
   }

   /**
   Cosine of this RPLComplex number (doesn't change this RPLComplex number).
   <br>cos(z) = (exp(i*z)+exp(-i*z))/ 2.
   @return cos(z) where z is this RPLComplex number.
    */
   public RPLComplex cos()
   {
      return new RPLComplex(cosh(y) * Math.cos(x), -sinh(y) * Math.sin(x));
   }

   /**
   Hyperbolic sine of this RPLComplex number 
   (doesn't change this RPLComplex number).
   <br>sinh(z) = (exp(z)-exp(-z))/2.
   @return sinh(z) where z is this RPLComplex number.
    */
   public RPLComplex sinh()
   {
      return new RPLComplex(sinh(x) * Math.cos(y), cosh(x) * Math.sin(y));
   }

   /**
   Hyperbolic cosine of this RPLComplex number 
   (doesn't change this RPLComplex number).
   <br>cosh(z) = (exp(z) + exp(-z)) / 2.
   @return cosh(z) where z is this RPLComplex number.
    */
   public RPLComplex cosh()
   {
      return new RPLComplex(cosh(x) * Math.cos(y), sinh(x) * Math.sin(y));
   }

   /**
   Tangent of this RPLComplex number (doesn't change this RPLComplex number).
   <br>tan(z) = sin(z)/cos(z).
   @return tan(z) where z is this RPLComplex number.
    */
   public RPLComplex tan()
   {
      return (this.sin()).div(this.cos());
   }

   /**
   Negative of this complex number (chs stands for change sign). 
   This produces a new RPLComplex number and doesn't change 
   this RPLComplex number.
   <br>-(x+i*y) = -x-i*y.
   @return -z where z is this RPLComplex number.
    */
   public RPLComplex chs()
   {
      return new RPLComplex(-x, -y);
   }

   /**
   String representation of this RPLComplex number.
   @return x+i*y, x-i*y, x, or i*y as appropriate.
    */
   @Override
   public String toString()
   {
      return "("+x+","+y+")";
      /*
      if (x != 0 && y > 0)
      {
         return x + " + " + y + "i";
      }
      if (x != 0 && y < 0)
      {
         return x + " - " + (-y) + "i";
      }
      if (y == 0)
      {
         return String.valueOf(x);
      }
      if (x == 0)
      {
         return y + "i";
      }
      // shouldn't get here (unless Inf or NaN)
      return x + " + i*" + y;
       */
   }
}
