package mx.unison.pong;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class BallRunner implements Runnable {

    public static final int MAX_X = 640;
    public static final int MAX_Y = 480;
    public static final int SIGN = -1;

    public static final int DX = 8;
    public static final int DY = 8;
    
    private final Ellipse2D.Double ball;
    private final Paddle paddle;

    private int ballX;
    private int ballY;

    private int directionX = 1;
    private int directionY = 1;

    private boolean isPaused;
    private boolean gameOver = false;
    private boolean stop = false;

    private BufferedImage spriteSheet;
    private Image sprite;

    public BallRunner(Ellipse2D.Double shape, Paddle p) {
        super();
        this.ball = shape;
        this.paddle = p;
        resetPosition();
        this.isPaused = false;

        loadSprite();
    }

    public void resetPosition() {
        // Se coloca la bola más abajo (Y = 380, justo por encima de la pala que está en Y = 450)
        this.ballX = 320;
        this.ballY = 380; 
        this.ball.x = ballX;
        this.ball.y = ballY;

        // Sentido horizontal inicial (puede ser 1 o -1 según prefieras)
        this.directionX = 1; 

        // -1 hace que el desplazamiento DY vaya restando posición en Y (movimiento hacia arriba)
        this.directionY = -1; 
        
        this.gameOver = false;
    }

    private void loadSprite() {
        try {
            spriteSheet = ImageIO.read(ClassLoader.getSystemResourceAsStream("ball.png"));
            this.sprite = spriteSheet;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la imagen de la bola: " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void paint(Graphics2D g2d) {
        if (sprite != null) {
            g2d.drawImage(sprite, ballX, ballY, (int) ball.width, (int) ball.height, null);
        } else {
            g2d.fill(ball);
        }
    }

    public synchronized void checkBrickCollision(Brick brick) {
        Rectangle2D.Double bounds = brick.getBounds();
        
        double ballMinX = ball.getMinX();
        double ballMaxX = ball.getMaxX();
        double ballMinY = ball.getMinY();
        double ballMaxY = ball.getMaxY();

        double brickMinX = bounds.getMinX();
        double brickMaxX = bounds.getMaxX();
        double brickMinY = bounds.getMinY();
        double brickMaxY = bounds.getMaxY();

        double overlapLeft   = ballMaxX - brickMinX;
        double overlapRight  = brickMaxX - ballMinX;
        double overlapTop    = ballMaxY - brickMinY;
        double overlapBottom = brickMaxY - ballMinY;

        double minOverlapX = Math.min(overlapLeft, overlapRight);
        double minOverlapY = Math.min(overlapTop, overlapBottom);

        if (minOverlapY < minOverlapX) {
            directionY *= SIGN;
        } else {
            directionX *= SIGN;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public void run() {
        while (!stop) {

            if (isPaused || gameOver) {
                Thread.yield();
                continue;
            }

            if (paddle.intersects(ball)) {
                directionY = directionY * SIGN;
                ballX = ballX + (DX * directionX);
                ballY = ballY + (DY * directionY);
                ball.x = ballX;
                ball.y = ballY;
                continue;
            }

            if (ball.getMinY() < 0) {
                directionY = directionY * SIGN;
            }

            if (ball.getMinX() < 0) {
                directionX = directionX * SIGN;
            }
            
            // Si la bola toca o supera el límite inferior (suelo), es Game Over
            if (ball.getMaxY() >= MAX_Y) {
                gameOver = true;
                continue;
            }

            if (ball.getMaxX() > MAX_X) {
                directionX = directionX * SIGN;
            }

            ballX = ballX + (DX * directionX);
            ballY = ballY + (DY * directionY);
            ball.x = ballX;
            ball.y = ballY;

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        stop = true;
    }
}