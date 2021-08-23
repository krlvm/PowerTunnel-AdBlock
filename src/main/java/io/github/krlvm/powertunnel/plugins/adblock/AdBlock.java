/*
 * This file is part of LibertyTunnel.
 *
 * LibertyTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibertyTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.krlvm.powertunnel.plugins.adblock;

import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.utiities.TextReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AdBlock extends PowerTunnelPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdBlock.class);

    @Override
    public void onProxyInitialization(@NotNull ProxyServer proxy) {
        final Set<String> blacklist = new HashSet<>();
        try {
            final String res = TextReader.read(new URL("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts").openStream());
            final String[] arr = res.split("\n");

            for(int i = 40; i < arr.length; i++) {
                String s = arr[i];
                if(s.isEmpty() || s.startsWith("#")) continue;
                s = s.substring(8);
                if(s.startsWith("www.")) {
                    s = s.replaceFirst("www.", "");
                }
                blacklist.add(s);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to load blacklist: {}", ex.getMessage(), ex);
            return;
        }
        if(blacklist.isEmpty()) {
            LOGGER.info("Blacklist is empty");
            return;
        }

        LOGGER.info("Loaded {} hosts", blacklist.size());
        registerProxyListener(new ProxyListener(getServer(), blacklist.toArray(new String[0])), -10);
    }
}
