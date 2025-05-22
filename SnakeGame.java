import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;
    private Image appleImage;
    private Font scoreFont;
    private Font gameOverFont;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(new Color(30, 30, 30));
        addKeyListener(this);
        setFocusable(true);

        //apple
        appleImage = createAppleImage();

        //fonts
        scoreFont = new Font("Segoe UI", Font.BOLD, 18);
        gameOverFont = new Font("Segoe UI", Font.BOLD, 36);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;
        
        //game speed
        gameLoop = new Timer(150, this);
        gameLoop.start();
    }    
    
    private Image createAppleImage() {
        BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        //draw apple
        g2d.setColor(Color.RED);
        g2d.fillOval(2, 2, tileSize-4, tileSize-4);
        g2d.setColor(new Color(100, 70, 0));
        g2d.fillRect(tileSize/2-1, 0, 3, 5);
        g2d.setColor(Color.GREEN);
        g2d.fillOval(tileSize/2+2, 2, 8, 5);
        
        g2d.dispose();
        return img;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
        //snake head
        g2d.setColor(new Color(50, 180, 50)); // Solid green head
        g2d.fillRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize);
        
        //snake body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            int greenValue = 150 - (i * 100 / Math.max(1, snakeBody.size()));
            greenValue = Math.max(50, greenValue); 
            g2d.setColor(new Color(0, greenValue, 0));
            g2d.fillRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize);
        }

        //Draw apple
        g.drawImage(appleImage, food.x*tileSize, food.y*tileSize, tileSize, tileSize, this);

        //score
        g.setFont(scoreFont);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + snakeBody.size(), 10, 20);

        //game Over
        if (gameOver) {
            g.setFont(gameOverFont);
            g.setColor(new Color(200, 50, 50));
            
            String gameOverText = "GAME OVER";
            String scoreText = "Score: " + snakeBody.size();
            String restartText = "Press SPACE to restart";
            
            int gameOverWidth = g.getFontMetrics().stringWidth(gameOverText);
            int scoreWidth = g.getFontMetrics(scoreFont).stringWidth(scoreText);
            int restartWidth = g.getFontMetrics(scoreFont).stringWidth(restartText);
            
            g.drawString(gameOverText, (boardWidth - gameOverWidth)/2, boardHeight/2 - 40);
            
            g.setFont(scoreFont);
            g.drawString(scoreText, (boardWidth - scoreWidth)/2, boardHeight/2);
            g.drawString(restartText, (boardWidth - restartWidth)/2, boardHeight/2 + 40);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
        
        //making sure food doesn't spawn on snake
        boolean onSnake = true;
        while (onSnake) {
            onSnake = false;
            if (food.x == snakeHead.x && food.y == snakeHead.y) {
                onSnake = true;
            }
            for (Tile part : snakeBody) {
                if (food.x == part.x && food.y == part.y) {
                    onSnake = true;
                    break;
                }
            }
            if (onSnake) {
                food.x = random.nextInt(boardWidth/tileSize);
                food.y = random.nextInt(boardHeight/tileSize);
            }
        }
    }

    public void move() {
        //eat food mmm
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        //move snak
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //conditions for game over
        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        if (snakeHead.x < 0 || snakeHead.x >= boardWidth/tileSize || 
            snakeHead.y < 0 || snakeHead.y >= boardHeight/tileSize) {
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public void resetGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        placeFood();
        velocityX = 1;
        velocityY = 0;
        gameOver = false;
        gameLoop.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
        }
        repaint();
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            resetGame();
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (velocityY != 1) {
                    velocityX = 0;
                    velocityY = -1;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (velocityY != -1) {
                    velocityX = 0;
                    velocityY = 1;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (velocityX != 1) {
                    velocityX = -1;
                    velocityY = 0;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (velocityX != -1) {
                    velocityX = 1;
                    velocityY = 0;
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame(800, 600);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}