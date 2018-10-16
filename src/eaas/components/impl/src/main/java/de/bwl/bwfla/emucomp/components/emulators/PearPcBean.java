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

package de.bwl.bwfla.emucomp.components.emulators;
//package de.bwl.bwfla.emucomp.beans;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//import javax.ejb.Stateful;
//
//import de.bwl.bwfla.common.datatypes.BWFLAException;
//import de.bwl.bwfla.common.datatypes.Drive;
//import de.bwl.bwfla.common.datatypes.Nic;
//
///**
// * @author iv1004
// * 
// */
//@Stateful
//public class PearPcBean extends EmulatorBean {
//
//	private List<String> cmd = new ArrayList<String>();
//
//	@PostConstruct
//	protected void initialize() throws BWFLAException, IOException {
//		super.initialize();
//
//		File exec = new File(properties.getProperty("executable"));
//		if (exec == null || !exec.exists())
//			throw new BWFLAException("Sheepshaver executable not found! Make sure you have specified a valid path to "
//					+ "your executable in the corresponding 'properties' file");
//
//		cmd.add(exec.getAbsolutePath());
//		cmd.add(properties.getProperty("config"));
//
//		this.exeEnvironment.put("SDL_VIDEODRIVER", "vnc");
//		this.exeEnvironment.put("SDL_VNC_DISPLAY", String.valueOf(port - globalBeanAPI.getMinPort()));
//		
//        this.exeEnvironment.put("SDL_AUDIODRIVER", "disk");
//        this.exeEnvironment.put("SDL_DISKAUDIOTYPE", "pipe");
//        this.exeEnvironment.put("SDL_DISKAUDIOFILE", "/tmp/sdl_pipe_" + port);
//	}
//
//	@Override
//	public ProcessRunner getEmulatorRunner() {
//		ProcessRunner runner = new ProcessRunner(cmd);
//		runner.setStartingDir(new File(properties.getProperty("startingdir")));
//		log.info("Command: " + cmd.toString());
//		return runner;
//	}
//
//	@Override
//	public boolean addDrive(Drive d) {
//		// TODO: implement
//		return false;
//	}
//
//	@Override
//	public boolean connectDrive(Drive drive, boolean attach) {
//		log.warning("Hotplug is not supported by this emulator.");
//		return false;
//	}
//
//	@Override
//	protected void addNativeConfig(String config) {
//		// TODO: implement
//	}
//
//	@Override
//	protected boolean addNic(Nic nic) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//}
