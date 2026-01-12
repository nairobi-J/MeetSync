package com.root.meetsync.entity;

import jakarta.persistence.*;


import java.time.LocalDateTime;
@Entity
public class ConfirmedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private EventSlot selectedSlots;

    private LocalDateTime confirmedAt;
    private String additionalNote;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventSlot getSelectedSlots() {
        return selectedSlots;
    }

    public void setSelectedSlots(EventSlot selectedSlots) {
        this.selectedSlots = selectedSlots;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    public String getAdditionalNote() { return additionalNote; }
    public void setAdditionalNote(String additionalNote) { this.additionalNote = additionalNote; }
}
