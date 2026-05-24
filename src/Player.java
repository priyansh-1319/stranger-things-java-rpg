import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Player {

    int x = 100;
    int y = 350;
    int speed = 4;
    int health = 100;
    int maxHealth = 100;
    int hitFlashTimer = 0;

    // Laser fields
    boolean laserActive = false;
    int laserTimer = 0;
    int laserDuration = 20;
    int laserCooldown = 0;
    int laserY = 0;

    // Fireball fields
    boolean fireballActive = false;   // is fireball currently flying
    int fireballX = 0;
    int fireballY = 0;
    int fireballSpeed = 10;
    int hitCount = 0;                 // how many times player has hit boss
    int hitsNeeded = 3;              // hits needed to charge fireball
    boolean fireballCharged = false;  // ready to fire?
    boolean fireballUsed = false;     // prevent holding F to keep firing

    KeyHandler keyH;
    BufferedImage playerImage;

    public Player(KeyHandler keyH) {
        this.keyH = keyH;
        try {
            playerImage = ImageIO.read(new File("assets/player.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (keyH.up)    y -= speed;
        if (keyH.down)  y += speed;
        if (keyH.left)  x -= speed;
        if (keyH.right) x += speed;

        if (x < 0)   x = 0;
        if (y < 0)   y = 0;
        if (x > 740) x = 740;
        if (y > 540) y = 540;

        if (hitFlashTimer > 0) hitFlashTimer--;
        if (laserCooldown > 0) laserCooldown--;

        // Laser fire on SPACE
        if (keyH.attack && laserCooldown == 0 && !laserActive) {
            laserActive = true;
            laserTimer = laserDuration;
            laserY = y + 30;
        }
        if (laserActive) {
            laserTimer--;
            if (laserTimer <= 0) {
                laserActive = false;
                laserCooldown = 40;
            }
        }

        // Launch fireball on F if charged
        if (keyH.fireball && fireballCharged && !fireballActive && !fireballUsed) {
            fireballActive = true;
            fireballX = x + 60;
            fireballY = y + 20;
            fireballCharged = false;
            hitCount = 0;
            fireballUsed = true; // prevent holding F
        }
        if (!keyH.fireball) fireballUsed = false;

        // Move fireball across screen
        if (fireballActive) {
            fireballX += fireballSpeed;
            if (fireballX > 850) {
                fireballActive = false; // off screen, disappear
            }
        }
    }

    // Called by GamePanel every time player lands a hit on boss
    public void registerHit() {
        if (!fireballCharged) {
            hitCount++;
            if (hitCount >= hitsNeeded) {
                fireballCharged = true;
                hitCount = hitsNeeded; // cap it
            }
        }
    }

    public void draw(Graphics2D g2) {

        // Draw laser
        if (laserActive) {
            g2.setColor(new Color(80, 180, 255, 80));
            g2.fillRect(x + 60, laserY - 10, 800, 20);
            g2.setColor(new Color(100, 200, 255, 200));
            g2.fillRect(x + 60, laserY - 4, 800, 8);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(x + 60, laserY - 1, 800, 2);
        }

        // Draw fireball — glowing orange circle with bright core
        if (fireballActive) {
            // Outer glow
            g2.setColor(new Color(255, 120, 0, 80));
            g2.fillOval(fireballX - 15, fireballY - 15, 50, 50);
            // Mid layer
            g2.setColor(new Color(255, 160, 0, 180));
            g2.fillOval(fireballX - 8, fireballY - 8, 36, 36);
            // Bright core
            g2.setColor(new Color(255, 230, 100, 255));
            g2.fillOval(fireballX, fireballY, 20, 20);
        }

        // Draw player sprite
        g2.drawImage(playerImage, x, y,80,80,null);

        // Red hit flash
        if (hitFlashTimer > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
            g2.setColor(Color.RED);
            g2.fillRect(x, y, 60, 60);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public Rectangle getLaserBounds() {
        if (laserActive) return new Rectangle(x + 60, laserY - 4, 800, 8);
        return null;
    }

    // Fireball hitbox — matches the mid layer circle
    public Rectangle getFireballBounds() {
        if (fireballActive) return new Rectangle(fireballX - 8, fireballY - 8, 36, 36);
        return null;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 15, y + 15, 30, 30);
    }
}