package at.contest;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class ResultAggregatorTest {

    @Test
    public void test() {
        String endpoint = "http://localhost:8580/search?text=Kap";
        System.out.println(StringUtils.substringAfter(endpoint, "?text="));
    }

}
