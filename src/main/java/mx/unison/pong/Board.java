package mx.unison.pong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import sfx.SFXManager;

public class Board extends JComponent implements Runnable, KeyListener {
    private Dimension preferredSize = null;
    private Ellipse2D.Double ball;

    private Paddle paddle;
    private BallRunner ballRunner;
    private List<Brick> bricks;

    private boolean isPaused = false;
    private boolean isGameOver = false;
    private int score = 0;

    private SFXManager sfx = SFXManager.getInstance();

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean shiftPressed = false;

    // --- Fuentes del Juego ---
    private Font customFont;
    private Font theFont;
    private Font subFont;
    private Font scoreFont;

    public Board() {
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        
        // Cargar fuente personalizada
        loadCustomFont();

        ball = new Ellipse2D.Double(320, 300, 20, 20);
        paddle = new Paddle();
        ballRunner = new BallRunner(ball, paddle);

        initBricks();

        Thread t1 = new Thread(ballRunner);
        t1.start();
        Thread t2 = new Thread(this);
        t2.start();
    }

    private void loadCustomFont() {
        try {
            // Carga la fuente TTF desde la carpeta de recursos
            InputStream fontStream = ClassLoader.getSystemResourceAsStream("font/ROGLyonsTypeRegular3.ttf");
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                
                // Deriva los tamaños deseados para cada contexto
                theFont = customFont.deriveFont(Font.BOLD, 28f);
                subFont = customFont.deriveFont(Font.PLAIN, 18f);
                scoreFont = customFont.deriveFont(Font.BOLD, 18f);
            } else {
                System.err.println("No se encontró el archivo de fuente ROG LyonsType.");
                useFallbackFonts();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la fuente: " + e.getMessage());
            e.printStackTrace();
            useFallbackFonts();
        }
    }

    private void useFallbackFonts() {
        theFont = new Font("SansSerif", Font.BOLD, 28);
        subFont = new Font("SansSerif", Font.PLAIN, 18);
        scoreFont = new Font("SansSerif", Font.BOLD, 18);
    }

    private void initBricks() {
        bricks = new ArrayList<>();
        
        // Paleta de la imagen desaturada para contraste en fondo negro
        Color[] rowColors = {
            new Color(0xD8, 0xD4, 0xF8),
            new Color(0xB5, 0xC8, 0xF0),
            new Color(0x9E, 0xA3, 0xB0),
            new Color(0xC5, 0xC3, 0xC7), 
            new Color(0x4A, 0x4F, 0x60)  
        };

        int rows = 5;
        int cols = 14;
        int padding = 4;
        int startX = 65;
        int startY = 40;

        for (int r = 0; r < rows; r++) {
            Color rowColor = rowColors[r % rowColors.length];
            for (int c = 0; c < cols; c++) {
                double x = startX + c * (Brick.WIDTH + padding);
                double y = startY + r * (Brick.HEIGHT + padding);
                bricks.add(new Brick(x, y, rowColor));
            }
        }
    }

    private void resetGame() {
        score = 0;
        isPaused = false;
        isGameOver = false;
        leftPressed = false;
        rightPressed = false;
        shiftPressed = false;
        initBricks();
        ballRunner.resetPosition();
    }

    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        Graphics2D g = (Graphics2D) graphicsContext;
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo Negro
        if (isOpaque()) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
        }

        g.setStroke(new BasicStroke(5.0f));

        // 1. Renderizar bloques activos
        for (Brick brick : bricks) {
            brick.paint(g);
        }

        // 2. Renderizar la bola y el paddle
        ballRunner.paint(g);
        paddle.paint(g);

        // 3. Renderizar el Score con ROG Fonts
        g.setFont(scoreFont);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 15, 25);

        // 4. Renderizar texto de Pausa o Game Over centrado
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(theFont);
            String title = "GAME OVER";
            FontMetrics fm1 = g.getFontMetrics(theFont);
            int x1 = (getWidth() - fm1.stringWidth(title)) / 2;
            int y1 = (getHeight() / 2) - 10;
            g.drawString(title, x1, y1);

            g.setColor(Color.WHITE);
            g.setFont(subFont);
            String subtitle = "Pulsa R para reiniciar";
            FontMetrics fm2 = g.getFontMetrics(subFont);
            int x2 = (getWidth() - fm2.stringWidth(subtitle)) / 2;
            int y2 = y1 + 30;
            g.drawString(subtitle, x2, y2);
        } else if (isPaused) {
            String pauseText = "Paused";
            g.setFont(theFont);
            g.setColor(Color.BLUE);

            FontMetrics fm = g.getFontMetrics(theFont);
            int textWidth = fm.stringWidth(pauseText);
            int textHeight = fm.getAscent();

            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 2;

            g.drawString(pauseText, x, y);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (preferredSize == null) {
            return new Dimension(640, 480);
        } else {
            return super.getPreferredSize();
        }
    }

    @Override
    public void setPreferredSize(Dimension newPrefSize) {
        preferredSize = newPrefSize;
        super.setPreferredSize(newPrefSize);
    }

    private void updateGame() {
        if (ballRunner.isGameOver()) {
            isGameOver = true;
        }

        if (!isPaused && !isGameOver) {
            if (leftPressed) {
                paddle.moveLeft(shiftPressed);
            }
            if (rightPressed) {
                paddle.moveRight(shiftPressed);
            }

            for (Brick brick : bricks) {
                if (brick.intersects(ball)) {
                    ballRunner.checkBrickCollision(brick);
                    brick.setDestroyed(true);
                    score += 10;
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            updateGame();
            repaint();
            
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                if (!isGameOver) {
                    isPaused = !isPaused;
                    if (isPaused) {
                        ballRunner.pause();
                        sfx.setPaused(true);
                        sfx.playSound("pause.wav");
                    } else {
                        ballRunner.resume();
                        sfx.setPaused(false);
                        sfx.playSound("resume.wav");
                    }
                }
                break;
            case KeyEvent.VK_R:
                if (isGameOver) {
                    resetGame();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed = false;
                break;
        }
    }
}