package org.example.Client;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FullScreenImage extends JFrame {

    private BufferedImage image;
    private boolean isPlayingAudio = false;

    public FullScreenImage(BufferedImage image) {
        this.image = image;

        setTitle("Full Screen Image");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setAlwaysOnTop(true);

        JLabel label = new JLabel(new ImageIcon(image));
        getContentPane().add(label, BorderLayout.CENTER);

        setVisible(true);
    }

    public void setIsPlayingAudio(boolean isPlayingAudio) {
        this.isPlayingAudio = isPlayingAudio;
    }

    public boolean isPlayingAudio() {
        return isPlayingAudio;
    }

    public static void runScreamer(String imagePath, String audioPath) {
        try {
            System.out.println("Я получил скример!");

            BufferedImage image = ImageIO.read(new File(imagePath));

            FullScreenImage fullScreenImage = new FullScreenImage(image);

            playAudio(audioPath, fullScreenImage);

            while (fullScreenImage.isPlayingAudio()) {
                Thread.sleep(100);
            }

            fullScreenImage.dispose();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void playAudio(String audioPath, FullScreenImage fullScreenImage) {
        try {
            File audioFile = new File(audioPath);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(audioFile));
            clip.start();
            fullScreenImage.setIsPlayingAudio(true);

            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    fullScreenImage.setIsPlayingAudio(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
