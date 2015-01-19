package eu.thog92.dramagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import eu.thog92.dramagen.util.ArrayListHelper;

public class Dictionary {

	private static Dictionary INSTANCE = new Dictionary();
	private HashMap<String, ArrayList<String>> dictionary;
	private File dicDir;

	public void loadCombinaisons() throws IOException {
		this.dictionary = new HashMap<String, ArrayList<String>>();
		for (String file : dicDir.list()) {
			dictionary.put(
					file.replaceAll(".txt", ""),
					ArrayListHelper.loadStringArrayFromFile(dicDir
							.getAbsolutePath() + File.separator + file));
		}
	}
	
	public ArrayList<String> get(String target)
	{
		return dictionary.get(target);
	}

	public void reload() {
		try {
			this.loadCombinaisons();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDir(File dir)
	{
		this.dicDir = dir;
	}
	
	public static Dictionary getInstance()
	{
		return INSTANCE;
	}
}
