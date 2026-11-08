package sfx;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Random;

// clase reciclada y adaptada de otro proyecto para desarrollar este
public class SFXManager {
    private static SFXManager instance;
    private Clip mainTheme;
    private FloatControl mainVolumeControl;
    private boolean isInCheck = false;
    private Random random = new Random();
    
    // tiempo de la pista musical
    private long pausePosition = 0;
    
    // Configuración de fade
    private static final float LOW_VOLUME = -45.0f; // Volumen base más bajo
    private static final float MAIN_VOLUME = -8.0f;  // Volumen normal
    private static final int FADE_DURATION_MS = 1800;
    private static final int FADE_STEPS = 30;
    
    private SFXManager() {
        try {
            mainTheme = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static SFXManager getInstance() {
        if (instance == null) {
            instance = new SFXManager();
        }
        return instance;
    }
    
    public void playSong(String mainFile) {
        stopAllMusic();
        
        try {
            // Cargar y convertir tema principal
            InputStream mainSrc = getClass().getResourceAsStream("/music/" + mainFile);
            AudioInputStream originalMainStream = AudioSystem.getAudioInputStream(
                new BufferedInputStream(mainSrc));
            AudioFormat mainFormat = getSupportedFormat(originalMainStream.getFormat());
            AudioInputStream mainStream = AudioSystem.getAudioInputStream(mainFormat, originalMainStream);
            
            // Abrir ambos clips
            mainTheme.open(mainStream);
            
            // Configurar controles de volumen
            mainVolumeControl = (FloatControl) mainTheme.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Establecer volúmenes iniciales
            setVolume(mainVolumeControl, MAIN_VOLUME);
            
            // Iniciar tema 
            mainTheme.loop(Clip.LOOP_CONTINUOUSLY);
            
            // Cerrar streams
            mainStream.close();
            originalMainStream.close();
            
        } catch (Exception e) {
            System.err.println("Error al cargar música: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private AudioFormat getSupportedFormat(AudioFormat originalFormat) {
        // Convertir a PCM_SIGNED 16-bit que es ampliamente compatible
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            originalFormat.getSampleRate(),
            16, // bits por muestra
            originalFormat.getChannels(),
            originalFormat.getChannels() * 2, // bytes por frame (16 bits = 2 bytes)
            originalFormat.getSampleRate(),
            false // little endian
        );
    }
    
    private void setVolume(FloatControl control, float volume) {
        if (volume > control.getMaximum()) volume = control.getMaximum();
        if (volume < control.getMinimum()) volume = control.getMinimum();
        control.setValue(volume);
    }
    
    private void stopAllMusic() {
        if (mainTheme != null) mainTheme.stop();
    }
    
    public void playSound(String soundType) {
        String soundFile = getRandomSoundFile(soundType);
        if (soundFile == null) {
            System.err.println("Tipo de sonido no válido: " + soundType);
            return;
        }
        
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/sounds/" + soundFile);
            if (audioSrc == null) {
                // Si falla, intentar una ruta relativa sin la barra inicial
                audioSrc = getClass().getResourceAsStream("sounds/" + soundFile);
            }
            
            if (audioSrc == null) {
                // Intentar una tercera alternativa usando ClassLoader
                audioSrc = getClass().getClassLoader().getResourceAsStream("sounds/" + soundFile);
            }
            
            if (audioSrc == null) {
                System.err.println("No se pudo encontrar el archivo: " + soundFile);
                return;
            }
            
            // Convertir el audio a un formato compatible
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(
                new BufferedInputStream(audioSrc));
            AudioFormat targetFormat = getCompatibleFormat(originalStream.getFormat());
            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
            
            Clip clip = AudioSystem.getClip();
            clip.open(convertedStream);
            clip.start();
            
            // Cerrar streams
            originalStream.close();
            convertedStream.close();
        } catch (Exception e) {
            System.err.println("Error al reproducir el sonido " + soundFile + ": " + e.getMessage());
        }
    }
    
    private AudioFormat getCompatibleFormat(AudioFormat originalFormat) {
        // Convertir a PCM_SIGNED 16-bit que es ampliamente compatible
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            originalFormat.getSampleRate(),
            16, // bits por muestra
            originalFormat.getChannels(),
            originalFormat.getChannels() * 2, // bytes por frame (16 bits = 2 bytes)
            originalFormat.getSampleRate(),
            false // little endian
        );
    }
    
    public void setPaused(boolean pause) {
        if (mainTheme == null || !mainTheme.isOpen()) {
            return;
        }

        if (pause) {
            if (mainTheme.isRunning()) {
                pausePosition = mainTheme.getMicrosecondPosition();
                mainTheme.stop();
            }
        } else {
            if (!mainTheme.isRunning()) {
                mainTheme.setMicrosecondPosition(pausePosition);
                mainTheme.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }
    
    private String getRandomSoundFile(String soundType) {
        int variant = random.nextInt(4);
        
        switch(soundType.toLowerCase()) {
            case "delete": return "delete" + variant + ".wav";
            case "grab": return "grab" + variant + ".wav";
            case "put": return "put" + variant + ".wav";
            case "hover": return "hover.wav";  // Añadir directamente hover.wav
            case "click": return "click.wav";  // Añadir directamente click.wav
            default: return soundType;
        }
    }
}