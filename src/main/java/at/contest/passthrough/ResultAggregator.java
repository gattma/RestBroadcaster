package at.contest.passthrough;

import at.contest.passthrough.model.Response;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResultAggregator {

    private final List<Response> responses;

    public ResultAggregator() {
        this.responses = new ArrayList<>();
    }

    public void addResponse(String endpoint, String responseAsJson) {
        responses.add(new Response(
                Response.Type.OK,
                extractHost(endpoint),
                extractSearchString(endpoint),
                LocalDateTime.now(),
                responseAsJson
        ));
    }

    public void addError(String endpoint, String errorAsJson) {
        responses.add(new Response(
                Response.Type.ERROR,
                extractHost(endpoint),
                extractSearchString(endpoint),
                LocalDateTime.now(),
                errorAsJson
        ));
    }

    public List<Response> getResponses() {
        return responses;
    }

    public Map<String, List<Response>> getResponsesByHost() {
        return responses
                .stream()
                .collect(Collectors.groupingBy(Response::getEndpoint));
    }

    private String extractSearchString(String endpoint) {
        return StringUtils.substringAfter(endpoint, "?text=");
    }

    private String extractHost(String endpoint) {
        return StringUtils.substringBefore(endpoint, "search?text=");
    }
}
