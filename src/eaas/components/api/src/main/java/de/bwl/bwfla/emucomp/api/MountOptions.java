package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import org.apache.tamaya.ConfigurationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MountOptions {
	private boolean readonly = false;
	private EmulatorUtils.XmountOutputFormat outFmt;
	private EmulatorUtils.XmountInputFormat inFmt;
	private long offset = 0;
	private long size = -1;
	
	protected final Logger log	= Logger.getLogger(this.getClass().getName());
	
	public void setOffset(long off) {
		this.offset = off;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setInFmt(EmulatorUtils.XmountInputFormat inFmt) {
		this.inFmt = inFmt;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public List<String> getArgs() {
		List<String> args = new ArrayList<>();

		if(size >= 0)
		{
			args.add("-s");
			args.add("" + size);
		}

		if(offset > 0)
		{
			args.add("-o--offset");
			args.add("-o" + offset);
		}
		return args;
	}
}
