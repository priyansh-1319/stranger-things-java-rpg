import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Enemy {

    int x = 500;
    int y = 250;
    int speedX = 2;
    int speedY = 2;
    int health = 50;
    int maxHealth = 50;
    int hitFlashTimer = 0;

    // Laser fields
    boolean laserActive = false;
    int laserTimer = 0;
    int laserCooldown = 180;
    int laserDuration = 40;
    int laserY = 0;

    // Charge fields
    String state = "roaming";
    int stateTimer = 0;
    int chargeCooldown = 240;
    int chargeTargetX, chargeTargetY;
    int chargeDX, chargeDY;
    int chargeSpeed = 12;

    BufferedImage bossImage;
    Random random = new Random();

    public Enemy() {
        try {
            bossImage = ImageIO.read(new File("assets/boss.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(int playerX, int playerY) {

        if (hitFlashTimer > 0) hitFlashTimer--;

        if (state.equals("roaming")) {

            x += speedX;
            y += speedY;
            if (x < 0 || x > 650) speedX = -speedX;
            if (y < 0 || y > 450) speedY = -speedY;
            if (random.nextInt(120) == 0) speedX = -speedX;
            if (random.nextInt(120) == 0) speedY = -speedY;

            // Laser logic
            if (laserActive) {
                laserTimer--;
                if (laserTimer <= 0) {
                    laserActive = false;
                    laserCooldown = 150 + random.nextInt(60);
                }
            } else {
                laserCooldown--;
                if (laserCooldown <= 0) {
                    laserActive = true;
                    laserTimer = laserDuration;
                    laserY = y + 75;
                }
            }

            // Charge cooldown
            chargeCooldown--;
            if (chargeCooldown <= 0) {
                state = "warning";
                stateTimer = 60;
                chargeTargetX = playerX;
                chargeTargetY = playerY;
                laserActive = false;
            }

        } else if (state.equals("warning")) {

            stateTimer--;
            x += random.nextInt(3) - 1;
            y += random.nextInt(3) - 1;

            if (stateTimer <= 0) {
                double dist = Math.sqrt(
                    Math.pow(chargeTargetX - x, 2) +
                    Math.pow(chargeTargetY - y, 2)
                );
                if (dist > 0) {
                    chargeDX = (int)((chargeTargetX - x) / dist * chargeSpeed);
                    chargeDY = (int)((chargeTargetY - y) / dist * chargeSpeed);
                }
                state = "charging";
                stateTimer = 30;
            }

        } else if (state.equals("charging")) {

            x += chargeDX;
            y += chargeDY;

            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x > 650) x = 650;
            if (y > 450) y = 450;

            stateTimer--;
            if (stateTimer <= 0) {
                state = "recovering";
                stateTimer = 50;
            }

        } else if (state.equals("recovering")) {

            stateTimer--;
            if (stateTimer <= 0) {
                state = "roaming";
                chargeCooldown = 200 + random.nextInt(80);
                speedX = (random.nextBoolean() ? 1 : -1) * 2;
                speedY = (random.nextBoolean() ? 1 : -1) * 2;
            }
        }
    }

    public void draw(Graphics2D g2) {

        // Draw laser
        if (laserActive) {
            g2.setColor(new Color(255, 50, 50, 80));
            g2.fillRect(0, laserY - 10, 800, 20);
            g2.setColor(new Color(255, 80, 80, 200));
            g2.fillRect(0, laserY - 4, 800, 8);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(0, laserY - 1, 800, 2);
        }

        // Warning pulse
        if (state.equals("warning")) {
            int pulse = (stateTimer % 10 < 5) ? 80 : 40;
            g2.setColor(new Color(255, 100, 0, pulse));
            g2.fillOval(x - 20, y - 20, 190, 190);
            g2.setColor(new Color(255, 150, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("!", x + 65, y - 25);
        }

        // Draw boss sprite
        g2.drawImage(bossImage, x, y, 150, 150, null);

        // White flash on damage
        if (hitFlashTimer > 0) {
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_ATOP, 0.6f));
            g2.setColor(state.equals("charging") ?
                Color.ORANGE : Color.WHITE);
            g2.fillRect(x, y, 150, 150);
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));
        }

        // Orange tint while charging
        if (state.equals("charging")) {
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_ATOP, 0.35f));
            g2.setColor(Color.ORANGE);
            g2.fillRect(x, y, 150, 150);
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public Rectangle getLaserBounds() {
        if (laserActive) return new Rectangle(0, laserY - 4, 800, 8);
        return null;
    }

    public Rectangle getChargeBounds() {
        if (state.equals("charging"))
            return new Rectangle(x + 20, y + 20, 110, 110);
        return null;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 40, y + 60, 70, 70);
    }
}