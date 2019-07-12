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

package de.bwl.bwfla.emucomp.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;


/**
 * @author iv1004
 * 
 */
public interface EmulatorComponent extends ClusterComponent 
{	
	public void start() throws BWFLAException;
	public void stop() throws BWFLAException;
	public List<BindingDataHandler> snapshot() throws BWFLAException;
	public int changeMedium(int containerId, String objReference) throws BWFLAException;
	public int attachMedium(DataHandler data, String mediumType) throws BWFLAException;
	public DataHandler detachMedium(int containerId) throws BWFLAException;
	
	public String getRuntimeConfiguration() throws BWFLAException;
	
	public Set<String> getColdplugableDrives();
	public Set<String> getHotplugableDrives();
	
	public String getEmulatorState();

	/* ==================== EmuCon API ==================== */

	public DataHandler checkpoint() throws BWFLAException;

	
	/* =============== Session recording API =============== */
	
	public boolean prepareSessionRecorder() throws BWFLAException;
	public void startSessionRecording() throws BWFLAException;
	public void stopSessionRecording() throws BWFLAException;
	public boolean isRecordModeEnabled() throws BWFLAException;
	public void addActionFinishedMark() throws BWFLAException;
	public void defineTraceMetadataChunk(String tag, String comment) throws BWFLAException;
	public void addTraceMetadataEntry(String ctag, String key, String value) throws BWFLAException;
	public String getSessionTrace() throws BWFLAException;
	
	
	/* =============== Session replay API =============== */
	
	public boolean prepareSessionPlayer(String trace, boolean headless) throws BWFLAException;
	public int getSessionPlayerProgress() throws BWFLAException;
	public boolean isReplayModeEnabled() throws BWFLAException;
	
	
	/* ==================== Monitoring API ==================== */
	
	public boolean updateMonitorValues() throws BWFLAException;
	public String getMonitorValue(ProcessMonitorVID id) throws BWFLAException;
	public List<String> getMonitorValues(Collection<ProcessMonitorVID> ids) throws BWFLAException;
	public List<String> getAllMonitorValues() throws BWFLAException;
	
	
	/* ==================== Print  API ==================== */
	public List<PrintJob> getPrintJobs() throws BWFLAException;


	/* ==================== Screenshot API ==================== */

	public void takeScreenshot() throws BWFLAException;
	public DataHandler getNextScreenshot() throws BWFLAException;
}
