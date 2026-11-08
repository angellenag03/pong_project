package mx.unison.pong;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Brick {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 16;

    private final Rectangle2D.Double bounds;
    private boolean destroyed = false;

    // Sprite base original en escala de grises
    private static BufferedImage baseTexture;

    // Imagen resultante ya tintada para este bloque específico
    private BufferedImage tintedSprite;

    static {
        try {
            // Carga la textura base una sola vez para todos los bloques
            baseTexture = ImageIO.read(ClassLoader.getSystemResourceAsStream("brick.png"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la imagen base del bloque: " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public Brick(double x, double y, Color color) {
        this.bounds = new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
        if (baseTexture != null) {
            this.tintedSprite = createTintedSprite(baseTexture, color);
        }
    }

    /**
     * Aplica el color deseado a la textura en escala de grises multiplicando 
     * la luminancia original por los componentes de color (tintado).
     */
    private BufferedImage createTintedSprite(BufferedImage src, Color color) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float rFactor = color.getRed() / 255.0f;
        float gFactor = color.getGreen() / 255.0f;
        float bFactor = color.getBlue() / 255.0f;

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int argb = src.getRGB(px, py);
                
                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue  = argb & 0xFF;

                // Como la imagen es escala de grises, red, green y blue son idénticos (luminancia)
                int newRed   = Math.min(255, (int) (red * rFactor));
                int newGreen = Math.min(255, (int) (green * gFactor));
                int newBlue  = Math.min(255, (int) (blue * bFactor));

                int newArgb = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                tinted.setRGB(px, py, newArgb);
            }
        }
        return tinted;
    }

    public void paint(Graphics2D g2d) {
        if (destroyed) {
            return;
        }

        if (tintedSprite != null) {
            g2d.drawImage(tintedSprite, (int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height, null);
        } else {
            g2d.fill(bounds);
        }
    }

    public boolean intersects(RectangularShape shape) {
        if (destroyed) return false;
        return bounds.intersects(shape.getBounds2D());
    }

    public Rectangle2D.Double getBounds() {
        return bounds;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}