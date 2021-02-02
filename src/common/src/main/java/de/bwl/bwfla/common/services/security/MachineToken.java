package de.bwl.bwfla.common.services.security;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;


/** Automatically refreshable machine-token */
public class MachineToken implements Supplier<String>
{
	private final Function<Duration, String> refresher;
	private final Duration lifetime;
	private long timestamp;
	private String token;

	private static final long LIFETIME_ADJUSTMENT = Duration.ofSeconds(30).toMillis();


	MachineToken(Duration lifetime, Function<Duration, String> refresher)
	{
		this.refresher = refresher;
		this.lifetime = lifetime;

		this.refresh();
	}

	@Override
	public String get()
	{
		if (MachineTokenProvider.time() > timestamp)
			this.refresh();

		return token;
	}

	private void refresh()
	{
		this.timestamp = MachineTokenProvider.time() + lifetime.toMillis() - LIFETIME_ADJUSTMENT;
		this.token = refresher.apply(lifetime);
	}
}
