import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class IntroPanel extends JPanel implements Runnable {

    JFrame parentFrame;
    SoundManager sound;
    Thread introThread;
    Random random = new Random();

    int stage = 0;
    int timer = 0;
    float alpha = 0f;

    String word1 = "STRANGER";
    String word2 = "THINGS";
    int lettersShown1 = 0;
    int lettersShown2 = 0;
    int letterTimer = 0;
    int letterInterval = 8;

    float glowPulse = 0f;
    float glowDir = 0.02f;

    int flickerTimer = 0;
    boolean flickerOn = false;
    int[][] staticNoise;
    int noiseTimer = 0;

    ArrayList<Particle> particles = new ArrayList<>();

    BufferedImage backgroundImage;
    BufferedImage playerImage;

    Rectangle playButton = new Rectangle(300, 380, 200, 60);
    boolean playHover = false;
    boolean playClicked = false;

    public IntroPanel(JFrame frame, SoundManager sound) {
        this.parentFrame = frame;
        this.sound = sound;
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        try {
            backgroundImage = ImageIO.read(new File("assets/background.png"));
            playerImage = ImageIO.read(new File("assets/player.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        staticNoise = new int[40][30];
        randomiseNoise();

        for (int i = 0; i < 60; i++) {
            particles.add(new Particle(random));
        }

        // Start intro music
        sound.play("assets/intro_music.wav", true);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (stage == 6 && playButton.contains(e.getPoint())) {
                    startGame();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                playHover = (stage == 6 && playButton.contains(e.getPoint()));
            }
        });
    }

    void randomiseNoise() {
        for (int i = 0; i < staticNoise.length; i++)
            for (int j = 0; j < staticNoise[0].length; j++)
                staticNoise[i][j] = random.nextInt(80);
    }

    public void startIntroThread() {
        introThread = new Thread(this);
        introThread.start();
    }

    @Override
    public void run() {
        while (introThread != null && !playClicked) {
            update();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        timer++;

        for (Particle p : particles) p.update();

        glowPulse += glowDir;
        if (glowPulse > 1f || glowPulse < 0f) glowDir = -glowDir;

        noiseTimer++;
        if (noiseTimer % 3 == 0) randomiseNoise();

        flickerTimer++;
        if (flickerTimer > 4 + random.nextInt(6)) {
            flickerOn = !flickerOn;
            flickerTimer = 0;
        }

        if (stage == 0) {
            if (timer >= 80) {
                stage = 1;
                timer = 0;
                lettersShown1 = 0;
            }
        } else if (stage == 1) {
            letterTimer++;
            if (letterTimer >= letterInterval) {
                letterTimer = 0;
                if (lettersShown1 < word1.length()) {
                    lettersShown1++;
                } else {
                    stage = 2;
                    timer = 0;
                    lettersShown2 = 0;
                    letterTimer = 0;
                }
            }
        } else if (stage == 2) {
            letterTimer++;
            if (letterTimer >= letterInterval) {
                letterTimer = 0;
                if (lettersShown2 < word2.length()) {
                    lettersShown2++;
                } else {
                    stage = 3;
                    timer = 0;
                }
            }
        } else if (stage == 3) {
            if (timer >= 100) {
                stage = 4;
                timer = 0;
                alpha = 0f;
            }
        } else if (stage == 4) {
            if (timer < 40) alpha = timer / 40f;
            else if (timer > 180) alpha = Math.max(0f, 1f - (timer - 180) / 40f);
            else alpha = 1f;
            if (timer >= 220) { stage = 5; timer = 0; alpha = 0f; }
        } else if (stage == 5) {
            if (timer < 40) alpha = timer / 40f;
            else if (timer > 180) alpha = Math.max(0f, 1f - (timer - 180) / 40f);
            else alpha = 1f;
            if (timer >= 220) { stage = 6; timer = 0; alpha = 0f; }
        } else if (stage == 6) {
            if (alpha < 1f) alpha = Math.min(1f, alpha + 0.03f);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 800, 600);

        if (stage == 0) {
            drawStatic(g2);
        } else if (stage == 1 || stage == 2 || stage == 3) {
            if (backgroundImage != null) {
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.2f));
                g2.drawImage(backgroundImage, 0, 0, 800, 600, null);
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 1f));
            }
            if (flickerOn && random.nextInt(4) == 0) {
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, 800, 600);
            }
            for (Particle p : particles) p.draw(g2);
            drawLetterTitle(g2);
        } else {
            if (backgroundImage != null) {
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.35f));
                g2.drawImage(backgroundImage, 0, 0, 800, 600, null);
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 1f));
            }
            for (Particle p : particles) p.draw(g2);
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));
            if (stage == 4) drawStoryScreen1(g2);
            else if (stage == 5) drawStoryScreen2(g2);
            else if (stage == 6) drawPlayScreen(g2);
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1f));
        }

        g2.dispose();
    }

    void drawStatic(Graphics2D g2) {
        int cellW = 800 / staticNoise.length;
        int cellH = 600 / staticNoise[0].length;
        for (int i = 0; i < staticNoise.length; i++) {
            for (int j = 0; j < staticNoise[0].length; j++) {
                int v = staticNoise[i][j];
                g2.setColor(new Color(v, 0, 0, 180));
                g2.fillRect(i * cellW, j * cellH, cellW, cellH);
            }
        }
        for (int i = 0; i < 5; i++) {
            int scanY = random.nextInt(600);
            g2.setColor(new Color(150, 0, 0, 40));
            g2.fillRect(0, scanY, 800, 2);
        }
    }

    void drawLetterTitle(Graphics2D g2) {
        Font stFont = new Font("Serif", Font.BOLD, 80);
        g2.setFont(stFont);
        FontMetrics fm = g2.getFontMetrics();

        int word1Width = fm.stringWidth(word1);
        int word2Width = fm.stringWidth(word2);
        int startX1 = (800 - word1Width) / 2;
        int startX2 = (800 - word2Width) / 2;
        int y1 = 240;
        int y2 = 340;

        for (int i = 0; i < lettersShown1; i++) {
            String ch = String.valueOf(word1.charAt(i));
            int cx = startX1 + fm.stringWidth(word1.substring(0, i));
            int glow = (int)(40 + glowPulse * 60);
            g2.setColor(new Color(180, 0, 0, glow));
            g2.setFont(new Font("Serif", Font.BOLD, 86));
            g2.drawString(ch, cx - 3, y1 + 3);
            g2.setFont(stFont);
            g2.setColor(new Color(
                (int)(160 + glowPulse * 60),
                (int)(10 + glowPulse * 10),
                (int)(10 + glowPulse * 10)
            ));
            g2.drawString(ch, cx, y1);
        }

        for (int i = 0; i < lettersShown2; i++) {
            String ch = String.valueOf(word2.charAt(i));
            int cx = startX2 + fm.stringWidth(word2.substring(0, i));
            int glow = (int)(40 + glowPulse * 60);
            g2.setColor(new Color(180, 0, 0, glow));
            g2.setFont(new Font("Serif", Font.BOLD, 86));
            g2.drawString(ch, cx - 3, y2 + 3);
            g2.setFont(stFont);
            g2.setColor(new Color(
                (int)(160 + glowPulse * 60),
                (int)(10 + glowPulse * 10),
                (int)(10 + glowPulse * 10)
            ));
            g2.drawString(ch, cx, y2);
        }

        if (stage == 3) {
            g2.setColor(new Color(180, 180, 180,
                (int)(120 + glowPulse * 100)));
            g2.setFont(new Font("Serif", Font.ITALIC, 22));
            String sub = "The Upside Down Awaits...";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sub,
                (800 - fm2.stringWidth(sub)) / 2, 420);
        }
    }

    void drawStoryScreen1(Graphics2D g2) {
        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("Serif", Font.BOLD, 28));
        String line1 = "Hawkins, Indiana. 1983.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(line1, (800 - fm.stringWidth(line1)) / 2, 220);

        g2.setFont(new Font("Serif", Font.PLAIN, 20));
        g2.setColor(new Color(180, 180, 180));
        String[] lines = {
            "A rift has opened to the Upside Down.",
            "The Demogorgon has crossed over into Hawkins.",
            "It lurks in the shadows, hunting its prey.",
            "Terror spreads across the town..."
        };
        FontMetrics fm2 = g2.getFontMetrics();
        int y = 290;
        for (String line : lines) {
            g2.drawString(line, (800 - fm2.stringWidth(line)) / 2, y);
            y += 38;
        }
    }

    void drawStoryScreen2(Graphics2D g2) {
        if (playerImage != null) {
            g2.drawImage(playerImage, 330, 120, 140, 140, null);
        }
        g2.setColor(new Color(220, 50, 50));
        g2.setFont(new Font("Serif", Font.BOLD, 30));
        String line1 = "One girl stands against the darkness.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(line1, (800 - fm.stringWidth(line1)) / 2, 310);

        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("Serif", Font.PLAIN, 22));
        String[] lines = {
            "Eleven. Armed with psychic powers.",
            "She must defeat the Demogorgon",
            "and close the gate — before it's too late."
        };
        FontMetrics fm2 = g2.getFontMetrics();
        int y = 360;
        for (String line : lines) {
            g2.drawString(line, (800 - fm2.stringWidth(line)) / 2, y);
            y += 36;
        }
    }

    void drawPlayScreen(Graphics2D g2) {
        g2.setColor(new Color(180, 20, 20));
        g2.setFont(new Font("Serif", Font.BOLD, 52));
        String title = "STRANGER THINGS";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (800 - fm.stringWidth(title)) / 2, 180);

        g2.setColor(new Color(160, 160, 160));
        g2.setFont(new Font("Serif", Font.ITALIC, 20));
        String sub = "The Demogorgon Boss Fight";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, (800 - fm2.stringWidth(sub)) / 2, 220);

        g2.setColor(new Color(130, 130, 130));
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        String[] controls = {
            "W A S D — Move",
            "SPACE — Laser attack",
            "F — Fireball (charge by hitting boss 3 times)"
        };
        int cy = 290;
        for (String c : controls) {
            FontMetrics fmc = g2.getFontMetrics();
            g2.drawString(c, (800 - fmc.stringWidth(c)) / 2, cy);
            cy += 26;
        }

        g2.setColor(playHover ?
            new Color(180, 30, 30) : new Color(120, 10, 10));
        g2.fillRoundRect(playButton.x, playButton.y,
            playButton.width, playButton.height, 16, 16);
        g2.setColor(new Color(220, 60, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(playButton.x, playButton.y,
            playButton.width, playButton.height, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 28));
        String playText = "PLAY";
        FontMetrics fmb = g2.getFontMetrics();
        g2.drawString(playText,
            playButton.x + (playButton.width - fmb.stringWidth(playText)) / 2,
            playButton.y + 38);
    }

    void startGame() {
        playClicked = true;
        introThread = null;
        sound.stop();
        parentFrame.getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel();
        gamePanel.sound = sound;
        parentFrame.add(gamePanel);
        parentFrame.pack();
        parentFrame.revalidate();
        parentFrame.repaint();
        gamePanel.requestFocusInWindow();
        sound.play("assets/game_music.wav", true);
        gamePanel.startGameThread();
    }

    class Particle {
        float x, y, speedX, speedY, size, opacity;
        Random r;

        Particle(Random random) {
            this.r = random;
            reset();
            y = r.nextFloat() * 600;
        }

        void reset() {
            x = r.nextFloat() * 800;
            y = -10;
            speedX = (r.nextFloat() - 0.5f) * 1.2f;
            speedY = 0.4f + r.nextFloat() * 1.2f;
            size = 2 + r.nextFloat() * 4;
            opacity = 0.3f + r.nextFloat() * 0.6f;
        }

        void update() {
            x += speedX;
            y += speedY;
            if (y > 610) reset();
        }

        void draw(Graphics2D g2) {
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));
            g2.setColor(new Color(180, 20, 20));
            g2.fillOval((int) x, (int) y, (int) size, (int) size);
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1f));
        }
    }
}