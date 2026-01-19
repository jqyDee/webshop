package at.qe.skeleton.tests.notifications;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.notifications.EmailNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotifierTest {

    private EmailNotifier emailNotifier;
    private Userx user;

    @BeforeEach
    void setUp() {
        emailNotifier = new EmailNotifier();
        user = mock(Userx.class);
    }

    @Test
    void testGetNotificationType() {
        assertEquals(NotificationType.EMAIL, emailNotifier.getNotificationType());
    }

    @Test
    void testSendWhenUserHasEmailOption() {
        when(user.getNotifyOptions()).thenReturn(Set.of(NotificationType.EMAIL));
        when(user.getEmail()).thenReturn("test@example.com");

        assertDoesNotThrow(() -> emailNotifier.send("Hello", user));
        verify(user, atLeastOnce()).getEmail();
    }

    @Test
    void testSendWhenUserLacksEmailOption() {
        when(user.getNotifyOptions()).thenReturn(Set.of(NotificationType.SMS));

        emailNotifier.send("Hello", user);

        verify(user, never()).getEmail();
    }

    @Test
    void testSendThrowsOnNullUser() {
        assertThrows(IllegalArgumentException.class, () -> emailNotifier.send("Message", null));
    }

    @Test
    void testSendThrowsOnNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> emailNotifier.send(null, user));
    }
}