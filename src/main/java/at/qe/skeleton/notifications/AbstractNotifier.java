package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;

/**
 * Abstract class implementing base function for every {@link Notifier} which checks {@link Userx}
 * notify options to contain given {@link NotificationType}. So notifier is only called when user
 * has activated receiving notifications for a type.
 * <p>
 * Abstracts the task of checking the users notify options. Only requiring the specific
 * {@link Notifier} to implement the sendImplementation method.
 */
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

    /**
     * Send the message over specified notifier by {@link Userx}.
     *
     * @param message message to be sent
     * @param user user to be notified
     */
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
