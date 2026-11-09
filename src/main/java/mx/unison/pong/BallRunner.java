package mx.unison.pong;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import sfx.SFXManager;

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

    private final SFXManager sfx = SFXManager.getInstance();
    // --- Control de Estela ---
    private final LinkedList<Point> trail = new LinkedList<>();
    private static final int MAX_TRAIL_SIZE = 6;

    public BallRunner(Ellipse2D.Double shape, Paddle p) {
        super();
        this.ball = shape;
        this.paddle = p;
        resetPosition();
        this.isPaused = false;

        loadSprite();
    }

    public void resetPosition() {
        this.ballX = 320;
        this.ballY = 380;
        this.ball.x = ballX;
        this.ball.y = ballY;
        this.directionX = 1;
        this.directionY = -1;
        this.gameOver = false;
        this.trail.clear();
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
        Composite originalComposite = g2d.getComposite();

        // 1. Dibujar la estela (posiciones anteriores con alpha decreciente)
        for (int i = 0; i < trail.size(); i++) {
            Point p = trail.get(i);
            float alpha = (float) (i + 1) / (trail.size() + 2); // Transparencia progresiva
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));

            if (sprite != null) {
                g2d.drawImage(sprite, p.x, p.y, (int) ball.width, (int) ball.height, null);
            } else {
                g2d.fillOval(p.x, p.y, (int) ball.width, (int) ball.height);
            }
        }

        // Restaurar opacidad original para la bola principal
        g2d.setComposite(originalComposite);

        // 2. Dibujar la bola actual
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

            // Registrar posición actual en la estela antes de mover
            trail.add(new Point(ballX, ballY));
            if (trail.size() > MAX_TRAIL_SIZE) {
                trail.removeFirst();
            }

            if (paddle.intersects(ball)) {
                directionY = directionY * SIGN;
                ballX = ballX + (DX * directionX);
                ballY = ballY + (DY * directionY);
                ball.x = ballX;
                ball.y = ballY;
                
            sfx.playSound("hit");
                continue;
            }

            if (ball.getMinY() < 0) {
                directionY = directionY * SIGN;
                sfx.playSound("hit");
            }

            if (ball.getMinX() < 0) {
                directionX = directionX * SIGN;
                sfx.playSound("hit");
            }
            
            if (ball.getMaxY() >= MAX_Y) {
                gameOver = true;
                continue;
            }

            if (ball.getMaxX() > MAX_X) {
                directionX = directionX * SIGN;
                sfx.playSound("hit");
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