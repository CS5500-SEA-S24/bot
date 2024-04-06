package edu.northeastern.cs5500.starterbot.model;

import java.util.Date;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Model {
    ObjectId id;

    @Nonnull Date sendTime;

    @Nonnull ObjectId userId;

    @Nonnull ObjectId eventId;
}
