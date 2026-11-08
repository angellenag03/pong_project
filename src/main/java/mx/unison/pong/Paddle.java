package mx.unison.pong;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Paddle {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    private static final double MIN_X = 0;
    private static final double MAX_X = 640 - WIDTH;

    private final Rectangle2D.Double paddle;
    private double baseSpeed = 8.0;
    private double sprintSpeed = 14.0;

    // --- Atributos para la Textura ---
    private BufferedImage textureSheet;
    private Image texture;

    public Paddle() {
        super();
        paddle = new Rectangle2D.Double(160.0, 450, WIDTH, HEIGHT);
        loadTexture();
    }

    private void loadTexture() {
        try {
            // Lee la imagen de 16x48 desde los recursos (ej: src/main/resources/paddle.png)
            textureSheet = ImageIO.read(ClassLoader.getSystemResourceAsStream("paddle.png"));
            
            // Si la textura ocupa toda la imagen de 16x48:
            this.texture = textureSheet;
            
            // Si la imagen fuera un atlas/spritesheet más grande y quisieras sub-recortar los 16x48:
            // this.texture = textureSheet.getSubimage(0, 0, 16, 48);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la textura del paddle: " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Método para renderizar la textura sobre el Rectangle2D
    public void paint(Graphics2D g2d) {
        if (texture != null) {
            // Escala dinámicamente la imagen de 16x48 al ancho (80) y alto (30) del paddle
            g2d.drawImage(texture, (int) paddle.x, (int) paddle.y, (int) paddle.width, (int) paddle.height, null);
        } else {
            // Fallback por si no encuentra la imagen: dibuja el rectángulo plano
            g2d.fill(paddle);
        }
    }

    public synchronized void moveRight(boolean isShiftPressed) {
        double currentSpeed = isShiftPressed ? sprintSpeed : baseSpeed;
        if (paddle.x + currentSpeed <= MAX_X) {
            paddle.x += currentSpeed;
        } else {
            paddle.x = MAX_X;
        }
    }

    public synchronized void moveLeft(boolean isShiftPressed) {
        double currentSpeed = isShiftPressed ? sprintSpeed : baseSpeed;
        if (paddle.x - currentSpeed >= MIN_X) {
            paddle.x -= currentSpeed;
        } else {
            paddle.x = MIN_X;
        }
    }

    public Rectangle2D.Double getPaddle() {
        return paddle;
    }

    public boolean check(int x, int y) {
        return paddle.contains(x, y);
    }

    public boolean intersects(RectangularShape shape) {
        return paddle.intersects(shape.getBounds2D());
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public void setSprintSpeed(double sprintSpeed) {
        this.sprintSpeed = sprintSpeed;
    }
}