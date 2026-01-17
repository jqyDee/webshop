package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;

public interface Notifier {
    NotificationType getNotificationType();
    void send(String message, Userx user);
}
