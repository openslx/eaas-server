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

package de.bwl.bwfla.emucomp.ws;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.api.BindingDataHandler;
import de.bwl.bwfla.emucomp.api.EmulatorComponent;
import de.bwl.bwfla.emucomp.api.PrintJob;


@MTOM
@WebServlet("/ComponentService/Machine")
@WebService(targetNamespace="http://bwfla.bwl.de/api/emucomp")
public class Machine
{	
    @Inject
    protected NodeManager nodeManager;
    
	public void start(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.start();
		
//		Runnable task = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				emul.start();
//			}
//		};
//		
//		EmucompSingleton.executor.submit(task);
	}
	
	public String stop(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.stop();
		
//		Runnable task = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				emul.stop();
//			}
//		};
//		
//		EmucompSingleton.executor.submit(task);
	}

	public int changeMedium(String componentId, int containerId, String objReference) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.changeMedium(containerId, objReference);
	}
	
	public int attachMedium(String componentId, @XmlMimeType("application/octet-stream") DataHandler data, String mediaType) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.attachMedium(data, mediaType);
	}
	
	public @XmlMimeType("application/octet-stream") DataHandler detachMedium(String componentId, int handle) throws BWFLAException
	{
		EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);		
		return emul.detachMedium(handle);
	}
	
	public String getRuntimeConfiguration(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);		
		return emul.getRuntimeConfiguration();
	}
	
	public Set<String> getColdplugableDrives(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getColdplugableDrives();
	}
	
	public Set<String> getHotplugableDrives(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getHotplugableDrives();
	}

	public List<BindingDataHandler> snapshot(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.snapshot();
	}
	
	public String getEmulatorState(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getEmulatorState();
	}


	/* ==================== EmuCon API ==================== */

	public @XmlMimeType("application/octet-stream") DataHandler checkpoint(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.checkpoint();
	}
	
	
	/* ==================== Session recording API ==================== */
	
	public boolean prepareSessionRecorder(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.prepareSessionRecorder();
	}
	
	public void startSessionRecording(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.startSessionRecording();
	}
	
	public void stopSessionRecording(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.stopSessionRecording();
	}
	
	public boolean isRecordModeEnabled(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.isRecordModeEnabled();
	}
	
	public void addActionFinishedMark(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.addActionFinishedMark();
	}
	
	public void defineTraceMetadataChunk(String componentId, String tag, String comment) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.defineTraceMetadataChunk(tag, comment);
	}
	
	public void addTraceMetadataEntry(String componentId, String ctag, String key, String value) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.addTraceMetadataEntry(ctag, key, value);
	}
	
	public String getSessionTrace(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getSessionTrace();
	}
	
	
	/* ==================== Session replay API ==================== */
	
	public boolean prepareSessionPlayer(String componentId, String trace, boolean headless) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.prepareSessionPlayer(trace, headless);
	}
	
	public int getSessionPlayerProgress(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getSessionPlayerProgress();
	}
	
	public boolean isReplayModeEnabled(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.isReplayModeEnabled();
	}
	
	
	/* ==================== Monitoring API ==================== */
	
	public boolean updateMonitorValues(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.updateMonitorValues();
	}
	
	public String getMonitorValue(String componentId, ProcessMonitorVID id) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getMonitorValue(id);
	}
	
	public List<String> getMonitorValues(String componentId, Collection<ProcessMonitorVID> ids) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getMonitorValues(ids);
	}

	public List<String> getAllMonitorValues(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getAllMonitorValues();
	}
	
	
	/* ==================== Print API ==================== */

	public List<PrintJob> getPrintJobs(String componentId) throws BWFLAException {
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getPrintJobs();
	}

	/* ==================== Screenshot API ==================== */

	public void takeScreenshot(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		emul.takeScreenshot();
	}
	
	public @XmlMimeType("application/octet-stream") DataHandler getNextScreenshot(String componentId) throws BWFLAException
	{
		final EmulatorComponent emul = nodeManager.getComponentById(componentId, EmulatorComponent.class);
		return emul.getNextScreenshot();
	}
}
