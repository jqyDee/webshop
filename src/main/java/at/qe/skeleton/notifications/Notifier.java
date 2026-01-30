package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;

/**
 * Interface of a notifier with a specific type, sending message via medium {@link NotificationType}
 * to a user.
 */
public interface Notifier {
    NotificationType getNotificationType();
    void send(String message, Userx user);
}
