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

package de.bwl.bwfla.imageclassifier.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.openslx.eaas.resolver.DataResolver;
import com.openslx.eaas.resolver.DataResolvers;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imageclassifier.datatypes.*;
import de.bwl.bwfla.common.datatypes.identification.DiskType;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imageclassifier.client.IdentificationRequest;


public abstract class BaseTask extends BlockingTask<Object>
{
	protected final IdentificationRequest request;
	protected final UserContext userctx;
	protected final ExecutorService executor;
	protected Configuration cfg = ConfigurationProvider.getConfiguration();
	private final ImageMounter imageMounter;

	private static final FileAttribute<?> BASEDIR_ATTRIBUTES;
	static {
		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);
		BASEDIR_ATTRIBUTES = PosixFilePermissions.asFileAttribute(permissions);
	}
	
	protected BaseTask(IdentificationRequest request, UserContext userctx, ExecutorService executor)
	{
		this.request = request;
		this.userctx = userctx;
		this.executor = executor;
		imageMounter = new ImageMounter(log);

		for (FileCollectionEntry fce : request.getFileCollection().files) {
			var location = fce.getUrl();

			// Optionally, resolve relative URLs...
			if (location == null || DataResolver.isRelativeUrl(location)) {
				location = DataResolvers.objects()
						.resolve(fce, userctx);

				fce.setUrl(location);
			}
		}
	}

	private IdentificationData<?> identifyFile(String url, String fileName)
	{
		Path basePath = null;
		IdentificationOutputIndex<?> index = null;
		DiskType type = null;

		try {
			basePath = BaseTask.newBaseDir();
		} catch (IdentificationTaskException e) {
			e.printStackTrace();
		}
		final File baseDir = basePath.toFile();
		String _fileName = fileName != null ? fileName : "user-file";
		Path uploadPath = basePath.resolve(_fileName);

		Binding b = new Binding();
		b.setUrl(url);

		try {
			EmulatorUtils.copyRemoteUrl(b, uploadPath, log);

			type = runDiskType(uploadPath, log);
			if(type == null)
				throw new BWFLAException("cannot identify disk image");

			log.info("Begin identification...");
			String tool = cfg.get("imageclassifier.identification_tool");
			Classifier classifier = null;
				classifier = new SiegfriedClassifier();

			boolean verbosemode = Boolean.parseBoolean(cfg.get("imageclassifier.verbosemode"));
			classifier.addDirectory(basePath);
			index = classifier.runIdentification(verbosemode);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			log.info("Cleaning up...");

			try {
				FileUtils.deleteDirectory(baseDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.info("Cleanup finished.");
			return new IdentificationData<>(index, type);
		}
	}

	private IdentificationData<?> identifyImage(FileCollectionEntry fce) {
		Path subresFilePath = null;
		Path cowMountpoint = null;
		Path isoMountpoint = null;
		Path hfsMountpoint = null;
		IdentificationOutputIndex<?> index = null;
		DiskType type = null;

		final String baseName = "data";
		Path basePath = null;
		try {
			basePath = BaseTask.newBaseDir();
		} catch (IdentificationTaskException e) {
			e.printStackTrace();
		}
		final File baseDir = basePath.toFile();

		try {
			final boolean isAlwaysDownload = ConfigurationProvider.getConfiguration().get("imageclassifier.always_download", Boolean.class);

			if (BaseTask.checkRangeRequestSupport(fce.getUrl(), log) && !isAlwaysDownload) {
				log.info("Object host supports range-requests, creating COW file...");
				final Path cowFilePath = basePath.resolve(baseName + ".cow");
				cowMountpoint = basePath.resolve(cowFilePath.getFileName() + ".fuse");
				QcowOptions options = new QcowOptions();
				options.setBackingFile(fce.getUrl());
				EmulatorUtils.createCowFile(cowFilePath, options, log);
				ImageMounter.Mount mount = imageMounter.mount(cowFilePath, cowMountpoint);
				subresFilePath = mount.getMountPoint();
			} else {
				subresFilePath = basePath.resolve("object.img");
				EmulatorUtils.copyRemoteUrl(fce, subresFilePath, log);
				log.info("Downloading object finished.");
			}

			type = runDiskType(subresFilePath, log);
			if(type == null)
				throw new BWFLAException("cannot identify disk image");
			type.setLocalAlias(fce.getLocalAlias());

			if (type.hasContentType("Q55336682"))
				isoMountpoint = mountAsIso(subresFilePath, log);
			if (type.hasContentType("Q375944"))
				hfsMountpoint = mountAsHfs(subresFilePath, log);

			log.info("Begin identification...");
			String tool = cfg.get("imageclassifier.identification_tool");
			Classifier classifier = null;
			classifier = new SiegfriedClassifier();

			boolean verbosemode = Boolean.parseBoolean(cfg.get("imageclassifier.verbosemode"));
			if (isoMountpoint != null) {
				classifier.addDirectory(isoMountpoint);
			}

			if (hfsMountpoint != null) {
				classifier.addDirectory(hfsMountpoint);
			}
			index = classifier.runIdentification(verbosemode);

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			log.info("Cleaning up...");

			imageMounter.unmount();

			try {
				FileUtils.deleteDirectory(baseDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.info("Cleanup finished.");
			return new IdentificationData<>(index, type);
		}
	}


	protected IdentificationResultContainer identify() throws Exception
	{
		final Map<String, String> policy = new HashMap<String, String>();

		if(request.getFileCollection() != null) {
			final String policyUrl = request.getPolicyUrl();
			if (policyUrl != null && !policyUrl.isEmpty()) {
				log.info("Reading policy file...");
				this.readPolicyFile(policyUrl, policy);
			}

			FileCollection fc = request.getFileCollection();
			if (fc == null)
				throw new BWFLAException("invalid object");

			IdentificationResult classificationResult = new IdentificationResult(fc, policy);

			for (FileCollectionEntry fce : fc.files) {
				IdentificationData<?> data = identifyImage(fce);
				classificationResult.addResult(fce.getId(), data);
			}
			return new IdentificationResultContainer<>(classificationResult);
		}
		else if (request.getFileUrl() != null && request.getFileName() != null)
		{
			IdentificationData<?> data = identifyFile(request.getFileUrl(), request.getFileName());
			FileIdentificationResult classificationResult = new FileIdentificationResult(request.getFileUrl(), request.getFileName(), data);

			return new IdentificationResultContainer<>(classificationResult);
		}
		else
			throw new BWFLAException("invalid identification request");
	}

	/* =============== Internal Helpers =============== */
	
	private static Path newBaseDir() throws IdentificationTaskException
	{
		try {
			return Files.createTempDirectory("", BASEDIR_ATTRIBUTES);
		}
		catch (IOException exception) {
			throw new IdentificationTaskException("Creating new temp dir failed!", exception);
		}
	}
	
	private static void enableInsecureSSL() throws NoSuchAlgorithmException, KeyManagementException
	{
		// Try to get 2. byte of the specified object from its server
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
		    public X509Certificate[] getAcceptedIssuers(){return null;}
		    public void checkClientTrusted(X509Certificate[] certs, String authType){}
		    public void checkServerTrusted(X509Certificate[] certs, String authType){}
		}};

		HostnameVerifier hv = new HostnameVerifier() {
		      public boolean verify(String hostname, SSLSession session) { return true; }
		    };
		
		SSLContext sc = SSLContext.getInstance("TLS");
	    sc.init(null, trustAllCerts, new SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	private static boolean checkRangeRequestSupport(String objectUrl, Logger log)
	{
		try {
			
			URL url = new URL(objectUrl);
			URLConnection connection; 
			
			if(url.getProtocol().equalsIgnoreCase("https"))
			{
				enableInsecureSSL();
			}
			connection = url.openConnection();
			if(url.getProtocol().equalsIgnoreCase("https"))
			{
				((HttpsURLConnection)connection).setRequestMethod("HEAD");
				((HttpsURLConnection)connection).connect();
			}
			else
			{
				((HttpURLConnection)connection).setRequestMethod("HEAD");
				((HttpURLConnection)connection).connect();
			}
			
			// Check server's response
		    String ranges = connection.getHeaderField("Accept-Ranges");
		    if (ranges != null && ranges.equals("bytes"))
		    	return true;
		}
		catch (IOException | NoSuchAlgorithmException | KeyManagementException exception) 
		{
			log.warning("Checking support for accepted range-requests for '" + objectUrl + "' failed!");
			log.log(Level.WARNING, exception.getMessage(), exception);
		}
		
		return false;
	}



	private DiskType runDiskType(Path isopath, Logger log)
	{
		log.info("running disktype");
		try {
			DeprecatedProcessRunner process = new DeprecatedProcessRunner();
			process.setLogger(log);
			process.setCommand("disktype");
			process.addArgument(isopath.toString());

			final DeprecatedProcessRunner.Result result = process.executeWithResult(false)
					.orElse(null);

			if (result == null || !result.successful())
				throw new BWFLAException("Running disktype failed!");

			final String res = result.stdout();
			DiskType type = DiskType.fromJsonValue(res, DiskType.class);
			log.warning(res);
			return type;
		}
		catch(Exception exception) {
			log.log(Level.WARNING, exception.getMessage(), exception);
			return null;
		}
	}

	private Path mountAsIso(Path iso, Logger log) throws BWFLAException {
		Path dest = ImageMounter.createWorkingDirectory();
		try {
			imageMounter.mount(iso, dest, FileSystemType.ISO9660);
			log.info("ISO file mounted to: " + dest.toString());
		}
		catch (Exception exception) {
			log.warning("Mounting '" + iso.toString() + "' as ISO failed!");
			log.log(Level.WARNING, exception.getMessage(), exception);
		}
		return dest;
	}
	
	private Path mountAsHfs(Path iso, Logger log) throws BWFLAException {
		Path dest = ImageMounter.createWorkingDirectory();;
		try {
			imageMounter.mount(iso, dest, FileSystemType.HFS);
			log.info("HFS file mounted to: " + dest.toString());
		}
		catch (Exception exception) {
			log.warning("Mounting '" + iso.toString() + "' as HFS failed!");
			log.log(Level.WARNING, exception.getMessage(), exception);
		}
		
		return dest;
	}

	private boolean readPolicyFile(String url, Map<String, String> policy)
	{
		final char delimiter = ' ';
		final char space = ' ';
		
		try (InputStream input = new URL(url).openConnection().getInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			
			// Policy file is expected to contain:
			//     <TYPE's ID> <VALUES>
			//     ...
			//     <TYPE's ID> <VALUES>
			
			String type, value;
			String line = null;
			int linenum = 1;
			
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty())
					continue;
				
				final int length = line.length();
				int offset = 0;
				
				// Find type's start index, skipping all spaces
				while ((offset < length) && (line.charAt(offset) == space))
					++offset;
				
				// Find type's end index, skipping all spaces
				int delpos = line.indexOf(delimiter, offset);
				if (delpos <= offset) {
					log.warning("Parsing line " + linenum + " in '" + url  + "' failed! Skipping it.");
					continue;
				}
				
				type = line.substring(offset, delpos);
				offset = delpos;
				
				// Find value's start index, skipping all spaces
				while ((offset < length) && (line.charAt(offset) == space))
					++offset;
				
				// No value?
				if (offset == length) {
					log.warning("Parsing policy value from line " + linenum + " in '" + url  + "' failed! Skipping it.");
					continue;
				}
				
				value = line.substring(offset);
				policy.put(type, value);
				++linenum;
			}
			
			return true;
		}
		catch (Exception exception) {
			log.warning("Parsing policy from '" + url + "' failed!");
			log.log(Level.WARNING, exception.getMessage(), exception);
			return false;
		}
	}
	
	
	private static class IdentificationTaskException extends Exception
	{
		private static final long serialVersionUID = -7167395621475291292L;

		public IdentificationTaskException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
