package eu.thog92.dramagen.task;

import eu.thog92.dramagen.Dictionary;

import java.util.ArrayList;
import java.util.Random;

public class DramaTask implements ITask<String> {

	private Dictionary dictionary;
	private Random rand;

	public DramaTask()
	{
		this.dictionary = Dictionary.getInstance();
		this.rand = new Random();
	}

	@Override
	public String execute() {
		ArrayList<String> dramaGenerated = dictionary.get("blacklist");
		return dramaGenerated.get(rand.nextInt(dramaGenerated.size()));
	}
}
