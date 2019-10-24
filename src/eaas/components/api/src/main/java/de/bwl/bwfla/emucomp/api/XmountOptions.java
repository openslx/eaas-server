package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;

import java.util.logging.Logger;

public class XmountOptions {
	private boolean readonly = false;
	private EmulatorUtils.XmountOutputFormat outFmt;
	private EmulatorUtils.XmountInputFormat inFmt;
	private long offset = 0;
	private long size = -1;
	private String proxyUrl = null;

	protected final Logger log	= Logger.getLogger(this.getClass().getName());
	
	public XmountOptions() {
		this(EmulatorUtils.XmountOutputFormat.RAW);
	}

	public XmountOptions(EmulatorUtils.XmountOutputFormat outFmt)
	{
		this.outFmt = outFmt;
		inFmt = EmulatorUtils.XmountInputFormat.QEMU;
	}
	
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

	public EmulatorUtils.XmountInputFormat getInFmt() {
		return inFmt;
	}

	public EmulatorUtils.XmountOutputFormat getOutFmt()
	{
		return outFmt;
	}
	
	public void setXmountOptions(DeprecatedProcessRunner process)
	{
		if(offset > 0 && size < 0)
		{
			process.addArguments("--offset", "" + offset);
		}

		process.addArguments("--out", outFmt.toString());
		if (!readonly) {
			process.addArguments("--cache", "writethrough");
			process.addArguments("--inopts", "qemuwritable=true,bdrv_cache=writeback");
		}
		
		if(size >= 0)
		{
			process.addArguments("--morph", "trim");
			String morphOpts;
			if(offset == 0)
				morphOpts = "size=" + size;
			else
				morphOpts = "offset=" + offset +",size=" + size;
			process.addArguments("--morphopts", morphOpts);
		}

		String proxyUrl = MachineTokenProvider.getAuthenticationProxy();
		if(proxyUrl != null) {
			log.warning("using http_proxy");

			process.addEnvVariable("LD_PRELOAD", "/usr/local/lib/LD_PRELOAD_libcurl.so");
	 		process.addEnvVariable("prefix_proxy", proxyUrl);
		}
	}
}
