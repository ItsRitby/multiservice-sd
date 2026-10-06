package co.edu.uptc.sd.multiservice.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class NodeIdentifier {

    @Value("${app.node.vm-hostname:unknown-vm}")
    private String vmHostname;

    @Value("${app.node.container-name:unknown-container}")
    private String containerName;

    public String getVmHostname() {
        return vmHostname;
    }

    public String getContainerName() {
        return containerName;
    }
}
