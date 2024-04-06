package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import edu.northeastern.cs5500.starterbot.listener.FakeScheduledEventListener;
import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.model.Notification;
import edu.northeastern.cs5500.starterbot.model.User;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationControllerTest {

    NotificationController notificationController;

    FakeScheduledEventListener scheduledEventListener;
    GenericRepository<Notification> notificationRepository;
    EventController eventController;
    UserController userController;

    @BeforeEach
    void beforeEach() {
        eventController = new EventController(new InMemoryRepository<>());
        userController = new UserController(new InMemoryRepository<>());
        notificationRepository = new InMemoryRepository<>();
        scheduledEventListener = new FakeScheduledEventListener();
        notificationController =
                new NotificationController(
                        notificationRepository,
                        eventController,
                        userController,
                        scheduledEventListener);
    }

    @Test
    void testThatNextNotificationTaskIsNotScheduledIfThereAreNoNotifications() {
        assertThat(notificationController.pendingTask).isNull();
    }

    @Test
    void testThatSchedulingAnEventCreatesAPendingTask() {
        User attendee = User.builder().discordUserId("1234").build();
        attendee = userController.addUser(attendee);
        Date now = new Date();
        // event starts one hour in the future
        Date startTime = new Date(now.getTime() + 60 * 60 * 1000);
        Event event =
                Event.builder()
                        .name("Test Event")
                        .attendees(List.of(attendee.getId()))
                        .startTime(startTime)
                        .build();
        event = eventController.addEvent(event);

        assertThat(notificationController).isNotNull();
        notificationController.schedule(event);
        assertThat(notificationController.pendingTask).isNotNull();
    }

    @Test
    void testThatNotificationsAreActuallySent() throws InterruptedException {
        User attendee = User.builder().discordUserId("1234").build();
        attendee = userController.addUser(attendee);
        Date now = new Date();
        // event started five seconds ago
        Date startTime = new Date(now.getTime() - 5 * 1000);
        Event event =
                Event.builder()
                        .name("Test Event")
                        .attendees(List.of(attendee.getId()))
                        .startTime(startTime)
                        .build();
        event = eventController.addEvent(event);

        assertThat(scheduledEventListener.getUsers()).isEmpty();
        assertThat(scheduledEventListener.getEvents()).isEmpty();
        assertThat(notificationController.pendingTask).isNull();
        notificationController.schedule(event);
        // time out after five seconds
        long timeoutTime = (new Date(now.getTime() + 5 * 1000)).getTime();
        while (notificationController.pendingTask != null
                && notificationController.pendingTask.isDone() == false) {
            if ((new Date()).getTime() > timeoutTime) {
                fail("Timeout exceeded waiting for notifications to be sent.");
            }
            Thread.sleep(50);
        }
        assertThat(scheduledEventListener.getUsers()).containsExactly(attendee);
        assertThat(scheduledEventListener.getEvents()).containsExactly(event);
    }
}
