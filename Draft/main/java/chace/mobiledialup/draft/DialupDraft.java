package chace.mobiledialup.draft;

import com.sun.media.sound.WaveFileWriter;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is my sketch of how I envision to put this together.
 * I will put some methods that will help me later on in the future when this idea becomes a reality,
 * people can look at this later on and see how I came up with the idea of doing this over mobile voice.
 *
 * Created by Chace Zwagerman 11/26/2021
 *
 * How does this work?
 *
 * Well its simple, I plan on converting text to binary then a DMTF tone will correspond to the binary value a 1 or a 0,
 * It will play the tone to the phone and the receipient will understand it and write it out.
 */
public class DialupDraft extends JFrame {

    private static Thread soundThread = null;

    /**
     * A hidden JFrame is required to play sounds for some unknown reason, there is probably a way around this but this is a draft
     * so I really don't care if theres a JFrame for the testing part.
     *
     * Applet#audioclip is also not working properly so had to use javaxsound#audioclip
     */
    public DialupDraft() {
        super("dialupdraft");
        super.setVisible(false);
    }

    public static void main(String[] args) throws InterruptedException, LineUnavailableException {
        new DialupDraft();
        encodeToDMTFandPlay("1111010000100101110");
    }
    /**
     * Takes in some data and plays it as a DMTF sound.
     * @param dataToEncode
     */
    private static void encodeToDMTFandPlay(String dataToEncode) throws LineUnavailableException, InterruptedException {

        playSound("/DTMF-dialTone.wav");

        String[] bits = dataToEncode.split("");

        for (String bit : bits) {
            if (bit.equals("0")) playSound("/dtmf-0.wav");
            if (bit.equals("1")) playSound("/dtmf-1.wav");
        }
    }

    /**
     * Takes a string of data and converts it to a DMTF tone that the phone can hear and understand.
     * This is done by converting it to DMTFF binary values 1 & 0.
     * @param dataToEncode
     * @return
     */
    private static File encodeToDMTF(String dataToEncode) {

        File file = null;

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new FileInputStream(file));

            WaveFileWriter waveFileWriter = new WaveFileWriter();
            waveFileWriter.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Method to play sounds.
     * @param url
     */
    public static synchronized void playSound(final String url) throws InterruptedException {
        CountDownLatch syncLatch = new CountDownLatch(1);

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(DialupDraft.class.getResource(url))) {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    syncLatch.countDown();
                }
            });
            clip.open(stream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        syncLatch.await();
    }
}
