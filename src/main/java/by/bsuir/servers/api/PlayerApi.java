package by.bsuir.servers.api;

import by.bsuir.events.EventHandler;
import by.bsuir.events.PauseEvent;
import by.bsuir.messages.PauseMessage;

public class PlayerApi {
    private final PlayerApiProvider playerApiProvider;

    private EventHandler<PauseEvent> onPlayerPaused;

    public PlayerApi(PlayerApiProvider playerApiProvider) {
        this.playerApiProvider = playerApiProvider;
    }

    public void pause(PauseMessage pauseMessage) {
        playerApiProvider.pause(pauseMessage);
    }

    public EventHandler<PauseEvent> getOnPlayerPaused() {
        return onPlayerPaused;
    }

    public void setOnPlayerPaused(EventHandler<PauseEvent> onPlayerPaused) {
        this.onPlayerPaused = onPlayerPaused;
    }

    public interface PlayerApiProvider {

        PlayerApi getPlayerApi();

        void pause(PauseMessage pauseMessage);
    }
}
