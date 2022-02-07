/*
 * This file is part of PowerTunnel-AdBlock.
 *
 * PowerTunnel-AdBlock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel-AdBlock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel-AdBlock.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    public Boolean onResolutionRequest(@NotNull DNSRequest request) {
        if(request.getResponse() == null && adBlock.isBlocked(request.getHost())) {
            request.setResponse(LOCALHOST);
        }
        return super.onResolutionRequest(request);
    }
}
