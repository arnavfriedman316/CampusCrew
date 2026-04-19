package com.campuscrew.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    private String posterUrl;

    @jakarta.persistence.Lob
    @jakarta.persistence.Column(columnDefinition = "LONGBLOB")
    private byte[] posterData;

    private String posterType;

    private Boolean isTeamEvent = false;
    private Integer minTeamSize;
    private Integer maxTeamSize;

    @jakarta.persistence.OneToMany(mappedBy = "event", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TeamRegistration> teamRegistrations = new java.util.ArrayList<>();

    public byte[] getPosterData() {
        return posterData;
    }

    public void setPosterData(byte[] posterData) {
        this.posterData = posterData;
    }

    public String getPosterType() {
        return posterType;
    }

    public void setPosterType(String posterType) {
        this.posterType = posterType;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

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

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Boolean getIsTeamEvent() {
        return isTeamEvent;
    }

    public void setIsTeamEvent(Boolean isTeamEvent) {
        this.isTeamEvent = isTeamEvent;
    }

    public Integer getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(Integer minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public java.util.List<TeamRegistration> getTeamRegistrations() {
        return teamRegistrations;
    }

    public void setTeamRegistrations(java.util.List<TeamRegistration> teamRegistrations) {
        this.teamRegistrations = teamRegistrations;
    }
}
