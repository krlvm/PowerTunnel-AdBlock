package io.github.krlvm.powertunnel.plugins.adblock;

import io.github.krlvm.powertunnel.sdk.proxy.DNSRequest;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public class DNSListener extends ProxyAdapter {

    private static final InetSocketAddress LOCALHOST = new InetSocketAddress("127.0.0.1", 0);
    private final AdBlock adBlock;

    public DNSListener(AdBlock adBlock) {
        this.adBlock = adBlock;
    }

    @Override
    public boolean onResolutionRequest(@NotNull DNSRequest request) {
        if(request.getResponse() == null && adBlock.isBlocked(request.getHost())) {
            request.setResponse(LOCALHOST);
        }
        return super.onResolutionRequest(request);
    }
}
