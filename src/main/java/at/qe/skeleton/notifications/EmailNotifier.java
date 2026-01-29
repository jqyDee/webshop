package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier extends AbstractNotifier {
    public EmailNotifier() {
        super(NotificationType.EMAIL);
    }
    protected void sendImplementation(String message, Userx user) {
        System.out.println("Sending email to " + user.getEmail());
        System.out.println(message);
    }
}
