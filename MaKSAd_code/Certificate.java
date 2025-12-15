import java.time.LocalDate;
/* Represents a certificate awarded to a volunteer after completing an event. */
public class Certificate { // [**User registration**]

    // Unique ID of the volunteer who earned the certificate
    private final int volunteerId;

    // Full name of the volunteer 
    private final String volunteerName;

    // Title of the event for which the certificate was awarded
    private final String eventName;

    // Number of hours the volunteer earned from the event
    private final double hours;

    // Date when the certificate was issued
    private final LocalDate issueDate;

    // Creates a certificate for a volunteer, linked to a specific event and number of hours
    public Certificate(int volunteerId, String volunteerName,
                       String eventName, double hours) {
        this.volunteerId   = volunteerId;
        this.volunteerName = volunteerName;
        this.eventName     = eventName;
        this.hours         = hours;
        this.issueDate     = LocalDate.now(); // Auto-sets the issue date to today's date
    }

    // Note: All fields are final → certificates cannot be modified after creation
    public int getVolunteerId()      { return volunteerId; }
    public String getVolunteerName() { return volunteerName; }
    public String getEventName()     { return eventName; }
    public double getHours()         { return hours; }
    public LocalDate getIssueDate()  { return issueDate; }

    // Used when displaying certificate in UI lists or logs
    @Override
    public String toString() {
        return volunteerName + " - " + eventName + " (" + issueDate + ")";
    }
}

