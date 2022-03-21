package at.contest.passthrough.application;

import lombok.Getter;
import lombok.ToString;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
@ToString
@Getter
public class Configuration {

    @Inject
    @ConfigProperty(name = "hosts.leader")
    String leader;

    @Inject
    @ConfigProperty(name = "hosts.broadcasts")
    Set<String> broadcasts;

}
