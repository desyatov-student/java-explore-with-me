package ru.practicum.ewm.event.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    private String title;
    private String description;
    @Column(name = "created_on")
    private Instant createdOn;
    @Column(name = "event_date")
    private Instant eventDate;
    @Column(name = "published_on")
    private Instant publishedOn;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private List<Request> requests;

    private Double latitude;
    private Double longitude;
    private Boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit = 0;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state = EventState.PENDING;
}