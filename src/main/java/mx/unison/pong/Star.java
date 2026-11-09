package mx.unison.pong;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Star {
    private double x, y;
    private double speed;
    private float alpha;
    private float alphaSpeed;
    private int size;

    private static final Random random = new Random();
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;

    public Star() {
        reset(true);
    }

    private void reset(boolean randomY) {
        this.x = random.nextInt(SCREEN_WIDTH);
        this.y = randomY ? random.nextInt(SCREEN_HEIGHT) : -5;
        this.speed = 0.5 + random.nextDouble() * 1.5; // Velocidad de caída
        this.size = random.nextInt(2) + 1;             // Tamaño 1px o 2px
        this.alpha = random.nextFloat();
        this.alphaSpeed = 0.01f + random.nextFloat() * 0.02f;
    }

    public void update() {
        // Desplazamiento hacia abajo
        y += speed;
        if (y > SCREEN_HEIGHT) {
            reset(false);
        }

        // Efecto de parpadeo (fade in/out)
        alpha += alphaSpeed;
        if (alpha >= 1.0f) {
            alpha = 1.0f;
            alphaSpeed = -alphaSpeed;
        } else if (alpha <= 0.2f) {
            alpha = 0.2f;
            alphaSpeed = -alphaSpeed;
        }
    }

    public void paint(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(Color.WHITE);
        g2d.fillRect((int) x, (int) y, size, size);
    }
}