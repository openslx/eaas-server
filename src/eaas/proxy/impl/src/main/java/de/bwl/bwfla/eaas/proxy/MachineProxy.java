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

package de.bwl.bwfla.eaas.proxy;

import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.api.emucomp.PrintJob;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.SessionRegistry;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.emucomp.api.BindingDataHandler;


@MTOM
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebServlet("/ComponentProxy/Machine")
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "MachineService", portName = "MachinePort")
public class MachineProxy implements Machine
{
    @Inject
    DirectComponentClient componentClient;

	@Inject
	private SessionRegistry sessions = null;

    protected Machine getMachine(String componentId) throws BWFLAException {
    	final SessionRegistry.Entry session = sessions.lookup(componentId);
    	if(session == null)
    		throw new BWFLAException("session for component id " + componentId + " not found.");
		final ResourceHandle resource = session.getResourceHandle();
        return componentClient.getMachinePort(resource.getNodeID());
    }
    
    @Override
    public void start(String componentId) throws BWFLAException
    {
        getMachine(componentId).start(componentId);
    }

    @Override
    public String stop(String componentId) throws BWFLAException
    {
        return getMachine(componentId).stop(componentId);
    }

	@Override
	public int changeMedium(String componentId, int arg1, String arg2) throws BWFLAException
	{
    	return getMachine(componentId).changeMedium(componentId, arg1, arg2);
	}
    
    @Override
    public int attachMedium(String componentId, @XmlMimeType("application/octet-stream") DataHandler arg1, String arg2) throws BWFLAException
    {
        return getMachine(componentId).attachMedium(componentId, arg1, arg2);
    }

    @Override
    public @XmlMimeType("application/octet-stream") DataHandler detachMedium(String componentId, int handle) throws BWFLAException 
    {
        return getMachine(componentId).detachMedium(componentId, handle);
    }

    @Override
    public String getRuntimeConfiguration(String componentId) throws BWFLAException
    {
    	return getMachine(componentId).getRuntimeConfiguration(componentId);
    }

    @Override
    public List<String> getColdplugableDrives(String componentId) throws BWFLAException
    {
        return getMachine(componentId).getColdplugableDrives(componentId);
    }

    @Override
    public List<String> getHotplugableDrives(String componentId) throws BWFLAException
    {
        return getMachine(componentId).getHotplugableDrives(componentId);
    }

    @Override
	public List<BindingDataHandler> snapshot(String componentId) throws BWFLAException
	{
		return getMachine(componentId).snapshot(componentId);
	}

    @Override
    public String getEmulatorState(String componentId) throws BWFLAException
    {
        return getMachine(componentId).getEmulatorState(componentId);
    }


	/* ==================== EmuCon API ==================== */

	@Override
	public @XmlMimeType("application/octet-stream") DataHandler checkpoint(String componentId) throws BWFLAException
	{
		return getMachine(componentId).checkpoint(componentId);
	}


    /* ==================== Session recording API ==================== */

	@Override
	public boolean prepareSessionRecorder(String componentId) throws BWFLAException 
	{
	    return getMachine(componentId).prepareSessionRecorder(componentId);
	}

    @Override
	public void startSessionRecording(String componentId) throws BWFLAException 
	{
        getMachine(componentId).startSessionRecording(componentId);
	}

	@Override
	public void stopSessionRecording(String componentId) throws BWFLAException 
	{
        getMachine(componentId).stopSessionRecording(componentId);
	}

	@Override
	public boolean isRecordModeEnabled(String componentId) throws BWFLAException 
	{
        return getMachine(componentId).isRecordModeEnabled(componentId);
	}

	@Override
	public void addActionFinishedMark(String componentId) throws BWFLAException 
	{
        getMachine(componentId).addActionFinishedMark(componentId);
	}

	@Override
	public void defineTraceMetadataChunk(String componentId, String tag, String comment) throws BWFLAException 
	{
        getMachine(componentId).defineTraceMetadataChunk(componentId, tag, comment);
	}

	@Override
	public void addTraceMetadataEntry(String componentId, String ctag, String key, String value) throws BWFLAException 
	{
        getMachine(componentId).addTraceMetadataEntry(componentId, ctag, key, value);
	}

	@Override
	public String getSessionTrace(String componentId) throws BWFLAException 
	{
	    return getMachine(componentId).getSessionTrace(componentId);
	}


	/* ==================== Session replay API ==================== */

	@Override
	public boolean prepareSessionPlayer(String componentId, String trace, boolean headless) throws BWFLAException 
	{
	    return getMachine(componentId).prepareSessionPlayer(componentId, trace, headless);
	}

	@Override
	public int getSessionPlayerProgress(String componentId) throws BWFLAException 
	{
	    return getMachine(componentId).getSessionPlayerProgress(componentId);
	}

	@Override
	public boolean isReplayModeEnabled(String componentId) throws BWFLAException 
	{
	    return getMachine(componentId).isReplayModeEnabled(componentId);
	}


	/* ==================== Monitoring API ==================== */

	@Override
	public boolean updateMonitorValues(String componentId) throws BWFLAException
	{
	    return getMachine(componentId).updateMonitorValues(componentId);
	}

	@Override
	public String getMonitorValue(String componentId, Integer vid) throws BWFLAException 
	{
	    return getMachine(componentId).getMonitorValue(componentId, vid);
	}

	@Override
	public List<String> getMonitorValues(String componentId, List<Integer> vids) throws BWFLAException 
	{
	    return getMachine(componentId).getMonitorValues(componentId, vids);
	}

	@Override
	public List<String> getAllMonitorValues(String componentId) throws BWFLAException 
	{
	    return getMachine(componentId).getAllMonitorValues(componentId);
	}
	
	
	/* ==================== Print API ==================== */

	@Override
	public List<PrintJob> getPrintJobs(String componentId) throws BWFLAException {
		return getMachine(componentId).getPrintJobs(componentId);
	}

	/* ==================== Screenshot API ==================== */

	@Override
	public void takeScreenshot(String componentId) throws BWFLAException
	{
        getMachine(componentId).takeScreenshot(componentId);
	}
	
	@Override
	public @XmlMimeType("application/octet-stream") DataHandler getNextScreenshot(String componentId) throws BWFLAException
	{
        return getMachine(componentId).getNextScreenshot(componentId);
	}
}