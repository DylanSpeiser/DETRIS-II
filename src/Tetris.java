import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Tetris extends JFrame implements ActionListener, KeyListener, MouseMotionListener, MouseWheelListener {
	//Swing Stuff:
	TetrisPanel panel = new TetrisPanel();
	Timer renderTimer = new Timer(16,this);
	Timer gravityTimer = new Timer(800,this);
	Timer flashTimer = new Timer(100,this);
	Timer flavorTimer = new Timer(1000,this);
		int flavorAnimationTime = 1000;
	float hueTimer = 0;
	
	//CONSTANTS:
	final static String TITLE = "DETRIS II";
	public int WINDOWWIDTH;
	public int WINDOWHEIGHT;
	public Point rescale;
	final static boolean debug = false;
	final static Point[][] tileCorners = new Point[21][12];
	final static Color[] foregroundColors = {Color.black,new Color(10,212,252),new Color(12,15,240),new Color(240,160,12),new Color(247,228,10),new Color(19,235,33),new Color(201,16,230),new Color(230,28,25),Color.white};
	final static Color[] backgroundColors = {Color.black,new Color(33,165,191),new Color(35,37,158),new Color(181,111,31),new Color(194,180,29),new Color(17,173,27),new Color(164,20,186),new Color(181,24,22),Color.white};
	final static Color undertaleYellow = Color.getHSBColor(56, 94, 94);
	final static ArrayList<Integer> defaultBag = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7));
	BufferedImage[] tetrominoImg = new BufferedImage[8];
    Clip song1;
    Clip song2;
    Clip song3;
    Clip loseSong;
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();

	
	//Fonts
	Font determinationSans;
	Font determinationMono;
	Font helvetica;
	
	//Game Variables
	int gameState = 0;
	int score = 0;
	int offset = 0;
	int speed = 800;
	int selectedMenuItem = 0;
	Point mousePos = new Point(0,0);
	String name = "________";
	int selectedNameChar = 0;
	int[][] board = new int[21][12];
	Tetromino activeTetromino;
	Ghost ghost;
	int typeSelector;
	boolean solidifyNext = false;
	boolean instantDropEnabled = false;
	int heldTetromino;
	ArrayList<Integer> upNext = new ArrayList<Integer>();
	ArrayList<Integer> linesToClear = new ArrayList<Integer>();
	boolean lose = false;
	ArrayList<Score> scores = new ArrayList<>();
	ArrayList<String> scoreStrings = new ArrayList<>();
	boolean handleLinesEnabled = true;
	int tetrisesInARow = 0;
	ArrayList<Integer> bag = defaultBag;
	int stage = 0;
	
	//POSITIONS AND THINGS:
	String[] menuStrings = {"   Play   ","Highscores","   Quit   "};

	public Tetris() {
		renderTimer.start();
		
		//Swing Stuff:
		panel.setBackground(Color.black);
		panel.setVisible(true);
		this.setBackground(Color.black);
		this.setUndecorated(true);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.add(panel);
		this.setTitle(TITLE);
		this.setVisible(true);
		this.setResizable(false);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		GraphicsDevice gd =
	            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(this);
		try {
			this.setIconImage(ImageIO.read((getClass().getClassLoader().getResource("icon.png"))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Fonts
		try {
			InputStream determinationSansStream = loader.getResourceAsStream("determinationsans.ttf");
			determinationSans = Font.createFont(Font.TRUETYPE_FONT, determinationSansStream).deriveFont(75f);
			InputStream determinationMonoStream = loader.getResourceAsStream("DTM-Mono.ttf");
			determinationMono = Font.createFont(Font.TRUETYPE_FONT, determinationMonoStream).deriveFont(75f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, determinationSansStream));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, determinationMonoStream));
		} catch (Exception e) {
			System.out.println("There was a(n) "+e+" error making the font.");
		}
		
		helvetica = new Font("Helvetica", Font.BOLD, 20);
		
		//Images
		for (int i = 1; i<=7; i++) {
			try {
				InputStream imageStream = loader.getResourceAsStream("pics/"+i+".png");
				tetrominoImg[i] =  ImageIO.read(imageStream);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
		
		//Music
	    try {
	    	InputStream song1Stream = loader.getResourceAsStream("songs/song1.wav");
	    	InputStream song2Stream = loader.getResourceAsStream("songs/song2.wav");
	    	InputStream song3Stream = loader.getResourceAsStream("songs/song3.wav");
	    	InputStream loseSongStream = loader.getResourceAsStream("songs/loseSong.wav");
	    	
	    	InputStream song1Buffer = new BufferedInputStream(song1Stream);
	    	InputStream song2Buffer = new BufferedInputStream(song2Stream);
	    	InputStream song3Buffer = new BufferedInputStream(song3Stream);
	    	InputStream loseSongBuffer = new BufferedInputStream(loseSongStream);
	    	
	    	song1 = AudioSystem.getClip();
	    	song1.open(AudioSystem.getAudioInputStream(song1Buffer));
	    	FloatControl song1Volume = (FloatControl)song1.getControl(FloatControl.Type.MASTER_GAIN);
	    	song1Volume.setValue((float)(Math.log(0.3) / Math.log(10.0) * 20.0));
	    	song2 = AudioSystem.getClip();
	    	song2.open(AudioSystem.getAudioInputStream(song2Buffer));
	    	FloatControl song2Volume = (FloatControl)song2.getControl(FloatControl.Type.MASTER_GAIN);
	    	song2Volume.setValue((float)(Math.log(0.3) / Math.log(10.0) * 20.0));
	    	song3 = AudioSystem.getClip();
	    	song3.open(AudioSystem.getAudioInputStream(song3Buffer));
	    	FloatControl song3Volume = (FloatControl)song3.getControl(FloatControl.Type.MASTER_GAIN);
	    	song3Volume.setValue((float)(Math.log(0.4) / Math.log(10.0) * 20.0));
	    	loseSong = AudioSystem.getClip();
	    	loseSong.open(AudioSystem.getAudioInputStream(loseSongBuffer));
	    	FloatControl loseVolume = (FloatControl)loseSong.getControl(FloatControl.Type.MASTER_GAIN);
	    	loseVolume.setValue(1);
	    } catch (Exception e) {
	    	System.err.println(e);
	    }
	    
	    //Scaling
	    WINDOWWIDTH = this.getWidth();
	    WINDOWHEIGHT = this.getHeight();
	    rescale = new Point(this.getWidth()/1920.0, this.getHeight()/1080.0);

		//Game Stuff
		for (int i=0; i<21; i++) {
			for (int j=0; j<12; j++) {
				board[i][j] = 0;
				tileCorners[i][j] = new Point((WINDOWWIDTH/2-200)+(40*(j-1)),(200+(40*(i-1))));
			}
		}
		while (upNext.size() < 4) {
			typeSelector = (int)(Math.random()*7)+1;
			if (!upNext.contains(typeSelector))
				upNext.add(typeSelector);
		}
	}
	
	@SuppressWarnings("unused")
	@Override
	public void keyPressed(KeyEvent arg0) {
		if (gameState == 0) {
			if (arg0.getKeyCode() == KeyEvent.VK_UP && selectedMenuItem > 0)
				selectedMenuItem--;
			if (arg0.getKeyCode() == KeyEvent.VK_DOWN && selectedMenuItem < 2)
				selectedMenuItem++;
			
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				switch (selectedMenuItem) {
				case 0:
					gameState = 1;
					break;
				case 1:
					gameState = 99;
					getHighscores("",0);
					break;
				case 2:
					System.exit(0);
					break;
				}
			}
		} else
		if (gameState == 1) {
			if (arg0.getKeyCode() == KeyEvent.VK_DELETE || arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				selectedNameChar--;
				name=name.substring(0,selectedNameChar)+"_"+name.substring(selectedNameChar+1);
			}
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				name = name.substring(0,selectedNameChar);
				gameState = 2;
				typeSelector = upNext.remove(0);
				activeTetromino = new Tetromino(typeSelector);
				ghost = new Ghost(typeSelector);
				typeSelector = (int)(Math.random()*7)+1;
				upNext.add(3,typeSelector);
				gravityTimer.start();
				ghost.update();
				instantDropEnabled = false;
		    	song1.loop(-1);

				board = new int[21][12];
			}
		}
		if (gameState == 2) {
			if (arg0.getKeyCode() == KeyEvent.VK_DOWN)
				activeTetromino.moveDown(0);
			if (arg0.getKeyCode() == KeyEvent.VK_LEFT)
				activeTetromino.moveLeft(0);
			if (arg0.getKeyCode() == KeyEvent.VK_RIGHT)
				activeTetromino.moveRight(0);
			if (arg0.getKeyCode() == KeyEvent.VK_UP)
				activeTetromino.rotate(0);
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER && instantDropEnabled) {
				while (!activeTetromino.moveDown(1)) {
					activeTetromino.moveDown(0);
				}
				activeTetromino.solidify();
				score+=5;
			}
			if (heldTetromino == 0 && arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
				heldTetromino = activeTetromino.type;
				typeSelector = upNext.remove(0);
				activeTetromino = new Tetromino(typeSelector);
				ghost = new Ghost(typeSelector);
				if (bag.size() > 1) {
					typeSelector = bag.remove((int)(Math.random()*bag.size()));
				} else {
					typeSelector = bag.remove(0);
					for (int i = 1; i < 8; i++) {
						bag.add(i);
					}
				}
				
				upNext.add(3, typeSelector);
				ghost.update();
				instantDropEnabled = false;
			} else if (heldTetromino != 0 && arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
				typeSelector = heldTetromino;
				heldTetromino = activeTetromino.type;
				activeTetromino = new Tetromino(typeSelector);
				ghost = new Ghost(typeSelector);
				ghost.update();
				instantDropEnabled = false;
			}
			
			
			ghost.update();
			
			if (debug && arg0.getKeyCode() == KeyEvent.VK_D)
				System.out.println("Debugging...");
			
			if (debug && arg0.getKeyCode() == KeyEvent.VK_S) {
				ArrayList<Piece> aPiece = new ArrayList<Piece>();
				aPiece.add(new Piece(1,new Point(4,1)));
				activeTetromino = new Tetromino(aPiece);
				ghost = new Ghost(typeSelector);
				ghost.update();
			}
		} else if (gameState == 3) {
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
				reset();
		} else if (gameState == 99) {
			if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				reset();
		}
	}


	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		if (gameState==1 && selectedNameChar<8 && Character.isLetter(arg0.getKeyChar())) {
			name=name.substring(0,selectedNameChar)+arg0.getKeyChar()+name.substring(selectedNameChar+1);
			selectedNameChar++;
		}
	}
	
	
	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousePos.setX(arg0.getX());
		mousePos.setY(arg0.getY());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(renderTimer)) {
			panel.repaint();
			if (gameState == 2) {
				speed = (int)(800-(score/6.23));
				gravityTimer.setDelay(speed);
				
				if (flavorTimer.isRunning() && flavorAnimationTime >= 0)
					flavorAnimationTime--;
				
				if (score >= 1000) {
					if (score >= 3200)
						hueTimer++;
					hueTimer++;
					if (score < 3200) {
						if (!song2.isActive()) {
							song1.stop();
							song2.loop(-1);;
						}
					} else if (score >= 3200) {
						if (!song3.isActive()) {
							song2.stop();
							song3.loop(-1);
						}
					}
				}
			}
		}
		if (arg0.getSource().equals(gravityTimer)) {
			if (activeTetromino.moveDown(1) && solidifyNext)
				activeTetromino.solidify();
			solidifyNext = activeTetromino.moveDown(0);
			
			for (Piece p : activeTetromino.pieces) {
				if (board[(int)p.index.y][(int)p.index.x] != 0) {
					gameState = 3;
					lose = true;
					gravityTimer.stop();
				}
			}
			if (lose) {
				getHighscores(name,score);
				song1.stop();
				song2.stop();
				song3.stop();
				loseSong.start();
			}
			if (!instantDropEnabled)
				instantDropEnabled = true;
		}
		if (arg0.getSource().equals(flashTimer)) {
			if (linesToClear.size() == 0) {
				handleLinesEnabled = true;
				flashTimer.stop();
			} else {
				handleLinesEnabled = false;
				for (int l = linesToClear.remove(linesToClear.size()-1); l>=0; l--) {
					if (l>0) {
						for (int j = 0; j<12; j++) {
							board[l][j] = board[l-1][j];
						}
					} else {
						Arrays.fill(board[l], 0);
					}
				}
				for (int i = 0; i<linesToClear.size(); i++) {
					linesToClear.set(i, linesToClear.get(i)+1);
				}
				ghost.update();
			}
		}
		if (arg0.getSource().equals(flavorTimer)) {
			flavorAnimationTime = 1000;
			flavorTimer.stop();
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (debug)
			offset += arg0.getWheelRotation()*10;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Tetris tetris = new Tetris();
	}
	
	public int handleLines() {
		if (handleLinesEnabled) {
			int count = 0;
			for (int i = 1; i<21; i++) {
				boolean full = true;
				for (int j = 1; j<11; j++) {
					if (board[i][j] == 0)
						full = false;
				}
				if (full) {
					Arrays.fill(board[i], 8);
					flashTimer.start();
					count++;
					if (!linesToClear.contains(i))
						linesToClear.add(i);
				}
			}
			linesToClear.sort(null);
			if (count == 4) {
				tetrisesInARow++;
				score+=25;
				flavorTimer.start();
			} else {
				tetrisesInARow = 0;
			}
			return count;
		}
		return linesToClear.size();
	}
	
	public void getHighscores(String newName, int newScore) {
		try {
			File scoresFile = new File("scores.csv");
			if(!scoresFile.isFile())
				scoresFile.createNewFile();
		
		BufferedReader br = new BufferedReader(new FileReader(scoresFile));
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(",");
		        scores.add(new Score(values[0],Integer.parseInt(values[1])));
		    }
		    br.close();
		    
			if (name.equals("")) {
				name = "NONAME";
				newName = "NONAME";
			}
			if (newName !="" && newScore != 0)
				scores.add(new Score(newName,newScore));
		    
			FileWriter writer = new FileWriter(scoresFile);
			Score.removeDuplicates(scores);
			Score.reverseBubbleSort(scores);
			scoreStrings = Score.format(scores);
			
			for (Score s : scores) {
				writer.write(s+"\n");
			}
			writer.close();
			
		} catch (Exception e) {System.err.println(e);}
	}
	
	public void reset() {
		gameState = 0;
		score = 0;
		offset = 0;
		speed = 800;
		selectedMenuItem = 0;
		mousePos = new Point(0,0);
		name = "________";
		selectedNameChar = 0;
		board = new int[21][12];
		solidifyNext = false;
		heldTetromino = 0;
		upNext = new ArrayList<Integer>();
		linesToClear = new ArrayList<Integer>();
		lose = false;
		scores = new ArrayList<>();
		scoreStrings = new ArrayList<>();
		for (int i = 0; i<4;i++) {
			typeSelector = (int)(Math.random()*7)+1;
			upNext.add(typeSelector);
		}
		loseSong.stop();
		song1.setFramePosition(0);
		song2.setFramePosition(0);
		song3.setFramePosition(0);
		loseSong.setFramePosition(0);
		
		instantDropEnabled = false;
	}
	
	public class TetrisPanel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
				super.paintComponent(g);
			
			if (gameState == 0) {
				try {
				//Menu Screen
				g.setFont(determinationSans.deriveFont(100f));
				g.setColor(Color.white);
				g.drawString(TITLE,(WINDOWWIDTH/2)-(int)((g.getFontMetrics(determinationSans.deriveFont(100f)).getStringBounds(TITLE, g).getWidth()/2)),90);
				g.setFont(determinationSans.deriveFont(50f));
				g.drawString("A wonderful experience", (WINDOWWIDTH/2)-(int)((g.getFontMetrics(determinationSans.deriveFont(50f)).getStringBounds("A wonderful experience", g).getWidth()/2)),150);
				g.drawString("by Dylan Speiser", (WINDOWWIDTH/2)-(int)((g.getFontMetrics(determinationSans.deriveFont(50f)).getStringBounds("by Dylan Speiser", g).getWidth()/2)),200);
				
				g.setFont(determinationMono);
				for (int i=0; i<menuStrings.length; i++) {
					if (i==selectedMenuItem) {
						g.setColor(undertaleYellow);
					} else {
						g.setColor(Color.white);
					}
						g.drawString(menuStrings[i], (int) ((WINDOWWIDTH/2)-((g.getFontMetrics(determinationMono).getStringBounds(menuStrings[i], g).getWidth()/2))), (int)((450+(i*200))*rescale.y));
				}
				} catch (Exception e) {}
			} else
			if (gameState == 1) {
				g.setColor(Color.white);
				g.setFont(determinationSans.deriveFont(60f));
				g.drawString("Please enter your name:", (int) ((WINDOWWIDTH/2)-((g.getFontMetrics(g.getFont()).getStringBounds("Please enter your name:", g).getWidth()/2))),(int)(350*rescale.y));
				
				g.setFont(determinationSans.deriveFont(90f));
				
			for (int i=0; i<name.length();i++) {
				if (i==selectedNameChar) {
					g.setColor(undertaleYellow);
				} else {
				g.setColor(Color.white);
				}
				g.drawString(name.charAt(i)+"",(int)((WINDOWWIDTH/2)-195+(i*50)+Math.random()*5*(rescale.x)), (int)(500+Math.random()*5*rescale.y));
			}
			g.setFont(determinationSans.deriveFont(50f));
			if (selectedNameChar == 8) {
				g.setColor(undertaleYellow);
			} else {
				g.setColor(Color.white);
			}
				g.drawString("Press ENTER when you are happy with your name.", (int) ((WINDOWWIDTH/2)-((g.getFontMetrics(g.getFont()).getStringBounds("Press ENTER when you are happy with your name.", g).getWidth()/2))),(int)(700*rescale.y));
			} else
			if (gameState >= 2 && gameState != 99) {
				//Main Board
				Color borderColor;
				
				if (score >= 1000) {
					borderColor = Color.getHSBColor((hueTimer%100)/100, 0.95f, 0.95f);
				} else {
					borderColor = Color.white;
				}
				
				g.setColor(borderColor);
				g.fillRect((WINDOWWIDTH/2-205),195,410,810);
				g.setColor(Color.black);
				g.fillRect((WINDOWWIDTH/2-200),200,400,800);
				
				//Name and Score Box
				g.setColor(borderColor);
				g.fillRect((WINDOWWIDTH/2-400), 195, 200, 140);
				g.setColor(Color.black);
				g.fillRect((WINDOWWIDTH/2-395), 200, 190, 130);
				
				g.setColor(borderColor.brighter().brighter());
				g.setFont(determinationMono.deriveFont(35f));
				g.drawString(name, (int)(((WINDOWWIDTH/2-400)+(WINDOWWIDTH/2-200))/2-((g.getFontMetrics(g.getFont()).getStringBounds(name, g)).getWidth()/2)), 250);
				g.drawString(score+"", (int)(((WINDOWWIDTH/2-400)+(WINDOWWIDTH/2-200))/2-((g.getFontMetrics(g.getFont()).getStringBounds(score+"", g)).getWidth()/2)), 300);
				
				//Hold Box
				g.setColor(borderColor);
				g.fillRect((WINDOWWIDTH/2-400), 805, 200, 200);
				g.setColor(Color.black);
				g.fillRect((WINDOWWIDTH/2-395), 810, 190, 190);
				g.setFont(determinationSans.deriveFont(35f));
				g.setColor(borderColor.brighter().brighter());
				g.drawString("HOLD", (int)(((WINDOWWIDTH/2-400)+(WINDOWWIDTH/2-200))/2-((g.getFontMetrics(g.getFont()).getStringBounds("HOLD", g)).getWidth()/2)), 855);
				
				//Next Piece Box
				g.setColor(borderColor);
				g.fillRect((WINDOWWIDTH/2+200), 195, 200, 540);
				g.setColor(Color.black);
				g.fillRect((WINDOWWIDTH/2+205), 200, 190, 530);
				g.setColor(borderColor.brighter().brighter());
				g.drawString("NEXT", (int)(((WINDOWWIDTH/2+200)+(WINDOWWIDTH/2+400))/2-((g.getFontMetrics(g.getFont()).getStringBounds("NEXT", g)).getWidth()/2)), 250);
				
				for (int i=1; i<21; i++) {
					for (int j=1; j<11; j++) {
						g.setColor(backgroundColors[board[i][j]]);
						g.fillRect((int)tileCorners[i][j].x, (int)tileCorners[i][j].y, 40, 40);
						g.setColor(foregroundColors[board[i][j]]);
						g.fillRect((int)tileCorners[i][j].x+4, (int)tileCorners[i][j].y+3, 34, 34);
					}
				}
				//Ghost Drawing
				g.setColor(backgroundColors[ghost.type]);
				for (Piece p : ghost.pieces) {
					g.fillRect((int)tileCorners[(int)p.index.y][(int)p.index.x].x, (int)tileCorners[(int)p.index.y][(int)p.index.x].y, 40, 40);
				}
				g.setColor(Color.black);
				for (Piece p : ghost.pieces) {
					g.fillRect((int)tileCorners[(int)p.index.y][(int)p.index.x].x+3, (int)tileCorners[(int)p.index.y][(int)p.index.x].y+3, 34, 34);
				}
				
				//Active Tetromino drawing
				g.setColor(backgroundColors[activeTetromino.type]);
				for (Piece p : activeTetromino.pieces) {
					g.fillRect((int)tileCorners[(int)p.index.y][(int)p.index.x].x, (int)tileCorners[(int)p.index.y][(int)p.index.x].y, 40, 40);
				}
				g.setColor(foregroundColors[activeTetromino.type]);
				for (Piece p : activeTetromino.pieces) {
					g.fillRect((int)tileCorners[(int)p.index.y][(int)p.index.x].x+3, (int)tileCorners[(int)p.index.y][(int)p.index.x].y+3, 34, 34);
				}
				
				//Flavor Text
				if (flavorTimer.isRunning()) {
					g.setColor(Color.cyan);
					g.setFont(determinationSans.deriveFont(30f));
						String[] places = {"","DOUBLE ","TRIPLE ","QUADRUPLE ","QUINTUPLE "};
						String tetrisFlavorString;
						if (tetrisesInARow <= 4) {
							tetrisFlavorString = places[tetrisesInARow-1]+"TETRIS!";
						} else {
							tetrisFlavorString = "ANOTHER TETRIS!";
						}
					g.drawString(tetrisFlavorString, (int) (rescale.x*720-(g.getFontMetrics(determinationSans.deriveFont(30f)).getStringBounds(tetrisFlavorString, g)).getWidth()), 140+(flavorAnimationTime/2));
				}
				
				//Hold Image
				g.drawImage(tetrominoImg[heldTetromino], (WINDOWWIDTH/2-370), 885, 140, 90, this);
				
				//Up Next Images
				for (int i = 0; i<upNext.size(); i++) {
					g.drawImage(tetrominoImg[upNext.get(i)], (WINDOWWIDTH/2+230), 280+(110*i), 140, 90, this);
				}
			}
			if (gameState == 3) {
				g.setColor(new Color(0,0,0,127));
				g.fillRect(0, 0, WINDOWWIDTH, WINDOWHEIGHT);
				
				g.setColor(Color.white);
				g.fillRect((WINDOWWIDTH/2-205),195,410,5);
				g.fillRect((WINDOWWIDTH/2-205),195,5,810);
				g.fillRect((WINDOWWIDTH/2-205),195+805,410,5);
				g.fillRect((WINDOWWIDTH/2-205)+405, 195, 5, 810);
				
				g.setColor(Color.white);
				g.setFont(determinationSans.deriveFont(40f));
				g.drawString("HIGHSCORES", (int)(WINDOWWIDTH/2 - ((g.getFontMetrics(determinationSans.deriveFont(40f)).getStringBounds("HIGHSCORES", g)).getWidth()/2)), 240);
				
				g.setFont(determinationMono.deriveFont(35f));
				for (int i = 0; i < Math.min(scoreStrings.size(),18); i++) {
					if (scoreStrings.get(i).equals(new Score(name,score).format())) {
						g.setColor(undertaleYellow);
					} else {
						g.setColor(Color.white);
					}
					g.drawString(scoreStrings.get(i), (int)(WINDOWWIDTH/2 - ((g.getFontMetrics(g.getFont()).getStringBounds(scoreStrings.get(0), g)).getWidth()/2)), 290+(i*40));
				}
			} 
			if (gameState == 99) {
				g.setColor(new Color(0,0,0,127));
				g.fillRect(0, 0, WINDOWWIDTH, WINDOWHEIGHT);
				
				g.setColor(Color.white);
				g.fillRect((WINDOWWIDTH/2-785),145,1510,830);
				g.setColor(Color.black);
				g.fillRect((WINDOWWIDTH/2-780),150,1500,820);
				
				g.setColor(Color.white);
				g.setFont(determinationSans.deriveFont(40f));
				g.drawString("HIGHSCORES", (int)(WINDOWWIDTH/2 - ((g.getFontMetrics(determinationSans.deriveFont(40f)).getStringBounds("HIGHSCORES", g)).getWidth()/2)), 190);
				
				g.setFont(determinationMono.deriveFont(35f));
				g.setColor(Color.white);
				int scoresPerRow = 19;
				int place = 1;
				for (int i = 0; i < Math.min(scoreStrings.size(),scoresPerRow); i++) {
					if (place < 10) {
						g.drawString(place+".  "+scoreStrings.get(i), (int)(WINDOWWIDTH/2 -500 -60*rescale.x - ((g.getFontMetrics(g.getFont()).getStringBounds(scoreStrings.get(0)+" ", g)).getWidth()/2)), 230+(i*40));
					} else {
						g.drawString(place+". "+scoreStrings.get(i), (int)(WINDOWWIDTH/2 -500 -60*rescale.x - ((g.getFontMetrics(g.getFont()).getStringBounds(scoreStrings.get(0)+" ", g)).getWidth()/2)), 230+(i*40));
					}
					place++;
				}
				if (scoreStrings.size() > scoresPerRow) {
					for (int i = 0; i < Math.min(scoreStrings.size()-scoresPerRow,scoresPerRow); i++) {
						g.drawString(place+". "+scoreStrings.get(scoresPerRow+i), (int)(WINDOWWIDTH/2 -60*rescale.x - ((g.getFontMetrics(g.getFont()).getStringBounds(scoreStrings.get(0)+" ", g)).getWidth()/2)), 230+(i*40));
						place++;
					}
					if (scoreStrings.size() > 2 * scoresPerRow) {
						for (int i = 0; i < Math.min(scoreStrings.size()-(2*scoresPerRow),scoresPerRow); i++) {
							g.drawString(place+". "+scoreStrings.get(2*scoresPerRow+i), (int)(WINDOWWIDTH/2 +500 -60*rescale.x - ((g.getFontMetrics(g.getFont()).getStringBounds(scoreStrings.get(0)+" ", g)).getWidth()/2)), 230+(i*40));
							place++;
						}
					}
				}
				g.drawString("Press BACKSPACE to return to the menu.", (int) ((WINDOWWIDTH/2)-((g.getFontMetrics(g.getFont()).getStringBounds("Press BACKSPACE to return to the menu.", g).getWidth()/2))),(int)(1040*rescale.y));
			}
			
			if (debug) {
				g.setColor(Color.cyan);
				g.setFont(helvetica);
				g.drawString(mousePos.x+" "+mousePos.y, 4, 22);
				g.drawString("Offset: "+offset, 4, 44);
				g.drawString("Rescale: "+rescale, 4, 264);
				g.drawString("Window Dimensions: "+this.getParent().getWidth()+" x "+this.getParent().getHeight(), 4, 284);
				
				g.drawLine((WINDOWWIDTH/2),0,(WINDOWWIDTH/2),WINDOWHEIGHT);
				g.drawLine(0,(WINDOWHEIGHT/2),WINDOWWIDTH,(WINDOWHEIGHT/2));
				
				if (gameState == 1) {
					g.drawString("SelectedNameChar: "+selectedNameChar, 4, 64);
				} else
				if (gameState == 2) {
					for (int i=0; i<tileCorners.length; i++) {
						for (int j=0; j<tileCorners[0].length; j++) {
							g.fillOval((int)tileCorners[i][j].x-2, (int)tileCorners[i][j].y-2, 4, 4);
							g.drawRect((int)tileCorners[i][j].x, (int)tileCorners[i][j].y, 40, 40);
						}
					}
					g.drawString("CollisionBottom: "+activeTetromino.moveDown(1), 4, 64);
					g.drawString("CollisionLeft: "+activeTetromino.moveLeft(1), 4, 84);
					g.drawString("CollisionRight: "+activeTetromino.moveRight(1), 4, 104);
					g.drawString("Up Next: "+upNext.toString(), 4, 124);
					g.drawString("LineHandler: "+handleLines(), 4, 144);
					g.drawString("Held Piece: "+heldTetromino, 4, 164);
					g.drawString("FlashTimer: Running: "+flashTimer.isRunning()+" Delay: "+flashTimer.getDelay(), 4, 184);
					g.drawString("LinesToClear: "+linesToClear+" ("+linesToClear.size()+")", 4, 204);
					g.drawString("Bag: "+bag+" ("+bag.size()+")", 4, 224);
					g.drawString("HueTimer: "+hueTimer, 4, 244);
					
					for (int i = 0; i<activeTetromino.pieces.size(); i++) {
						g.drawString(i+"", (int)tileCorners[(int)activeTetromino.pieces.get(i).index.y][(int)activeTetromino.pieces.get(i).index.x].x+15, (int)tileCorners[(int)activeTetromino.pieces.get(i).index.y][(int)activeTetromino.pieces.get(i).index.x].y+30);
					}
				}
			}
		}
	}
	
	public class Piece {
		public int type;
		public Point index;
		public int rotationIndex;
		
		public Piece(int type, Point index) {
			this.type = type;
			this.index = index;
		}
		
		@Override
		public String toString() {
			return "type "+type+" @"+(int)index.x+","+(int)index.y;
		}
	}
	
	public class Tetromino {
		protected ArrayList<Piece> pieces = new ArrayList<Piece>();
		public int type;
		public int rotationState = 0;

		public Tetromino(int type) {
			this.type = type;
			switch (type) {
			case 1:
				pieces.add(new Piece(type,new Point(4,1)));
				pieces.add(new Piece(type,new Point(5,1)));
				pieces.add(new Piece(type,new Point(6,1)));
				pieces.add(new Piece(type,new Point(7,1)));
				break;
			case 2:
				pieces.add(new Piece(type,new Point(4,1)));
				pieces.add(new Piece(type,new Point(4,2)));
				pieces.add(new Piece(type,new Point(5,2)));
				pieces.add(new Piece(type,new Point(6,2)));
				break;
			case 3:
				pieces.add(new Piece(type,new Point(6,1)));
				pieces.add(new Piece(type,new Point(4,2)));
				pieces.add(new Piece(type,new Point(5,2)));
				pieces.add(new Piece(type,new Point(6,2)));
				break;
			case 4:
				pieces.add(new Piece(type,new Point(5,1)));
				pieces.add(new Piece(type,new Point(5,2)));
				pieces.add(new Piece(type,new Point(6,1)));
				pieces.add(new Piece(type,new Point(6,2)));
				break;
			case 5:
				pieces.add(new Piece(type,new Point(5,1)));
				pieces.add(new Piece(type,new Point(6,1)));
				pieces.add(new Piece(type,new Point(5,2)));
				pieces.add(new Piece(type,new Point(4,2)));
				break;
			case 6:
				pieces.add(new Piece(type,new Point(4,1)));
				pieces.add(new Piece(type,new Point(6,1)));
				pieces.add(new Piece(type,new Point(5,1)));
				pieces.add(new Piece(type,new Point(5,2)));
				break;
			case 7:
				pieces.add(new Piece(type,new Point(4,1)));
				pieces.add(new Piece(type,new Point(5,1)));
				pieces.add(new Piece(type,new Point(5,2)));
				pieces.add(new Piece(type,new Point(6,2)));
				break;	
			}
		}
		
		public Tetromino(ArrayList<Piece> thePieces) {
			pieces = thePieces;
			type = pieces.get(0).type;
		}
		
		public boolean moveDown(int check) {
			boolean collisionDetector = false;
			for (Piece p : pieces) {
				if (p.index.y == 20) {
					collisionDetector = true;
				}
				else if (board[(int) (p.index.y+1)][(int) p.index.x] != 0) {
					collisionDetector = true;
				}
			}
			if (collisionDetector && check == 0)
				solidify();
			if (!collisionDetector && check == 0) {
				for (int i=0; i<pieces.size(); i++) {
					pieces.get(i).index.y++;
				}
				score++;
			}
			return collisionDetector;
		}
		
		public boolean moveLeft(int check) {
			boolean collisionDetector = false;
			for (Piece p : pieces) {
				if (p.index.x == 1) {
					collisionDetector = true;
				}
				else if (board[(int) (p.index.y)][(int) p.index.x-1] != 0) {
					collisionDetector = true;
				}
			}
			if (!collisionDetector && check == 0) {
				for (int i=0; i<pieces.size(); i++) {
					pieces.get(i).index.x--;
				}
			}
			return collisionDetector;
		}
		
		public boolean moveRight(int check) {
			boolean collisionDetector = false;
			for (Piece p : pieces) {
				if (p.index.x == 10) {
					collisionDetector = true;
				}
				else if (board[(int) (p.index.y)][(int) p.index.x+1] != 0) {
					collisionDetector = true;
				}
			}
			if (!collisionDetector && check == 0) {
				for (int i=0; i<pieces.size(); i++) {
					pieces.get(i).index.x++;
				}
			}
			return collisionDetector;
		}
		
		public void rotate(int direction) {
			// 0 is clockwise, 1 is counterclockwise
			if (type == 1 && Math.min(Math.min(Math.min(pieces.get(0).index.y, pieces.get(1).index.y), pieces.get(2).index.y), pieces.get(3).index.y) >= 1) {
				int[][] rotationMatrixX = {
						{ 2, 1,-2,-1},
						{ 1, 0,-1, 0},
						{ 0,-1, 0, 1},
						{-1,-2, 1, 2}
				};
				
				int[][] rotationMatrixY = {
						{-1, 2, 1,-2},
						{ 0, 1, 0,-1},
						{ 1, 0,-1, 0},
						{ 2,-1,-2, 1}
				};
				
				for (int i=0; i<pieces.size(); i++) {
					pieces.get(i).index.x+=rotationMatrixX[i][rotationState];
					pieces.get(i).index.y+=rotationMatrixY[i][rotationState];
				}
				
			} else if (type != 4 && type != 1) {
				int[] rotationMatrixX = { 2, 1, 0,
										  1, 0,-1,
										  0,-1,-2, };
				
				int[] rotationMatrixY = { 0, 1, 2,
										 -1, 0, 1,
										 -2,-1, 0, };
				
				int count = 0;
				for (int y = -1; y<=1; y++) {
					for (int x = -1; x<=1; x++) {
						for (int p = 0; p<pieces.size(); p++) {
							if (pieces.get(p).index.x == (int)pieces.get(2).index.x+x && pieces.get(p).index.y == (int)pieces.get(2).index.y+y)
								pieces.get(p).rotationIndex = count;
						}
						count++;
					}
				}
				for (int rM = 0; rM<rotationMatrixX.length; rM++) {
					for(int p = 0; p<pieces.size(); p++) {
						if (pieces.get(p).rotationIndex == rM) {
							pieces.get(p).index.x+=rotationMatrixX[rM];
							pieces.get(p).index.y+=rotationMatrixY[rM];
						}
					}
				}
			}
			if (direction == 0) {
				rotationState++;
			} else {
				rotationState--;
			}
			if (rotationState >= 4)
				rotationState = 0;
			if (!isValid() && direction == 0) {
				rotate(1);
				rotate(1);
				rotate(1);
			}
		}
		
		public void solidify() {
			for (Piece p : pieces) {
				board[(int)p.index.y][(int)p.index.x] = type;
			}
			if (!lose) {
				typeSelector = upNext.remove(0);
				activeTetromino = new Tetromino(typeSelector);
				ghost = new Ghost(typeSelector);
				if (bag.size() > 1) {
					typeSelector = bag.remove((int)(Math.random()*bag.size()));
				} else {
					typeSelector = bag.remove(0);
					for (int i = 1; i < 8; i++) {
						bag.add(i);
					}
				}
				upNext.add(3, typeSelector);
				ghost.update();
				
				if (handleLines() != 4)
					tetrisesInARow = 0;
			}
		}
		
		public boolean isValid() {
			for (Piece p : pieces) {
				if (p.index.x < 1 || p.index.x > 11)
					return false;
				if (p.index.y < 1 || p.index.x > 21)
					return false;
				if (board[(int)p.index.y][(int)p.index.x] != 0)
					return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "type "+type;
		}
	}
	
	public class Ghost extends Tetromino {

		public Ghost(int type) {
			super(type);
		}
		
		public boolean moveDown() {
			boolean collisionDetector = false;
			for (Piece p : pieces) {
				if (p.index.y == 20) {
					collisionDetector = true;
				}
				else if (board[(int) (p.index.y+1)][(int) p.index.x] != 0) {
					collisionDetector = true;
				}
			}
			if (!collisionDetector) {
				for (int i=0; i<pieces.size(); i++) {
					pieces.get(i).index.y++;
				}
			}
			return collisionDetector;
		}
		
		public void update() {
			for (int i = 0; i<pieces.size(); i++) {
				pieces.get(i).index.x = activeTetromino.pieces.get(i).index.x;
				pieces.get(i).index.y = activeTetromino.pieces.get(i).index.y;
			}
			for (int i=0; i<22; i++) {
				moveDown();
			}
		}
	}
}
