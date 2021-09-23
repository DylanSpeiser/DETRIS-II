import java.util.ArrayList;

public class Score implements Comparable<Score> {
	public String name;
	public int score;
	
	public Score(String name, int score) {
		this.name = name;
		this.score = score;
	}
	
	public int compareTo(Score arg0) {
		return this.score-arg0.score;
	}
	
	public boolean equals(Score other) {
		if (this.name.equals(other.name) && this.score == other.score)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return name+","+score;
	}
	
	public static ArrayList<Score> removeDuplicates(ArrayList<Score> arrayList) {
		ArrayList<Score> returning = new ArrayList<Score>();
		for (int i = 0; i < arrayList.size(); i++) {
			if (!Score.contains(arrayList,arrayList.get(i)))
				returning.add(arrayList.get(i));
		}
		return returning;
	}
	
	public static boolean contains(ArrayList<Score> arrayList, Score test) {
		for (int i = 0; i < arrayList.size(); i++) {
			if (arrayList.get(i).equals(test))
				return true;
		}
		return false;
	}
	
	public static ArrayList<String> format(ArrayList<Score> arrayList) {
		ArrayList<String> returning = new ArrayList<String>();
		for (Score s : arrayList) {
			String dashes = "-----------------";
			int digits = dashes.length()-s.name.length()-(int)(Math.floor(Math.log10(s.score)));
			dashes = dashes.substring(0, digits);
			returning.add(s.name+dashes+s.score);
		}
		return returning;
	}
	
	public String format() {
			String dashes = "-----------------";
			int digits = dashes.length()-this.name.length()-(int)(Math.floor(Math.log10(this.score)));
			dashes = dashes.substring(0, digits);
			return this.name+dashes+this.score;
	}
	
	public static ArrayList<Score> reverseBubbleSort(ArrayList<Score> arrayList) {
	    boolean sorted = false;
	    Score temp;
	    while(!sorted) {
	        sorted = true;
	        for (int i = 0; i < arrayList.size() - 1; i++) {
	            if (arrayList.get(i).score < arrayList.get(i+1).score) {
	                temp = arrayList.get(i);
	                arrayList.set(i,arrayList.get(i+1));
	                arrayList.set(i+1,temp);
	                sorted = false;
	            }
	        }
	    }
		return arrayList;
	}
}
