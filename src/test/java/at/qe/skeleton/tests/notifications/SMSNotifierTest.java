package at.qe.skeleton.tests.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.notifications.SMSNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SMSNotifierTest {

    private SMSNotifier smsNotifier;
    private Userx user;

    @BeforeEach
    void setUp() {
        smsNotifier = new SMSNotifier();
        user = mock(Userx.class);
    }

    @Test
    void testGetNotificationType() {
        assertEquals(NotificationType.SMS, smsNotifier.getNotificationType());
    }

    @Test
    void testSendWhenUserHasSMSOption() {
        when(user.getNotifyOptions()).thenReturn(Set.of(NotificationType.SMS));
        when(user.getPhone()).thenReturn("123456789");

        smsNotifier.send("SMS Content", user);

        verify(user, atLeastOnce()).getPhone();
    }

    @Test
    void testSendWhenUserLacksSMSOption() {
        when(user.getNotifyOptions()).thenReturn(Set.of(NotificationType.EMAIL));

        smsNotifier.send("SMS Content", user);

        verify(user, never()).getPhone();
    }
}