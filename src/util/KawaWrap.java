package util;

import util.errors.ErrorManager;
import globals.*;

import gnu.kawa.lispexpr.ReadTable;
import gnu.mapping.*;
import kawa.lang.NamedException;
import kawa.standard.Scheme;

/**
 * Wrapper around Kawa.
 */
public class KawaWrap {
	Scheme kawa;
	Environment env;

	/**
	 * Connect to Kawa.
	 */
	public KawaWrap() {
		reset();
	}
	
	/**
	 * Reset the stored Scheme interpreter and environment.
	 */
	public void reset() {
		ReadTable.defaultBracketMode = 0; // allow square brackets
		
		Scheme.registerEnvironment();
		kawa = new Scheme();
		env = kawa.getEnvironment();
		
		// Load globals.
        for (Globals g : new Globals[]{
        		new WDefine(),
        		new WRandom(),
        		new WMath(),
        		new WTree(),
        		new WImage(),
        }) {
        	try {
        		g.addMethods(this);
        	} catch(Throwable ex) {
        		ErrorManager.logError("Unable to load globals from " + g.getClass().getName() + ": " + ex.getMessage());
        	}
        }
	}
	
	/**
	 * Bind a new function from the Java end of things.
	 * 
	 * @param proc The function to bind.
	 */
	public void bind(Named proc) {
		kawa.defineFunction(proc);
	}
	
	/**
	 * Evaluate a command, given as a string.
	 * @param s The string to evaluate.
	 * @return The result.
	 */
	public Object eval(String cmd) {
		try {
			Object result = Scheme.eval(cmd, env);
			
			// Return the final result.
			if (result == null || result.toString().length() == 0)
				return null;
			else 
				return formatObject(result);
		
		} catch (StackOverflowError ex) {
			return "Possible infinite loop detected.";
		
		} catch (UnboundLocationException ex) {
			return "Error: " + ex.getMessage().replace("location", "variable");
		
		} catch (WrongArguments ex) {
			return "Error: " + ex.getMessage();
		
		} catch (IllegalArgumentException ex) {
			return ex.getMessage();
		
		} catch (NamedException ex) {
			return ex.toString();
		
		} catch (WrongType ex) {
			if ("procedure".equals(ex.expectedType.toString()))
				return "Error: Attempted to apply non-procedure '" + ex.argValue + "'";
			else 
				return "Error in " + ex.procname + ": Incorrect argument type. Got " + ex.argValue.getClass().getName() + ", expected " + ex.expectedType.getClass().getName() + ".";
		
		} catch (RuntimeException ex) {
			return "Error: " + ex.getMessage().replace(';', ',').replace("<string>", "<repl>");
		
		} catch (Throwable ex) {
			ex.printStackTrace();
			ErrorManager.logError("Unknown error handled (" + ex.getClass().getName() + "): " + ex.toString());
			return "Error: " + ex.getMessage().replace(';', ',').replace("<string>", "<repl>");
		}
	}
	
	/**
	 * Format an object using Scheme rules.
	 * @param v The object to format.
	 * @return
	 */
	public static String formatObject(Object v) {
		if (v == null)
			return "";
		
		else if (v instanceof String)
			return '"' + ((String) v) + '"';
		
		else if (v instanceof Boolean)
			return ((((Boolean) v).booleanValue()) ? "#t" : "#f");
		
		else if (v instanceof gnu.text.Char)
			return "#\\" + (((gnu.text.Char) v).charValue());
		
		else if (v instanceof gnu.lists.Pair) {
			gnu.lists.Pair p = (gnu.lists.Pair) v;
			
			if (p.getCdr() instanceof gnu.lists.LList) {
				if (((gnu.lists.LList) p.getCdr()).isEmpty())
					return "(" + formatObject(p.getCar()) + ")";
				else
					return "(" + formatObject(p.getCar()) + " " + formatObject(p.getCdr()).substring(1);
			} else
				return "(" + formatObject(p.getCar()) + " . " + formatObject(p.getCdr()) + ")";
		}
		
		else if (v instanceof kawa.lang.Quote)
			return "#<macro quote>";
		else if (v instanceof kawa.lang.Lambda)
			return "#<macro lambda>";
		
		else if (v instanceof gnu.lists.FVector) {
			gnu.lists.FVector vec = (gnu.lists.FVector) v;
			
			StringBuilder sb = new StringBuilder();
			sb.append("#(");
			for (Object o : vec) {
				sb.append(formatObject(o));
				sb.append(" ");
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");
					
			return sb.toString();
		}
		
		else if (v instanceof gnu.expr.ModuleMethod) {
			gnu.expr.ModuleMethod m = (gnu.expr.ModuleMethod) v;
			
			if (m.getName() != null)
				return "#<procedure " + m.getName() + ">";
			else if (m.getSymbol() != null)
				return "#<procedure " + m.getSymbol() + ">";
			else
				return "#<procedure>";
		}
		
		else {
			return v.toString();
		}
			
	}
}
