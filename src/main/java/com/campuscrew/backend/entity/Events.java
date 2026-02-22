package com.campuscrew.backend.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "events")
public class Events {
    @jakarta.persistence.ManyToMany
    private java.util.List<AppUser> attendees;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;           //this will give a title to the event that will be taking place
    private String location;        //this will give the venue to the users
    private String description;     //this will give extra details to the user, for eg, asking them to bring their own laptops, instruments, etc, the extra details so to speak.
    private LocalDateTime dateTime; //this will tell the date and the time
    //getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public java.util.List<AppUser> getAttendees() {
        return attendees;
    }
    public void setAttendees(java.util.List<AppUser> attendees) {
        this.attendees = attendees;
    }
    public void addAttendee(AppUser user) {
        this.attendees.add(user);
    }
}