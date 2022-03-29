package at.contest.passthrough;

import at.contest.passthrough.application.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.*;

public class BroadcastManager extends HttpServlet {

    @Inject
    Logger logger;

    @Inject
    Configuration config;

    @Inject
    ResultAggregator resultAggregator;

    private final ExecutorService executorService;

    private final ObjectMapper mapper;

    private Long currentId;

    public BroadcastManager() {
        executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        mapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .build();
        currentId = 0L;
    }

    @PostConstruct
    public void logSetup() {
        logger.info(config.toString());
    }

    // TODO POST: https://stackoverflow.com/questions/8100634/get-the-post-request-body-from-httpservletrequest
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if(logger.isTraceEnabled()) traceRequestInformation(req);
        if(req.getHeader("readResults") != null) {
            logger.debug("Results requested...");
            returnResults(res);
        } else if(req.getHeader("readResultsByHosts")  != null) {
            logger.debug("Results by host requested...");
            returnResultsByHosts(res);
        } else {
            logger.debug("Passthrough...");
            passRequest(req, res);
        }
    }

    private void returnResults(HttpServletResponse res) {
        try {
            logger.infof("Current result size: %s", resultAggregator.getResponses().size());
            var result = mapper.writeValueAsString(resultAggregator.getResponses());
            writeResult(HttpServletResponse.SC_OK, result, res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnResultsByHosts(HttpServletResponse res) {
        try {
            logger.infof("Current result size: %s", resultAggregator.getResponses().size());
            var result = mapper.writeValueAsString(resultAggregator.getResponsesByHost());
            writeResult(HttpServletResponse.SC_OK, result, res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void passRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            var result = call(req.getPathInfo(), req.getMethod(), req.getQueryString());
            writeResult(HttpServletResponse.SC_OK, result, res);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
            writeResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), res);
        }
    }

    private String call(String path, String method, String query) throws InterruptedException, ExecutionException {
        currentId += 1;
        var leader = config.getLeader();
        logger.infof("Calling leader %s...", leader);
        var leaderResultFuture = executorService.submit(
                new CallHttpEndpointWorker(currentId, String.format("%s%s?%s", leader, path, query), method, logger, resultAggregator)
        );

        // Broadcast
        config.getBroadcasts().forEach(broadcast -> {
            logger.infof("Calling %s...", broadcast);
            executorService.submit(
                    new CallHttpEndpointWorker(currentId, String.format("%s%s?%s", broadcast, path, query), method, logger, resultAggregator)
            );
        });

        // get result of leader
        var result = leaderResultFuture.get();
        logger.infof("Got result from leader (%s): %s", leader, result);
        return result;
    }

    private void writeResult(int status, String result, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setStatus(status);
        var pw = res.getWriter();
        pw.println(result);
        pw.flush();
    }

    private void traceRequestInformation(HttpServletRequest req) {
        logger.debugf("PathInfo: %s", req.getPathInfo());
        logger.debugf("PathTranslated: %s", req.getPathTranslated());
        logger.debugf("ServletPath: %s", req.getServletPath());
        logger.debugf("ContextPath: %s", req.getContextPath());
        logger.debugf("RequestURL: %s", req.getRequestURL());
        logger.debugf("Method: %s", req.getMethod());
        logger.debugf("QueryString: %s", req.getQueryString());
        logger.debugf("RequestURI: %s", req.getRequestURI());

    }

}
