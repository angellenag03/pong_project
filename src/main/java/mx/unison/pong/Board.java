package mx.unison.pong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import powerups.IncreasePaddlePowerUp;
import powerups.PowerUp;
import powerups.SlowTimePowerUp;
import powerups.TripleBallPowerUp;
import sfx.SFXManager;

public class Board extends JComponent implements Runnable, KeyListener {
    private Dimension preferredSize = null;

    private Paddle paddle;
    
    // --- Lista para soportar Multibola ---
    private List<BallRunner> ballRunners;
    private List<Brick> bricks;
    private List<PowerUp> fallingPowerUps;
    private Random random = new Random();

    // --- Campo de Estrellas ---
    private List<Star> stars;
    private static final int STAR_COUNT = 40;

    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isYouWin = false;
    private int score = 0;
    private int level = 1;

    private SFXManager sfx = SFXManager.getInstance();

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean shiftPressed = false;

    private Font customFont;
    private Font theFont;
    private Font subFont;
    private Font scoreFont;

    public Board() {
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        
        loadCustomFont();

        paddle = new Paddle();
        ballRunners = new ArrayList<>();
        fallingPowerUps = new ArrayList<>();

        // Crear la bola inicial
        spawnInitialBall();

        initBricks();
        initStars();

        Thread t2 = new Thread(this);
        t2.start();
    }

    private void spawnInitialBall() {
        Ellipse2D.Double ballShape = new Ellipse2D.Double(320, 380, 20, 20);
        BallRunner mainBall = new BallRunner(ballShape, paddle);
        ballRunners.add(mainBall);
        
        Thread t = new Thread(mainBall);
        t.start();
    }

    public void spawnExtraBalls() {
        if (ballRunners.isEmpty()) return;

        // Toma la posición de la primera bola activa para generar las extra
        BallRunner firstBall = ballRunners.get(0);
        double currentX = firstBall.getBall().x;
        double currentY = firstBall.getBall().y;

        for (int i = 0; i < 2; i++) {
            Ellipse2D.Double extraShape = new Ellipse2D.Double(currentX, currentY, 20, 20);
            BallRunner extraBall = new BallRunner(extraShape, paddle);
            
            // Si la bola principal ya tenía más velocidad por el nivel, se aplica
            for (int lvl = 1; lvl < level; lvl++) {
                extraBall.increaseSpeed();
            }

            ballRunners.add(extraBall);
            Thread t = new Thread(extraBall);
            t.start();
        }
    }

    private void initStars() {
        stars = new ArrayList<>();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }
    }

    private void loadCustomFont() {
        try {
            InputStream fontStream = ClassLoader.getSystemResourceAsStream("font/ROGLyonsTypeRegular3.ttf");
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                theFont = customFont.deriveFont(Font.BOLD, 28f);
                subFont = customFont.deriveFont(Font.PLAIN, 18f);
                scoreFont = customFont.deriveFont(Font.BOLD, 18f);
            } else {
                useFallbackFonts();
            }
        } catch (Exception e) {
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

    private void clearAllBalls() {
        for (BallRunner runner : ballRunners) {
            runner.stop();
        }
        ballRunners.clear();
    }

    private void resetGame() {
        score = 0;
        level = 1;
        isPaused = false;
        isGameOver = false;
        isYouWin = false;
        leftPressed = false;
        rightPressed = false;
        shiftPressed = false;

        fallingPowerUps.clear();
        initBricks();
        clearAllBalls();
        spawnInitialBall();
    }

    private void nextLevel() {
        level++;
        isPaused = false;
        isGameOver = false;
        isYouWin = false;
        leftPressed = false;
        rightPressed = false;
        shiftPressed = false;

        fallingPowerUps.clear();
        initBricks();
        clearAllBalls();
        
        spawnInitialBall();
        // Aumentar velocidad según el nivel alcanzado
        for (BallRunner runner : ballRunners) {
            for (int i = 1; i < level; i++) {
                runner.increaseSpeed();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        Graphics2D g = (Graphics2D) graphicsContext;
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Fondo
        if (isOpaque()) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
        }

        // 2. Estrellas
        for (Star star : stars) {
            star.paint(g);
        }

        g.setComposite(AlphaComposite.SrcOver);
        g.setStroke(new BasicStroke(5.0f));

        // 3. Bloques
        for (Brick brick : bricks) {
            brick.paint(g);
        }

        // 4. Power-Ups cayendo
        for (PowerUp pwr : fallingPowerUps) {
            pwr.paint(g);
        }

        // 5. Todas las bolas activas y el paddle
        for (BallRunner runner : ballRunners) {
            runner.paint(g);
        }
        paddle.paint(g);

        // 6. HUD (Score y Nivel)
        g.setFont(scoreFont);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 15, 25);
        g.drawString("Level: " + level, getWidth() - 110, 25);

        // 7. Textos de Pausa / Game Over / Victory
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

        } else if (isYouWin) {
            g.setColor(Color.GREEN);
            g.setFont(theFont);
            String title = "YOU WIN!";
            FontMetrics fm1 = g.getFontMetrics(theFont);
            int x1 = (getWidth() - fm1.stringWidth(title)) / 2;
            int y1 = (getHeight() / 2) - 10;
            g.drawString(title, x1, y1);

            g.setColor(Color.WHITE);
            g.setFont(subFont);
            String subtitle = "Pulsa R para el siguiente nivel";
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

    private void spawnRandomPowerUp(double x, double y) {
        int type = random.nextInt(3);
        switch (type) {
            case 0:
                fallingPowerUps.add(new IncreasePaddlePowerUp(x, y));
                break;
            case 1:
                fallingPowerUps.add(new SlowTimePowerUp(x, y));
                break;
            case 2:
                fallingPowerUps.add(new TripleBallPowerUp(x, y));
                break;
        }
    }

    private void updateGame() {
        for (Star star : stars) {
            star.update();
        }

        // Limpiar bolas que hayan caído al suelo
        Iterator<BallRunner> ballIterator = ballRunners.iterator();
        while (ballIterator.hasNext()) {
            BallRunner runner = ballIterator.next();
            if (runner.isGameOver()) {
                runner.stop();
                ballIterator.remove();
            }
        }

        // Si ya no queda ninguna bola activa en el tablero, es GAME OVER
        if (ballRunners.isEmpty()) {
            isGameOver = true;
        }

        if (!isPaused && !isGameOver && !isYouWin) {
            if (leftPressed) {
                paddle.moveLeft(shiftPressed);
            }
            if (rightPressed) {
                paddle.moveRight(shiftPressed);
            }

            // Actualizar Power-ups cayendo
            Iterator<PowerUp> pwrIterator = fallingPowerUps.iterator();
            while (pwrIterator.hasNext()) {
                PowerUp pwr = pwrIterator.next();
                pwr.update();

                if (pwr.intersects(paddle.getPaddle())) {
                    // Tomamos una de las bolas activas como referencia para SlowTime si aplica
                    BallRunner activeRunner = ballRunners.isEmpty() ? null : ballRunners.get(0);
                    pwr.applyEffect(this, activeRunner, paddle);
                    sfx.playSound("hit");
                    pwrIterator.remove();
                } else if (pwr.isOutScreen(getHeight())) {
                    pwrIterator.remove();
                }
            }

            // Checar colisión de TODAS las bolas activas con los bloques
            for (BallRunner runner : ballRunners) {
                for (int i = 0; i < bricks.size(); i++) {
                    Brick brick = bricks.get(i);
                    if (brick.intersects(runner.getBall())) {
                        runner.checkBrickCollision(brick);
                        
                        // Probabilidad 1/4 de soltar Power-Up
                        if (random.nextInt(4) == 0) {
                            spawnRandomPowerUp(brick.getBounds().x, brick.getBounds().y);
                        }

                        bricks.remove(i);
                        score += 10;
                        break;
                    }
                }
            }

            // Condición de victoria
            if (bricks.isEmpty()) {
                isYouWin = true;
                for (BallRunner runner : ballRunners) {
                    runner.pause();
                }
            }
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
                if (!isGameOver && !isYouWin) {
                    isPaused = !isPaused;
                    for (BallRunner runner : ballRunners) {
                        if (isPaused) runner.pause();
                        else runner.resume();
                    }
                    if (isPaused) {
                        sfx.setPaused(true);
                        sfx.playSound("pause.wav");
                    } else {
                        sfx.setPaused(false);
                        sfx.playSound("resume.wav");
                    }
                }
                break;
            case KeyEvent.VK_R:
                if (isYouWin) {
                    nextLevel();
                } else if (isGameOver) {
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