package edu.northeastern.cs5500.starterbot.listener;

import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.model.User;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

public class FakeScheduledEventListener implements ScheduledEventListener {
    @Getter Set<User> users;
    @Getter Set<Event> events;

    public FakeScheduledEventListener() {
        users = new HashSet<>();
        events = new HashSet<>();
    }

    @Override
    public void onNotificationTime(User user, Event event) {
        users.add(user);
        events.add(event);
    }
}
