import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {

    private Clip currentClip;

    public void play(String filePath, boolean loop) {
        try {
            stop();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                new File(filePath));
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            if (loop) {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentClip.start();
            }
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Audio format not supported: " + filePath);
        } catch (IOException e) {
            System.out.println("Could not find audio file: " + filePath);
        } catch (LineUnavailableException e) {
            System.out.println("Audio line unavailable: " + filePath);
        }
    }

    public void stop() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
        }
    }
}