package edu.northeastern.cs5500.starterbot.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Model {
    ObjectId id;

    String name;
    String description;
    Date startTime;
    Date endTime;

    ObjectId owner;
    List<ObjectId> attendees;
}
