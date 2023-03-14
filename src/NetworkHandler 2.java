import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NetworkHandler {
	private static String database = "jdbc:mysql://detris-database.cd6abzr0usev.us-east-1.rds.amazonaws.com/detris";
	private static String user = "DETRIS";
	private static String password = "DetrisUserPassword";
	
	private static int nextID = 0;
	
	private static Connection con;
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void publishScore(Score s) {
		if (s.equals(new Score("",0)))
			return;
		try {
			con = DriverManager.getConnection(database,user,password);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM detris.highscores;");
			while (rs.next()) {
				nextID = rs.getInt(1)+1;
			}
			
			String valueParentheses = "("+nextID+",\""+s.name+"\","+s.score+")";
			PreparedStatement ps = con.prepareStatement("INSERT INTO detris.highscores (id,name,score) VALUES "+valueParentheses+";");
			ps.executeUpdate();

			nextID++;
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return the full list of high scores, sorted descendingly.
	 */
	public static ArrayList<Score> getScores() {
		ArrayList<Score> scores = new ArrayList<Score>();
		try {
			con = DriverManager.getConnection(database,user,password);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from highscores;");
			while (rs.next()) {
				scores.add(new Score(rs.getString(2),rs.getInt(3)));
			}
			
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT MAX(id) FROM detris.highscores;");
			while (rs.next()) {
				nextID = rs.getInt(1)+1;
			}
			
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(scores);
		return scores;
	}
	
	/**
	 * Updates nextID
	 */
	public static void updateNextID() {
		try {
			con = DriverManager.getConnection(database,user,password);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM detris.highscores;");
			while (rs.next()) {
				nextID = rs.getInt(1)+1;
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
