package eu.thog92.dramagen;

import eu.thog92.dramagen.util.ArrayListHelper;
import eu.thog92.dramagen.util.WritableArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dictionary {

	private static Dictionary INSTANCE = new Dictionary();
	private HashMap<String, ArrayList<String>> dictionary;
	private File dicDir;

	public void loadCombinaisons() throws IOException {
		this.dictionary = new HashMap<String, ArrayList<String>>();
		System.out.println("Loading Files...");
		for (String file : dicDir.list()) {
			dictionary.put(
					file.replace(".txt", ""),
					ArrayListHelper.loadStringArrayFromFile(dicDir
							.getAbsolutePath() + File.separator + file));
		}
	}

	public ArrayList<String> get(String target) {
		return dictionary.get(target);
	}

	public void reload() {
		try {
			this.loadCombinaisons();
			this.loadBlackList();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDir(File dir) {
		this.dicDir = dir;
	}

	public static Dictionary getInstance() {
		return INSTANCE;
	}

	public void loadBlackList() throws IOException {
		File blackListFile = new File("blacklist.txt");
		if (!blackListFile.exists())
			blackListFile.createNewFile();

		dictionary.put(blackListFile.getName().replace(".txt", ""), new WritableArrayList<String>(ArrayListHelper.loadStringArrayFromFile(blackListFile.getAbsolutePath()), blackListFile));
	}
}
