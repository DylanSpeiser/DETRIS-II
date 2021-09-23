import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class URLReader {
    ArrayList<String> html = new ArrayList<String>();
    
	public URLReader(String url) throws Exception {
		URL link = new URL(url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(link.openStream()));

        int i = 0;
        String inputline;
        while ((inputline = in.readLine()) != null) {
        	html.add(inputline);
        	i++;
        }
        in.close();
	}

	public ArrayList<String> getHTML() {
		return html;
	}
	
	public ArrayList<String> findID(String id) {
		ArrayList<String> returning = new ArrayList<String>();
		boolean inside = false;
		String tag = "";
		String endTag = "";
		for (String s : html) {
			if (!inside) {
				if (s.contains("id=\""+id+"\"")) {
					inside = true;
					returning.add(s);
					tag = s.substring( s.indexOf('<'),s.substring(s.indexOf('<')+1).indexOf('>')+s.indexOf('<')+2);
					endTag = "</"+tag.substring(1,tag.indexOf(" "))+">";
				}
			} else {
				if (s.contains(endTag)) {
					inside = false;
					returning.add(s);
				} else {
					returning.add(s);
				}
			}
			System.out.println((inside ? "*" : "") + "    " + s);
		}
		return returning;
	}
	
	public ArrayList<String> findClass(String classs) {
		ArrayList<String> returning = new ArrayList<String>();
		boolean inside = false;
		String tag = "";
		String endTag = "";
		for (String s : html) {
			if (!inside) {
				if (s.contains("class=\""+classs+"\"")) {
					inside = true;
					returning.add(s);
					tag = s.substring( s.indexOf('<'),s.substring(s.indexOf('<')+1).indexOf('>')+s.indexOf('<')+2);
					endTag = "</"+tag.substring(1,tag.indexOf(" "))+">";
				}
			} else {
				if (s.contains(endTag)) {
					inside = false;
					returning.add(s);
				} else {
					returning.add(s);
				}
			}
			System.out.println((inside ? "*" : "") + "    " + s);
		}
		return returning;
	}
	
	public String findValueFromClass(String classs, String tagType) {
		String returning = "none";
		
		ArrayList<String> classLines = findClass(classs);
		for (String s : classLines) {
			if (s.indexOf(tagType) == s.indexOf('<')+1) {
				
			}
		}
		
		return returning;
	}
}