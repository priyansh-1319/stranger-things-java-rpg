import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable {

    public final int WIDTH = 800;
    public final int HEIGHT = 600;

    Thread gameThread;
    KeyHandler keyH = new KeyHandler();
    SoundManager sound; // receives music from IntroPanel

    Player player = new Player(keyH);
    Enemy enemy = new Enemy();

    BufferedImage backgroundImage;

    boolean defeated = false;
    boolean victory = false;

    int playerDamageCooldown = 0;
    int enemyDamageCooldown = 0;
    int laserDamageCooldown = 0;
    int chargeDamageCooldown = 0;
    int playerLaserDamageCooldown = 0;
    int fireballDamageCooldown = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        try {
            backgroundImage = ImageIO.read(new File("assets/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null && !defeated && !victory) {
            update();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        repaint();
    }

    public void update() {
        if (!defeated && !victory) {
            player.update();
            enemy.update(player.x, player.y);

            if (playerDamageCooldown > 0) playerDamageCooldown--;
            if (enemyDamageCooldown > 0) enemyDamageCooldown--;
            if (laserDamageCooldown > 0) laserDamageCooldown--;
            if (chargeDamageCooldown > 0) chargeDamageCooldown--;
            if (playerLaserDamageCooldown > 0) playerLaserDamageCooldown--;
            if (fireballDamageCooldown > 0) fireballDamageCooldown--;

            // Boss body hits player
            if (player.getBounds().intersects(enemy.getBounds())) {
                if (enemyDamageCooldown == 0) {
                    player.health -= 5;
                    player.hitFlashTimer = 8;
                    enemyDamageCooldown = 30;
                }
            }

            // Boss charge hits player
            Rectangle chargeBounds = enemy.getChargeBounds();
            if (chargeBounds != null && chargeBounds.intersects(player.getBounds())) {
                if (chargeDamageCooldown == 0) {
                    player.health -= 25;
                    player.hitFlashTimer = 15;
                    chargeDamageCooldown = 40;
                }
            }

            // Boss laser hits player
            Rectangle bossLaser = enemy.getLaserBounds();
            if (bossLaser != null && bossLaser.intersects(player.getBounds())) {
                if (laserDamageCooldown == 0) {
                    player.health -= 20;
                    player.hitFlashTimer = 8;
                    laserDamageCooldown = 20;
                }
            }

            // Player melee hits boss
            if (keyH.attack && player.getBounds().intersects(enemy.getBounds())) {
                if (playerDamageCooldown == 0) {
                    enemy.health -= 10;
                    enemy.hitFlashTimer = 8;
                    playerDamageCooldown = 20;
                    player.registerHit();
                }
            }

            // Player laser hits boss
            Rectangle playerLaser = player.getLaserBounds();
            if (playerLaser != null && playerLaser.intersects(enemy.getBounds())) {
                if (playerLaserDamageCooldown == 0) {
                    enemy.health -= 15;
                    enemy.hitFlashTimer = 8;
                    playerLaserDamageCooldown = 20;
                    player.registerHit();
                }
            }

            // Fireball hits boss
            Rectangle fireball = player.getFireballBounds();
            if (fireball != null && fireball.intersects(enemy.getBounds())) {
                if (fireballDamageCooldown == 0) {
                    enemy.health -= 40;
                    enemy.hitFlashTimer = 15;
                    player.fireballActive = false;
                    fireballDamageCooldown = 10;
                }
            }

            if (player.health <= 0) defeated = true;
            if (enemy.health <= 0) victory = true;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);

        if (!defeated && !victory) {
            player.draw(g2);
            enemy.draw(g2);

            // PLAYER HP bar
            int playerBarWidth = 200;
            int playerFill = (int)((player.health / (float) player.maxHealth) * playerBarWidth);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(20, 20, playerBarWidth, 15);
            g2.setColor(Color.RED);
            g2.fillRect(20, 20, playerFill, 15);
            g2.setColor(Color.WHITE);
            g2.drawRect(20, 20, playerBarWidth, 15);
            g2.drawString("PLAYER HP", 20, 15);

            // BOSS HP bar
            int enemyBarWidth = 200;
            int enemyFill = (int)((enemy.health / (float) enemy.maxHealth) * enemyBarWidth);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(20, 45, enemyBarWidth, 15);
            g2.setColor(Color.RED);
            g2.fillRect(20, 45, enemyFill, 15);
            g2.setColor(Color.WHITE);
            g2.drawRect(20, 45, enemyBarWidth, 15);
            g2.drawString("BOSS HP", 20, 40);

            // FIREBALL CHARGE METER
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(20, 70, 200, 15);
            if (player.fireballCharged) {
                int pulse = (System.currentTimeMillis() % 500 < 250) ? 200 : 255;
                g2.setColor(new Color(255, pulse, 0));
                g2.fillRect(20, 70, 200, 15);
                g2.setColor(Color.WHITE);
                g2.drawRect(20, 70, 200, 15);
                g2.drawString("FIREBALL READY! (F)", 20, 65);
            } else {
                int chargeFill = (int)((player.hitCount / (float) player.hitsNeeded) * 200);
                g2.setColor(new Color(255, 140, 0));
                g2.fillRect(20, 70, chargeFill, 15);
                g2.setColor(Color.WHITE);
                g2.drawRect(20, 70, 200, 15);
                g2.drawString("FIREBALL " + player.hitCount + "/" + player.hitsNeeded, 20, 65);
            }

            // HUD warnings
            if (enemy.laserActive) {
                g2.setColor(new Color(255, 80, 80));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString("! LASER !", 355, 30);
            }
            if (enemy.state.equals("warning")) {
                g2.setColor(new Color(255, 150, 0));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString("! INCOMING CHARGE !", 290, 30);
            }
            if (enemy.state.equals("charging")) {
                g2.setColor(Color.ORANGE);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString("!! CHARGING !!", 310, 30);
            }
            if (player.laserActive) {
                g2.setColor(new Color(80, 200, 255));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString(">> FIRING <<", 320, 55);
            }

        } else if (victory) {
            if (sound != null) sound.stop();
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            g2.drawString("YOU WIN!", 280, 300);
        } else {
            if (sound != null) sound.stop();
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            g2.drawString("YOU ARE DEFEATED", 200, 300);
        }

        g2.dispose();
    }
}