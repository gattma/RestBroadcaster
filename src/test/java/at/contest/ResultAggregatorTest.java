package at.contest;

import at.contest.passthrough.model.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResultAggregatorTest {

    @Test
    public void test() {
        List<Response> responses = new ArrayList<>() {{
            add(new Response(1L, Response.Type.OK, "endpoint1", "dummy", LocalDateTime.now(), ""));
            add(new Response(2L, Response.Type.OK, "endpoint1", "dummy", LocalDateTime.now().plusSeconds(3), ""));
            add(new Response(2L, Response.Type.OK, "endpoint2", "dummy", LocalDateTime.now().plusSeconds(3), ""));
            add(new Response(1L, Response.Type.OK, "endpoint2", "dummy", LocalDateTime.now().plusSeconds(5), ""));
            add(new Response(3L, Response.Type.OK, "endpoint1", "dummy", LocalDateTime.now().plusSeconds(7), ""));
            add(new Response(3L, Response.Type.OK, "endpoint1", "dummy", LocalDateTime.now().plusSeconds(7), ""));
        }};

        responses.sort(Comparator.comparing(x -> x.id));
        responses.forEach(x -> System.out.printf("%s %s %s%n", x.getId(), x.getEndpoint(), x.getDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME)));
    }

}
