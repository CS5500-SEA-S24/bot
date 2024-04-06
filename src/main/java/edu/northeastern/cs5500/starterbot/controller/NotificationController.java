package edu.northeastern.cs5500.starterbot.controller;

import com.mongodb.lang.Nullable;
import edu.northeastern.cs5500.starterbot.listener.ScheduledEventListener;
import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.model.Notification;
import edu.northeastern.cs5500.starterbot.model.User;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Singleton
@Slf4j
public class NotificationController {
    GenericRepository<Notification> notificationRepository;
    EventController eventController;
    UserController userController;
    ScheduledEventListener scheduledEventListener;

    @Nullable Date nextNotification;
    ScheduledExecutorService scheduler;
    ScheduledFuture<?> pendingTask;

    @Inject
    public NotificationController(
            GenericRepository<Notification> notificationRepository,
            EventController eventController,
            UserController userController,
            ScheduledEventListener scheduledEventListener) {
        this.notificationRepository = notificationRepository;
        this.eventController = eventController;
        this.userController = userController;
        this.scheduledEventListener = scheduledEventListener;
        nextNotification = null;
        scheduler = Executors.newScheduledThreadPool(1);
        sendPending();
    }

    /** Must be called periodically in order to ensure events get sent */
    public void sendPending() {
        Date now = new Date();
        Map<ObjectId, Event> events = new HashMap<>();
        Map<ObjectId, Boolean> eventCancelled = new HashMap<>();
        for (Notification notification : notificationRepository.getAll()) {
            Date sendTime = notification.getSendTime();
            if (now.before(sendTime)) {
                if (nextNotification.after(sendTime)) {
                    nextNotification = sendTime;
                }
                continue;
            }
            ObjectId eventId = notification.getEventId();
            if (eventCancelled.containsKey(eventId)) continue;
            Event event =
                    events.computeIfAbsent(eventId, (key) -> eventController.getEventById(key));
            if (event == null) {
                eventCancelled.put(eventId, true);
                continue;
            }

            // event exists and it's probably time to send a notification
            // TODO: this assumes that notification send time is always the same as event start time
            if (now.before(event.getStartTime())) continue;

            User user = userController.getUserById(notification.getUserId());
            if (user == null) {
                log.error(
                        "Tried to send notification to non-existent user id: "
                                + notification.getUserId());
                continue;
            }

            try {
                // event exists and it's ABSOLUTELY time to send a notification!
                scheduledEventListener.onNotificationTime(user, event);
            } catch (Exception e) {
                log.error("Unable to deliver notification to user", e);
            } finally {
                notificationRepository.delete(notification.getId());
            }
        }

        // Only schedule a new task if there are pending notifications
        if (nextNotification != null) updateScheduledTask(nextNotification);
    }

    public boolean cancelPending(@Nonnull ObjectId eventId) {
        boolean didCancel = false;
        for (Notification notification : notificationRepository.getAll()) {
            if (eventId.equals(notification.getEventId())) {
                notificationRepository.delete(notification.getId());
                didCancel = true;
            }
        }
        return didCancel;
    }

    boolean updateScheduledTask(@Nonnull Date eventStartTime) {
        assert eventStartTime != null;
        if (nextNotification == null || eventStartTime.before(nextNotification)) {
            nextNotification = eventStartTime;
            if (pendingTask != null) pendingTask.cancel(false);
            pendingTask =
                    scheduler.schedule(
                            this::sendPending,
                            nextNotification.getTime() - (new Date()).getTime(),
                            TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    public void schedule(@Nonnull Event event) {
        Date eventStartTime = event.getStartTime();
        ObjectId eventId = event.getId();
        cancelPending(eventId);

        for (ObjectId userId : event.getAttendees()) {
            User user = userController.getUserById(userId);
            if (user == null) {
                log.error("Tried to schedule notification for non-existent user id: " + userId);
                continue;
            }

            Notification notification =
                    Notification.builder()
                            .eventId(eventId)
                            .userId(userId)
                            .sendTime(eventStartTime)
                            .build();
            notificationRepository.add(notification);
        }

        updateScheduledTask(eventStartTime);
    }
}
