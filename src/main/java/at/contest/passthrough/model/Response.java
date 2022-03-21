package at.contest.passthrough.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(value = { "dateTime" })
public class Response {

    public enum Type {
        OK, ERROR
    }

    public Response(Type type, String endpoint, String searchString, LocalDateTime dateTime, String result) {
        this.type = type;
        this.endpoint = endpoint;
        this.searchString = searchString;
        this.dateTime = dateTime;
        this.dateTimeString = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"));
        this.result = result;
    }

    public Type type;
    public String endpoint;
    public String searchString;
    public LocalDateTime dateTime;
    public String dateTimeString;
    public String result;

    public Type getType() {
        return type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getResult() {
        return result;
    }
}
