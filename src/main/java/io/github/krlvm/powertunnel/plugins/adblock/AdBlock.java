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

    private String[] blacklist;

    @Override
    public void onProxyInitialization(@NotNull ProxyServer proxy) {
        final Configuration config = readConfiguration();
        final Set<String> blacklist = new HashSet<>();

        final long interval = config.getLong("last_mirror_load", 0);
        if ((System.currentTimeMillis() - interval) < getMirrorInterval(config.get("update_interval", "interval_2"))) {
            if (!loadFiltersFromCache(blacklist)) {
                loadFiltersFromMirror(blacklist, config, interval != 0);
            }
        } else {
            if (!loadFiltersFromMirror(blacklist, config, interval != 0)) {
                loadFiltersFromCache(blacklist);
            }
        }

        if(blacklist.isEmpty()) {
            LOGGER.info("Blacklist is empty");
            return;
        }

        LOGGER.info("Loaded {} hosts", blacklist.size());
        this.blacklist = blacklist.toArray(new String[0]);


        final FiltrationMode mode = FiltrationMode.valueOf(readConfiguration().get("mode", FiltrationMode.DNS.toString()).toUpperCase());

        final boolean filterDns = mode == FiltrationMode.DNS || mode == FiltrationMode.BOTH;
        final boolean filterProxy = mode == FiltrationMode.PROXY || mode == FiltrationMode.BOTH;

        if(filterDns) {
            registerProxyListener(new DNSListener(this), -10);
        }
        if(filterProxy) {
            registerProxyListener(new RequestListener(this), -10);
        }
    }

    protected boolean isBlocked(final String host) {
        if(blacklist == null || host == null) return false;
        for (String s : blacklist) {
            if(host.endsWith(s)) return true;
        }
        return false;
    }



    private boolean loadFiltersFromMirror(Set<String> blacklist, Configuration config, boolean caching) {
        LOGGER.info("Loading filters from mirror...");
        try {
            final String raw = TextReader.read(new URL("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts").openStream());
            parseFilters(blacklist, raw);

            if (caching) {
                try {
                    config.setLong("last_mirror_load", System.currentTimeMillis());
                    saveConfiguration();
                } catch (IOException ex) {
                    LOGGER.warn("Failed to save the time of the last load of the filters from the mirror: {}", ex.getMessage(), ex);
                }
                try {
                    saveTextFile("adblock-cache.txt", raw);
                } catch (IOException ex) {
                    LOGGER.warn("Failed to save cached filters: {}", ex.getMessage(), ex);
                }
            }

            return true;
        } catch (IOException ex) {
            LOGGER.warn("Failed to load filters: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private boolean loadFiltersFromCache(Set<String> blacklist) {
        LOGGER.info("Loading filters from cache...");
        try {
            parseFilters(blacklist, readTextFile("adblock-cache.txt"));
            return true;
        } catch (IOException ex) {
            LOGGER.error("Failed to read cached blacklist: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private void parseFilters(Set<String> blacklist, String raw) {
        final String[] arr = raw.split("\n");

        for(int i = 40; i < arr.length; i++) {
            String s = arr[i];
            if(s.isEmpty() || s.startsWith("#")) continue;
            s = s.substring(8);
            if(s.startsWith("www.")) {
                s = s.replaceFirst("www.", "");
            }
            blacklist.add(s);
        }
    }

    private static long getMirrorInterval(String key) {
        switch (key) {
            case "interval_5": return 3 * 24 * 60 * 60 * 1000;
            case "interval_4": return 2 * 24 * 60 * 60 * 1000;
            case "interval_3": return 24 * 60 * 60 * 1000;
            case "interval_1": return 0;
            default: case "interval_2": return 12 * 60 * 60 * 1000;
        }
    }
}
