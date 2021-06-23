import java.awt.event.*;
import java.awt.*;
import java.nio.Buffer;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import javax.swing.Timer;
import java.awt.image.*;
import java.io.*;

public class Game extends JFrame {

        public Game()
        {

            setTitle("Kill Virus");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(800,600);
            setContentPane(new Board());
            pack();
            setLocationRelativeTo(null);

            setVisible(true);
            setResizable(false);

        }

        public static void main(String[] args) {
            new Game();


        }
    }
public class Board extends JPanel implements Runnable, MouseListener, KeyListener, ActionListener{

    boolean ingame = false;
    private Player player;//player
    
    //Dimensions
    public static int WIDTH = 800;
   public static int HEIGHT = 600;
   
   //Graphics and instances
    private BufferedImage image;
    private Graphics2D g;
    int fps = 60;
    public static ArrayList<Bullet> ammo;//Bullets
    public static ArrayList<Enemy> virus;//Enemies
    //Running variables
    int countdown; 
    private Thread thread;
    //Wave variables
    int wave;    
    int spawnDelay = 1000;
    private long waveTimer;
    private long waveDiff;
    private int waveNum;
    private boolean waveStart;
    //Menu
    JButton startButton = new JButton("Start");
    
    //Constructor
    public Board() {
        super();
        addMouseListener(this);
        
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setFocusable(true);
        requestFocus();
        setDoubleBuffered(true);
    }
        //Functions
    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) ;
        {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
        startButton.addActionListener(this);

    }

    
    public void run() {
        while(!ingame) {
        	 
        	 startButton.setBounds(100,200,10,50);
             add(startButton);
             setVisible(true);            
        }
        
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       
        player = new Player();
        ammo = new ArrayList<Bullet>();
        virus = new ArrayList<Enemy>();
        
        //Waves declared
        waveTimer = 0;
        waveDiff = 0;
        waveNum = 0;
        waveStart = true;
        countdown = 30;
        
        long startTime;
        long timeMillis;
        long waitTime;
        long target = 1000 / fps;
        long total = 0;

        int frames = 0;
        int maxFrames = 60;
        double ave;
        while (ingame) {
            startTime = System.nanoTime();
            update();
            render();
            draw();
            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = target - timeMillis;
            try {
            	
                Thread.sleep(waitTime);
               
                
            } catch (Exception e) {

            }
            total += System.nanoTime() - startTime;
            frames++;
            
            if (frames == maxFrames) {
            	//Ensures the frames are consistently at 60fps
            	ave = 1000.0/ ((total/frames)/1000000);
            	System.out.println(ave);
                frames = 0;
                total = 0;
                countdown--;
                if(countdown <= 0) {
          			ingame = false;
          			break;
            }
      	  }
      	 //Game over
      		
        }
    }

    
  private void update() {
	  //Wave Update
	  if(waveTimer == 0 && virus.size() == 0) {
		  waveNum++;
		  player.addScore(countdown/2);
		  waveStart = false;
		  waveTimer = System.nanoTime();
		  countdown = 30;}
	  else {
		  waveDiff = ((System.nanoTime() - waveTimer)/1000000);
		  if(waveDiff > spawnDelay) {
			 waveStart = true;
			  waveTimer = 0;
			  waveDiff = 0;
	        }
		  
	  }
	       

	    
	  //create enemies
	  if(waveStart && virus.size() == 0) {
		  Instantiate();
  }
      //Player Update
      player.update();

      //Bullet Update
      for (int i = 0; i < ammo.size(); i++) {
          boolean remove = ammo.get(i).update();
          if (remove) {
              ammo.remove(i);
              i--;
              
          }
      }
      //Enemy Update
      for (int i = 0; i < virus.size(); i++) {
          virus.get(i).update();
      }
      //Enemy destroy
      for(int i = 0; i < ammo.size(); i++) {
    	Bullet b = ammo.get(i);
    	double bx = b.getX();
    	double by = b.getY();
    	double br = b.getZ();
    	for(int j = 0; j < virus.size(); j++) {
        	Enemy v = virus.get(j);
        	double vx = v.getX();
        	double vy = v.getY();
        	double vr = v.getZ();
        	
        	double dx = bx - vx;
        	double dy = by - vy;
        	double distance = Math.sqrt(dx*dx+dy * dy);
        	if (distance < br+vr) {
        		v.hit();
	        	ammo.remove(i);
	        	i--;
	        	break;
        	}
    	}
        
      }
      //Enemy death conditions
      for (int i = 0; i < virus.size(); i++) {
          if(virus.get(i).isDead()) {
        	  Enemy v = virus.get(i);
        	  player.addScore(v.getRank());//Enemy rank is sent to the 'addscore' subroutine (score added is rank * 10)
        	  virus.remove(i);
        	  i--;
        	  v.spread(v.getX(),v.getY());//If the conditions are met, the virus will split into smaller viruses
          	}
      }
  }
    private void render() {
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        Font small = new Font("Helvetica", Font.BOLD, 14);
        g.setColor(Color.black);
        g.setFont(small);
        g.drawString("Score: " + player.getScore(), 10,10);
        
   	 	g.drawString("Time:" + countdown, 600,10);
   	
        player.draw(g);

        for (int i = 0; i < ammo.size(); i++) {
            ammo.get(i).draw(g);
        }
        for (int i = 0; i < virus.size(); i++) {
            virus.get(i).drawEnemy(g);
        }
       
    }
    private void draw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        }
    //Wave Render
    private void Instantiate() {
    	virus.clear();
    	Enemy v;
    	
    	if(waveNum == 1) {
    		 for(int i = 0; i < 1; i++){
    	            virus.add(new Enemy(1,1));

    	}
    	}
    	if(waveNum == 2) {
   		 for(int i = 0; i < 2; i++){
   	            virus.add(new Enemy(1,1));
   	        }
  		}
    	if(waveNum == 3) {
      		 for(int i = 0; i < 4; i++){
      	            virus.add(new Enemy(1,1));
      	        }
     		}
    	if(waveNum == 4) {
     		 for(int i = 0; i < 8; i++){
     	            virus.add(new Enemy(1,1));
     		 }
    	}
    	if(waveNum == 5) {
    		 for(int i = 0; i < 3; i++){
    	            virus.add(new Enemy(1,2));
    		 }
    	}
    	if(waveNum == 6) {
   		 for(int i = 0; i < 11; i++){
   	            virus.add(new Enemy(1,1));
   		 }
   		 for(int i = 0; i < 3; i++){
   	            virus.add(new Enemy(1,2));
   		 }
   	
    	}
   		if(waveNum == 7) {
   	         for(int i = 0; i < 5; i++){
    	            virus.add(new Enemy(1,1));
    	     
	         }
     		 for(int i = 0; i < 3; i++){
     	            virus.add(new Enemy(1,3));
     		 }
     	}
   	      
   		if(waveNum == 8) {
  	         for(int i = 0; i < 5; i++){
   	            virus.add(new Enemy(1,2));
  	         }
  	         for(int i = 0; i < 2; i++){
  	            virus.add(new Enemy(2,3));
 	         }}
   		if(waveNum == 9) {
 	         for(int i = 0; i < 30; i++){
  	            virus.add(new Enemy(1,1));
 	         }
   		}
   		if(waveNum == 10) {
 	         for(int i = 0; i < 6; i++){
  	            virus.add(new Enemy(1,1));
 	         }
 	      for(int i = 0; i < 4; i++){
	            virus.add(new Enemy(1,2));
	         }
 	     for(int i = 0; i < 2; i++){
	            virus.add(new Enemy(2,3));
	         }
 	    if(waveNum == 11) {
 	    	virus.add(new Enemy(1,4));
 	    }
   		}
  
    }
    //Shooting
    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("haha");
    	player.isFiring(true);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    	player.isFiring(false);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {
    }
//Controls
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT)
        player.setLeft(true);
        if (keyCode == KeyEvent.VK_RIGHT)
        player.setRight(true);
        if (keyCode == KeyEvent.VK_UP)
        player.setUp(true);
        if (keyCode == KeyEvent.VK_DOWN)
        player.setDown(true);
    }
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT)
            player.setLeft(false);
        if (keyCode == KeyEvent.VK_RIGHT)
            player.setRight(false);
        if (keyCode == KeyEvent.VK_UP)
            player.setUp(false);
        if (keyCode == KeyEvent.VK_DOWN)
            player.setDown(false);
        if (keyCode == KeyEvent.VK_SPACE)
            player.isFiring(false);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==startButton){
            ingame = true;
        }
    }
public class Bullet {
    private double x, y;
    private int r;
    private double dx, dy, rad, speed;
    private Color col;

    //Constructor: Calculate Bullet trajectory
    public Bullet(double angle, int x, int y) {
        this.x = x;
        this.y = y;
        r = 5;
        speed = 25;
        rad = Math.toRadians(angle);
        dx = Math.cos(rad) *speed;
        dy = Math.sin(rad) *speed;
        col = Color.blue;

    }

    public void draw(Graphics2D g) {
        g.setColor(col);
        g.fillOval((int) (x-r) ,(int) (y -r),2*r, 2*r);
    }
    public double getX(){return  x;}
    public double getY(){return  y;}
    public double getZ(){return  r;}

    public boolean update() {
        x += dx;
        y += dy;
        if (x < -r || x > Board.WIDTH + r || y < -r || y > Board.HEIGHT + r) {
            return true;
        } else {
            return false;
        }
    }
}

public class Enemy {
    private int type;
     private int rank;
    private double speed,rad;
    private int health;
    private Color col;
    private boolean awake, dead;
    private double x, y, dx, dy;
    private int r;


    public Enemy(int type, int rank) {
    	this.type = type;
    	this.rank = rank;
    	//Basic Enemy
        if (rank == 1) {
            col = Color.red;
                speed = 4;
                r = 12;
                health = 1;
        	}
        //Medium Enemy
        if (rank == 2) {
            col = Color.orange;
            speed = 6;
            r = 24;
            health = 2;
           
        }
        //Medium Enemy
        if (rank == 3) {
            col = Color.GRAY;
             
                speed = 8;
                r = 36;
                health = 5;
                         
            }
        //Medium Enemy
        if (rank == 4) {
            col = Color.BLACK;
             
                speed = 8;
                r = 36;
                health = 50;
                         
            }
        
        if (type == 2)
        	 if (rank == 1) {
                 col = Color.red;
                     speed = 8;
                     r = 12;
                     health = 1;
             	}
        x = Math.random() * Board.WIDTH / 2 + Board.HEIGHT / 4;
        y = -r;
       
        double angle = Math.random() * 140 + 20;
        rad = Math.toRadians(angle);
        dx = Math.cos(rad) * speed;
        dy = Math.sin(rad) * speed;
        awake = false;
        dead = false;
    }
    //Enemy Functions
    public double getX(){return  x;}
    public double getY(){return  y;}
    public double getZ(){return  r;}
   public int getType() {return type;}
   public int getRank() {return rank;}
    boolean isDead(){return dead;}
    public void hit(){
        health--;
        if (health<=0){
            dead = true;
        }
    }
    public void spread(double x, double y) {
    
    int child = 0;
    if(type == 2) {
	    if (rank > 2) {
	    	child = 3;
	    }
	    for(int i = 0; i < child; i++) {
	    	Enemy v = new Enemy(getType(), (getRank() - 2));
	    	Board.virus.add(v);

	    }
	    
    }
    }
    
	public void update(){
        x += dx;
        y += dy;
        if (!awake){
            //if(x > r && x < Board.WIDTH - r && y > r && y < Board.HEIGHT - r){
                awake = true;
            //}
        }
        if(x < r && dx < 0)dx = -dx;
        if(y < r && dy < 0)dy = -dy;
        if(x > Board.WIDTH - r && dx >0) dx = -dx;
        if(y > Board.HEIGHT - r && dy >0) dy = -dy;
    }
    public void drawEnemy(Graphics2D g){
        g.setColor(col);
        g.fillOval((int)(x-r) ,(int)(y-r),2*r, 2*r);
    }

    }



public class Player {
    private int x;
    private int y;
    private int r;

    private int dx;
    private int dy;
    private int speed;	
    private int score;
    private boolean fire;
    private  long fireTimer;
    private  long fireDelay;
    private boolean left,right,up,down;

    public Player() {
        x = Board.WIDTH/2;
        y = Board.HEIGHT/2;
        r = 15;
        score = 0;
        dx = 0;
        dy = 0;
        speed = 8;
        fire = false;
        fireDelay = 200;
        fireTimer = System.nanoTime();
    }
    
    public int getScore() {return score;}
    public void setLeft(boolean b){ left = b;}
    public void setRight(boolean b){ right = b;}
    public void setUp(boolean b){ up = b;}
    public void setDown(boolean b){ down = b;}
    //Firing bullet boolean
    public void isFiring(boolean b) { fire = b;}
    //Score count
    
    public void addScore(int i){score += (i*10);}
   
    public void update() {
        //Directional Movement
        if (left) {
            dx = -speed;
        }
        if (right) {
            dx = speed;
        }
        if (up) {
            dy = -speed;
        }
        if (down) {
            dy = speed;
        }
        x += dx;
        y += dy;
        if (x < r) x = r;
        if (y < r) y = r;
        if (x > Board.WIDTH) x = Board.WIDTH - r;
        if (y > Board.WIDTH) y = Board.HEIGHT - r;
        dx = 0;
        dy = 0;
        if (fire) {
            long elapsedTime = (System.nanoTime() - fireTimer) / 1000000;
            if (elapsedTime > fireDelay) {
                Board.ammo.add(new Bullet(270, x, y));
                fireTimer = System.nanoTime();    
            }
        }
    }

    public void draw(Graphics2D g){
        g.setColor(Color.blue);
        g.fillOval(x-r, y-r, 2*r, 2*r);
    }
	





}
