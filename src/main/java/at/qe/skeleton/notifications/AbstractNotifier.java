package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;

public abstract class AbstractNotifier implements Notifier {
    private final NotificationType type;

    protected AbstractNotifier(NotificationType type) {
        this.type = type;
    }

    @Override
    public NotificationType getNotificationType() {
        return type;
    }

    protected abstract void sendImplementation(String message, Userx user);

    @Override
    public void send(String message, Userx user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message is null");
        }
        if (user.getNotifyOptions().contains(type)) {
            sendImplementation(message, user);
        }
    }
}
