package edu.northeastern.cs5500.starterbot.listener;

import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.model.User;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

@Slf4j
@Singleton
public class DiscordScheduledEventListener implements ScheduledEventListener {
    JDA jda;

    @Inject
    public DiscordScheduledEventListener(JDA jda) {
        this.jda = jda;
    }

    public static String formatNotification(Event event) {
        return String.format("Event %s is starting now!", event.getName());
    }

    @Override
    public void onNotificationTime(User user, Event event) {
        jda.openPrivateChannelById(user.getDiscordUserId())
                .queue(
                        privateChannel ->
                                privateChannel
                                        .sendMessage(formatNotification(event))
                                        .queue(ignored -> log.info("Notification sent!")));
    }
}
