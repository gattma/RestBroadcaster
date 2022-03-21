package at.contest.passthrough;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

public class CallHttpEndpointWorker implements Callable<String> {

    String endpoint;
    String method;
    Logger logger;
    ResultAggregator resultAggregator;

    public CallHttpEndpointWorker(String endpoint, String method, Logger logger, ResultAggregator resultAggregator) {
        this.endpoint = endpoint;
        this.method = method;
        this.logger = logger;
        this.resultAggregator = resultAggregator;
    }

    @Override
    public String call() {
        logger.debugf("Calling endpoint '%s'...", endpoint);
        String result;
        try {
            var url = new URL(endpoint);
            var conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Accept", "application/json");
            logger.debugf("ResponseCode from %s: %s", endpoint, conn.getResponseCode());

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = IOUtils.toString(conn.getInputStream(), Charset.defaultCharset());
                logger.debugf("Add new result for %s: %s", endpoint, result);
                resultAggregator.addResponse(endpoint, result);
            } else {
                result = IOUtils.toString(
                        conn.getErrorStream() == null ? conn.getInputStream() : conn.getErrorStream(),
                        Charset.defaultCharset()
                );

                logger.debugf("Add new error for %s: %s", endpoint, result);
                resultAggregator.addError(endpoint, result);
            }

            logger.infof("Result from %s: %s", endpoint, result);
        } catch (IOException e) {
            logger.errorf(e, "Exception for endpoint '%s'", endpoint);
            result = e.getMessage();
            resultAggregator.addError(endpoint, e.getMessage());
        }

        return result;
    }
}
