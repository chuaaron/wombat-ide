/* 
 * License: source-license.txt
 * If this code is used independently, copy the license here.
 */

package wombat.scheme.util;

import java.awt.image.BufferedImage;

import wombat.scheme.libraries.*;
import wombat.scheme.libraries.types.ImageData;
import wombat.scheme.libraries.types.TreeData;
import wombat.util.Base64;

/**
 * Helper that stores all of the interop functions for calling Java methods from Scheme.
 */
public class InteropAPI {
	public static String SchemeCD = null;
	
	private InteropAPI() {}
	
	/**
	 * Interop method.
	 * @param key The method name.
	 * @param val Any parameters.
	 * @return Either null to send nothing or a string to send back.
	 */
	public static String interop(String key, String val) {
		key = key.toLowerCase();
		
		try {
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Image API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			
			if ("read-image".equals(key)) {
				ImageData img = null;
				if (val == null) {
					img = ImageAPI.readImage();
				} else {
					int split = val.indexOf("\" \"");
					String cd = val.substring(1, split);
					String fn = val.substring(split + 3, val.length() - 1);
				
					img = ImageAPI.readImage(cd, fn);
				}
				
				return "(" + img.Width + " " + img.Height + " \"" + Base64.encodeBytes(Conversion.int2byte(img.Data)) + "\")";
			} 
			
			// write images to file
			else if ("write-image".equals(key)) {
				String cd = null;
				if (val.startsWith("\"")) {
					int split = val.indexOf('"', 2);
					cd = val.substring(1, split);
					val = val.substring(split + 2, val.length());
				}
				
				String[] args = val.split(" ");
				ImageData img = new ImageData(
					Integer.parseInt(args[0]),
					Integer.parseInt(args[1]),
					Conversion.byte2int(Base64.decode(args[2]))
				);
				
				if (cd == null)
					ImageAPI.writeImage(img);
				else
					ImageAPI.writeImage(cd, img, args[3].substring(1, args[3].length() - 1));
			}
			
			// display images to the screen
			else if ("draw-image".equals(key)) {
				String[] args = val.split(" ");
				ImageData img = new ImageData(
					Integer.parseInt(args[0]),
					Integer.parseInt(args[1]),
					Conversion.byte2int(Base64.decode(args[2]))
				);
				
				ImageAPI.displayImage(img);
			}
			
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Matrix API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
			else if ("draw-matrix".equals(key)) {
				MatrixAPI.drawMatrix(val);
			}
			
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Tree API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
			else if ("draw-tree".equals(key)) {
				TreeAPI.drawTree(TreeData.decode(val));
			}
			
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Trutle API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
			else if ("draw-turtle".equals(key)) {
				TurtleAPI.drawTurtle(val);
			}
			
			else if ("update-live-timer".equals(key)) {
				TurtleAPI.Pause = Double.parseDouble(val);
			}
			
			else if ("turtle-update".equals(key)) {
				String[] parts = val.split(" ", 2);
				String[] args = parts[1].substring(1, parts[1].length() - 1).split(" ", 2);
				
				TurtleAPI.updateTurtle(parts[0], args[0], args[1].split(" "));
			}
			
			else if ("turtle->image".equals(key)) {
				BufferedImage bi = (BufferedImage) TurtleAPI.turtleToImage(val);
				
				int[] imgdata = new int[bi.getWidth() * bi.getHeight()];
				bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), imgdata, 0, bi.getWidth());
				ImageData img = new ImageData(bi.getWidth(), bi.getHeight(), imgdata);
				
				return "(" + img.Width + " " + img.Height + " \"" + Base64.encodeBytes(Conversion.int2byte(img.Data)) + "\")";
			}
			
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Meta API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
			else if ("set-cd".equals(key)) {
				SchemeCD = val;
			}
			
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
			// Test API
			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
			
			// Test method.
			else if ("fact".equals(key)) {
				int n = Integer.parseInt(val);
				int a = 1;
				for (int i = 2; i <= n; i++) a *= i;
				return "(" + a + ")";
			}
			
			// Unknown method!
			else {
				throw new Exception("Unknown method: " + key);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			return "(exception " + key + " \"" + e.getClass().getSimpleName() + ": " + e.getMessage().replace("\\", "\\\\") + "\")";
		}
		
		return "()";
	}
}
