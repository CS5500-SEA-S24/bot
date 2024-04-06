package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Event;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class EventController {
    GenericRepository<Event> eventRepository;

    @Inject
    public EventController(GenericRepository<Event> eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event getEventById(ObjectId id) {
        return eventRepository.get(id);
    }

    public Event addEvent(Event event) {
        return eventRepository.add(event);
    }
}
