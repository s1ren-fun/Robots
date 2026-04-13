package networkrace;

import Localization.LocalizationManager;
import log.Logger;

/**
 * Слушатель сетевых событий, делегирующий обработку в {@link NetworkGameAdapter}.
 */
public class NetworkGameListener implements Listener {
    private final NetworkGameAdapter adapter;

    public NetworkGameListener(NetworkGameAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onStart(int fieldWidth, int fieldHeight, int targetX, int targetY,
                        int p1x, int p1y, int p2x, int p2y, int targetRadius, int step) {
        adapter.handleStart(targetX, targetY, p1x, p1y, p2x, p2y, targetRadius);
    }

    @Override
    public void onState(int p1x, int p1y, int p2x, int p2y, int targetX, int targetY) {
        adapter.handleState(p1x, p1y, p2x, p2y, targetX, targetY);
    }

    @Override
    public void onWin(int winnerId) {
        adapter.handleWin(winnerId);
    }

    @Override
    public void onInfo(String text) {
        String[] p = text.split("\\s+");
        LocalizationManager local = LocalizationManager.getInstance();
        Logger.debug("[INFO] " + (p.length > 1 ? local.getLocalizedMessage(p[1],Integer.parseInt(p[0])):
                local.getLocalizedMessage(p[0])));
    }

    @Override
    public void onDisconnected(String text) {
        adapter.handleDisconnected(text);
    }

    @Override
    public void onWaiting(String text) {
        adapter.handleWaiting(LocalizationManager.getInstance().getLocalizedMessage(text));
    }

    @Override
    public void onCloseRequest() {
        adapter.handleCloseRequest();
    }
}