package com.openslx.eaas.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.common.databind.JsonDataUtils;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner.Result;

import java.io.IOException;
import java.util.Optional;

/*
This class contains information that is returned when 'runc state {ID}' is executed. For more detailed information visit
https://github.com/opencontainers/runtime-spec/blob/main/runtime.md#state
 */
public class RuncStateInformation
{
	@JsonProperty("ociVersion")
	private String ociVersion;

	@JsonProperty("id")
	private String id;

	@JsonProperty("pid")
	private String pid;

	@JsonProperty("status")
	private String status;

	@JsonProperty("bundle")
	private String bundle;

	@JsonProperty("rootfs")
	private String rootfs;

	@JsonProperty("created")
	private String created;

	@JsonProperty("owner")
	private String owner;

	public RuncStateInformation()
	{
	}

	public String getOciVersion()
	{
		return ociVersion;
	}

	public void setOciVersion(String ociVersion)
	{
		this.ociVersion = ociVersion;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPid()
	{
		return pid;
	}

	public void setPid(String pid)
	{
		this.pid = pid;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getBundle()
	{
		return bundle;
	}

	public void setBundle(String bundle)
	{
		this.bundle = bundle;
	}

	public String getRootfs()
	{
		return rootfs;
	}

	public void setRootfs(String rootfs)
	{
		this.rootfs = rootfs;
	}

	public String getCreated()
	{
		return created;
	}

	public void setCreated(String created)
	{
		this.created = created;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public static RuncStateInformation getRuncStateInformationForComponent(String componentId) throws BWFLAException
	{
		int code = -1;
		String output = null;

		try {
			final var runcListRunner = new DeprecatedProcessRunner("sudo")
					.addArguments("runc", "state", "--", componentId);

			final var result = runcListRunner.executeWithResult(true).get();

			code = result.code();
			output = result.stdout();

			return DataUtils.json().read(output, RuncStateInformation.class);
		} catch (Exception e) {
			throw new BWFLAException("Could not determine runc state for given ID: " + componentId
					+ ", stdout of subprocess was: " + output + ", exit status: " + code, e);
		}
	}
}
