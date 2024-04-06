package edu.northeastern.cs5500.starterbot.listener;

import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.model.User;

public interface ScheduledEventListener {

    void onNotificationTime(User user, Event event);
}
