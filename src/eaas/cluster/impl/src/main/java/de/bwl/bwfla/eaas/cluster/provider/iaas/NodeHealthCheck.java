/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.eaas.cluster.provider.iaas;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfig;
import de.bwl.bwfla.eaas.cluster.provider.Node;

// package-private

/** A helper class for performing health checks on nodes */
class NodeHealthCheck
{
	public static NodeState run(NodeInfo info, NodeAllocatorConfig config)
	{
		return NodeHealthCheck.run(info, config, false);
	}

	public static NodeState run(NodeInfo info, NodeAllocatorConfig config, boolean ignoreFailure)
	{
		return NodeHealthCheck.run(info, config, null, ignoreFailure);
	}

	public static NodeState run(NodeInfo info, NodeAllocatorConfig config, Logger log)
	{
		return NodeHealthCheck.run(info, config, log, false);
	}

	public static NodeState run(NodeInfo info, NodeAllocatorConfig config, Logger log, boolean ignoreFailure)
	{
		final Node node = info.getNode();
		final URL url = info.getHealthCheckUrl();
		HttpURLConnection connection = null;
		int code = 0;
		
		try {
			// Get the node's health
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout((int) config.getHealthCheckConnectTimeout());
			connection.setReadTimeout((int) config.getHealthCheckReadTimeout());
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.connect();
			code = connection.getResponseCode();
		}
		catch (Exception exception) {
			if (log != null) {
				String message = "Health checking for '" + url.toString() + "' failed!\n";
				log.log(Level.WARNING, message, exception);
			}
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		
		// Node reachable?
		if (code == HttpURLConnection.HTTP_OK) {
			info.setNodeState(NodeState.REACHABLE);
			info.resetUnreachableTimestamp();
			node.setHealthy(true);
		}
		else {
			// Healthcheck failed!
			
			if (code != 0) {
				try {
					final String message = "Health check for '" + url.toString()
						+ "' failed with: " + code + " " + connection.getResponseMessage();
					
					log.warning(message);
				}
				catch (Throwable throwable) {
					// Ignore it!
				}
			}
			
			final long curtime = System.currentTimeMillis();
			final NodeState curstate = info.getNodeState();
			if (curstate == NodeState.REACHABLE || curstate == NodeState.UNKNOWN) {
				info.setNodeState(NodeState.UNREACHABLE);
				info.setUnreachableTimestamp(curtime);
				node.setHealthy(false);
			}

			if (!ignoreFailure) {
				// Is the node unreachable long enough?
				final long elapsed = curtime - info.getUnreachableTimestamp();
				if (elapsed >= config.getHealthCheckFailureTimeout())
					info.setNodeState(NodeState.FAILED);  // Yes
			}
		}
		
		return info.getNodeState();
	}
}
