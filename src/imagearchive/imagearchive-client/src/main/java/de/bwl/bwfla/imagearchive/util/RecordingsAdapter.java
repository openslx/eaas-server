package de.bwl.bwfla.imagearchive.util;

import java.util.List;

import de.bwl.bwfla.api.imagearchive.IwdMetaData;
import de.bwl.bwfla.common.exceptions.BWFLAException;

public class RecordingsAdapter extends ImageArchiveWSClient
{
	private static final long serialVersionUID = 1L;

	public RecordingsAdapter(String wsHost)
	{
		super(wsHost);
	}
	
	public List<IwdMetaData> getRecordings(String env) throws BWFLAException
	{
		connectArchive();
		return archive.getRecordings(env);
	}
	
	public boolean addRecording(String envId, String traceId, String data) throws BWFLAException {
		return archive.addRecordingFile(envId, traceId, data);
	}
	
	public String getRecording(String envId, String traceId) throws BWFLAException
	{
		this.connectArchive();
		return archive.getRecording(envId, traceId);
	}
}
