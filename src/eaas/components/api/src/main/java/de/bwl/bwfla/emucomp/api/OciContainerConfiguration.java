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

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ociContainerConfiguration", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
		"process",
		"rootfs",
		"isGui",
		"customSubdir"
})
@XmlRootElement(name = "ociContainerConfiguration", namespace = "http://bwfla.bwl.de/common/datatypes")
public class OciContainerConfiguration extends ContainerConfiguration
{
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	protected Process process;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	protected String rootfs;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	protected boolean isGui = false;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	protected String customSubdir = null;

	public Process getProcess()
	{
		return process;
	}

	public void setProcess(Process process)
	{
		this.process = process;
	}

	public String getRootFilesystem()
	{
		return rootfs;
	}

	public void setRootFilesystem(String rootfs)
	{
		this.rootfs = rootfs;
	}

	public static OciContainerConfiguration fromValue(String data) throws JAXBException
	{
		return JaxbType.fromValue(data, OciContainerConfiguration.class);
	}

	public boolean isGui() {
		return isGui;
	}

	public void setGui(boolean gui) {
		isGui = gui;
	}

	public String getCustomSubdir() {
		return customSubdir;
	}

	public void setCustomSubdir(String customSubdir) {
		this.customSubdir = customSubdir;
	}

	public OciContainerConfiguration copy()
	{
		try {
			return OciContainerConfiguration.fromValue(this.value());
		}
		catch (JAXBException e) {
			Logger.getLogger(OciContainerConfiguration.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}


	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "process")
	public static class Process
	{
		@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", name = "env")
		protected List<String> envs;

		@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", name = "arg")
		protected List<String> args;

		@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", name = "workingDir")
		private String workingDir;

		public void setEnvironmentVariables(List<String> vars)
		{
			this.envs = vars;
		}

		public List<String> getEnvironmentVariables() {
			if (envs == null)
				envs = new ArrayList<>();
			return envs;
		}

		public List<String> getArguments()
		{
			if(args == null)
				args = new ArrayList<>();
			return args;
		}

		public String getWorkingDir() {
			return workingDir;
		}

		public void setWorkingDir(String workingDir) {
			this.workingDir = workingDir;
		}
	}


}
