package io.github.krlvm.powertunnel.plugins.adblock;

import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAdapter;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import org.jetbrains.annotations.NotNull;

public class ProxyListener extends ProxyAdapter {

    private final PowerTunnelServer server;
    private final String[] blacklist;

    private final ProxyResponse response;

    public ProxyListener(PowerTunnelServer server, String[] blacklist) {
        this.server = server;
        this.blacklist = blacklist;

        this.response = server.getProxyServer().getResponseBuilder("", 403).build();
    }

    @Override
    public void onClientToProxyRequest(@NotNull ProxyRequest request) {
        if(request.isBlocked()) return;

        final String host;

        if(request.address() != null) {
            host = request.address().getHost();
        } else if(request.getUri() != null) {
            host = FullAddress.fromString(request.getUri()).getHost();
        } else if(request.headers().contains("Host")) {
            host = request.headers().get("Host");
        } else {
            return;
        }

        if(isBlocked(host)) {
            request.setResponse(response);
        }
    }


    private boolean isBlocked(final String host) {
        for (String s : blacklist) {
            if(host.endsWith(s)) return true;
        }
        return false;
    }
}
