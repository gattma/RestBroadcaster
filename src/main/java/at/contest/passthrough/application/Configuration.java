package at.contest.passthrough.application;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class Configuration {

    @Inject
    @ConfigProperty(name = "hosts.leader")
    String leader;

    @Inject
    @ConfigProperty(name = "hosts.broadcasts")
    Set<String> broadcasts;

    public String getLeader() {
        return leader;
    }

    public Set<String> getBroadcasts() {
        return broadcasts;
    }
}
