package io.github.krlvm.powertunnel.plugins.adblock;

import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAdapter;
import org.jetbrains.annotations.NotNull;

public class RequestListener extends ProxyAdapter {

    private final AdBlock adBlock;
    private final ProxyResponse response;

    public RequestListener(AdBlock adBlock) {
        this.adBlock = adBlock;
        this.response = adBlock.getServer().getProxyServer().getResponseBuilder("", 403).build();
    }

    @Override
    public void onClientToProxyRequest(@NotNull ProxyRequest request) {
        if(request.isBlocked()) return;
        if(adBlock.isBlocked(request.getHost())) request.setResponse(response);
    }
}
