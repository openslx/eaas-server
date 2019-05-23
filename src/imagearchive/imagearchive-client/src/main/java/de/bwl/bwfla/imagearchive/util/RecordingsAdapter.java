package de.bwl.bwfla.imagearchive.util;

import java.util.List;

import de.bwl.bwfla.api.imagearchive.IwdMetaData;
import de.bwl.bwfla.common.exceptions.BWFLAException;

public class RecordingsAdapter extends ImageArchiveWSClient
{
	private static final long serialVersionUID = 1L;

	public RecordingsAdapter(String wsHost)
	{
		super(wsHost, null);
	}

	public List<IwdMetaData> getRecordings(String env) throws BWFLAException
	{
		return this.getRecordings(this.getDefaultBackendName(), env);
	}

	public List<IwdMetaData> getRecordings(String backend, String env) throws BWFLAException
	{
		connectArchive();
		return archive.getRecordings(backend, env);
	}

	public boolean addRecording(String envId, String traceId, String data) throws BWFLAException {
		return this.addRecording(this.getDefaultBackendName(), envId, traceId, data);
	}

	public boolean addRecording(String backend, String envId, String traceId, String data) throws BWFLAException {
		return archive.addRecordingFile(backend, envId, traceId, data);
	}

	public String getRecording(String envId, String traceId) throws BWFLAException
	{
		return this.getRecording(this.getDefaultBackendName(), envId, traceId);
	}

	public String getRecording(String backend, String envId, String traceId) throws BWFLAException
	{
		this.connectArchive();
		return archive.getRecording(backend, envId, traceId);
	}
}
