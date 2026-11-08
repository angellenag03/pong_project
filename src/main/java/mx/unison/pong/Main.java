package mx.unison.pong;

import javax.swing.*;
import java.awt.*;
import sfx.SFXManager;

public class Main {
    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Pong");

        f.setResizable(false);
        f.setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout());
        Board board = new Board();
        p.add(board, BorderLayout.CENTER);

        SFXManager sfx = SFXManager.getInstance();
        sfx.playSong("breakout.wav");
        f.addKeyListener( board );
        f.setContentPane(p);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}