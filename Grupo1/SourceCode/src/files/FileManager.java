package files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class FileManager {
	
	public static boolean writeObjectToFile(Serializable obj, String fileName) {
		try {
			File file = new File(fileName);
			if(!file.createNewFile()) {
				return false;
			}
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject(obj);

			o.close();
			f.close();
			return true;
			
		} catch (Exception e) {
			// error in opening file
			System.err.println("Error writing object to file");
		}
		return false;
	}
	
	public static Serializable readObjectFromFile(String fileName) {
		try {
			FileInputStream f = new FileInputStream(new File(fileName));
			ObjectInputStream o = new ObjectInputStream(f);

			// Read objects
			Serializable obj = (Serializable) o.readObject();

			o.close();
			f.close();
			return obj;
		} catch (Exception e) {
			// error in reading object
			System.err.println("Error reading object from file");
		}
		return null;
	}
	
	
}
