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

    // Velocidad base por defecto (en lugar de constantes fijas final)
    private int speedX = 8;
    private int speedY = 8;
    
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

    // Reinicia la velocidad de la bola al valor inicial (al perder la partida)
    public void resetSpeed() {
        this.speedX = 8;
        this.speedY = 8;
    }

    // Aumenta un poco la velocidad de la bola para el siguiente nivel
    public void increaseSpeed() {
        this.speedX += 1;
        this.speedY += 1;
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

        for (int i = 0; i < trail.size(); i++) {
            Point p = trail.get(i);
            float alpha = (float) (i + 1) / (trail.size() + 2);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));

            if (sprite != null) {
                g2d.drawImage(sprite, p.x, p.y, (int) ball.width, (int) ball.height, null);
            } else {
                g2d.fillOval(p.x, p.y, (int) ball.width, (int) ball.height);
            }
        }

        g2d.setComposite(originalComposite);

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

    /**
     * Aplica el efecto de cámara lenta reduciendo temporalmente la velocidad de la bola.
     * 
     * @param durationMs Duración del efecto en milisegundos.
     */
    public void applySlowTime(long durationMs) {
        new Thread(() -> {
            try {
                // Guarda la velocidad previa
                int originalDX = this.speedX;
                int originalDY = this.speedY;

                // Reduce la velocidad a la mitad (mínimo 3 px/frame para no congelarla)
                this.speedX = Math.max(3, this.speedX / 2);
                this.speedY = Math.max(3, this.speedY / 2);

                Thread.sleep(durationMs);

                // Restaura la velocidad original
                this.speedX = originalDX;
                this.speedY = originalDY;
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    public Ellipse2D.Double getBall() {
        return ball;
    }
    
    @Override
    public void run() {
        while (!stop) {

            if (isPaused || gameOver) {
                Thread.yield();
                continue;
            }

            trail.add(new Point(ballX, ballY));
            if (trail.size() > MAX_TRAIL_SIZE) {
                trail.removeFirst();
            }

            if (paddle.intersects(ball)) {
                directionY = directionY * SIGN;
                ballX = ballX + (speedX * directionX);
                ballY = ballY + (speedY * directionY);
                ball.x = ballX;
                ball.y = ballY;
                
                sfx.playSound("hit");
                continue;
            }

            if (ball.getMinY() < 0) {
                sfx.playSound("hit");                
                directionY = directionY * SIGN;
                ballY = 0; // <-- Ajustar posición para despegarla del techo
                ball.y = ballY;
            }

            if (ball.getMinX() < 0) {
                sfx.playSound("hit");
                directionX = directionX * SIGN;
                ballX = 0; // <-- Ajustar posición
                ball.x = ballX;
            }
            
            if (ball.getMaxY() >= MAX_Y) {
                gameOver = true;
                continue;
            }

            if (ball.getMaxX() > MAX_X) {
                sfx.playSound("hit");
                directionX = directionX * SIGN;
                ballX = MAX_X - (int) ball.width; // <-- Ajustar posición al borde derecho
                ball.x = ballX;
            }

            ballX = ballX + (speedX * directionX);
            ballY = ballY + (speedY * directionY);
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