package at.qe.skeleton.services;

import at.qe.skeleton.model.Userx;
import at.qe.skeleton.notifications.Notifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final List<Notifier> notifiers;

    @Autowired
    public NotificationService(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    public void notify(String message, Userx user) {
        for (Notifier notifier : notifiers) {
            notifier.send(message, user);
        }
    }
}
