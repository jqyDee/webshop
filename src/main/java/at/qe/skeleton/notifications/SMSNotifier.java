package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;
import org.springframework.stereotype.Component;

/**
 * STUBBED: {@link Notifier} implementing sms notification.
 */
@Component
public class SMSNotifier extends AbstractNotifier{
    public SMSNotifier() {
        super(NotificationType.SMS);
    }

    @Override
    protected void sendImplementation(String message, Userx user) {
        System.out.println("Sending sms to " + user.getPhone());
        System.out.println(message);
    }
}
