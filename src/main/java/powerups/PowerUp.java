package powerups;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import mx.unison.pong.BallRunner;
import mx.unison.pong.Board;
import mx.unison.pong.Paddle;

public abstract class PowerUp {

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;
    private static final double FALL_SPEED = 3.0;

    protected Rectangle2D.Double bounds;
    protected BufferedImage sprite;
    protected boolean active = true;

    public PowerUp(double x, double y, String spriteName) {
        this.bounds = new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
        loadSprite(spriteName);
    }

    private void loadSprite(String spriteName) {
        try {
            // Asume que las imágenes están en src/main/resources/ (ej: PWRincreasepaddle.png)
            sprite = ImageIO.read(ClassLoader.getSystemResourceAsStream(spriteName + ".png"));
        } catch (Exception e) {
            System.err.println("Error al cargar sprite del PowerUp: " + spriteName);
        }
    }

    public void update() {
        bounds.y += FALL_SPEED;
    }

    public void paint(Graphics2D g2d) {
        if (!active) return;
        if (sprite != null) {
            g2d.drawImage(sprite, (int) bounds.x, (int) bounds.y, WIDTH, HEIGHT, null);
        } else {
            g2d.fill(bounds);
        }
    }

    public boolean intersects(Rectangle2D.Double paddleBounds) {
        return active && bounds.intersects(paddleBounds);
    }

    public abstract void applyEffect(Board board, BallRunner ballRunner, Paddle paddle);

    public boolean isOutScreen(int maxY) {
        return bounds.y > maxY;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}