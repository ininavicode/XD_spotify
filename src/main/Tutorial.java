package main;

import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

public class Tutorial {

    private final AudioPlayerComponent mediaPlayerComponent;

    public static void main(String[] args) {
        Tutorial tutorial = new Tutorial();
        tutorial.start("data/ISABELLA.mp3");
        try {
            Thread.currentThread().join();
        }
        catch(InterruptedException e) {
        }
    }

    private Tutorial() {
        mediaPlayerComponent = new AudioPlayerComponent();
        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                exit(0);
            }
            @Override
            public void error(MediaPlayer mediaPlayer) {
                exit(1);
            }
        });
    }
    private void start(String mrl) {
        mediaPlayerComponent.mediaPlayer().media().play(mrl);
    }

    private void exit(int result) {
        // It is not allowed to call back into LibVLC from an event handling thread, so submit() is used
        mediaPlayerComponent.mediaPlayer().submit(new Runnable() {
            @Override
            public void run() {
                mediaPlayerComponent.mediaPlayer().release();
                System.exit(result);
            }
        });
    }
}
