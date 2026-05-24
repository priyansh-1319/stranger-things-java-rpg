import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("2D RPG Boss Fight");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        SoundManager sound = new SoundManager();

        IntroPanel introPanel = new IntroPanel(frame, sound);
        frame.add(introPanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        introPanel.startIntroThread();
    }
}
