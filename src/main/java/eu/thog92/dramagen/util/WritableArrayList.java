package eu.thog92.dramagen.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class WritableArrayList<E> extends ArrayList<E> {

	private File file;

	public WritableArrayList(Collection<? extends E> c, File file) {
		super(c);
		this.file = file;
	}

	private static final long serialVersionUID = 7959171795250720924L;

	@SuppressWarnings("unchecked")
	public boolean addAndWrite(String s) {
		boolean result = super.add((E) s);
		if (result) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(file.getAbsolutePath(),
						true));
				s += "\n";
				out.write(s, 0, s.length());
				out.flush();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}
}
