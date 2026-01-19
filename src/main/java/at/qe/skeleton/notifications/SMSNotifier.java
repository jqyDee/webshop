package at.qe.skeleton.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;

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
