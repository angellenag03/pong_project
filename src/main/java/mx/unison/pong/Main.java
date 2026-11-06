package mx.unison.pong;

import javax.swing.*;
import java.awt.*;

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

        JPanel p = new JPanel(new BorderLayout());
        Board board = new Board();
        p.add(board, BorderLayout.CENTER);

        f.addKeyListener( board );
        f.setContentPane(p);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}