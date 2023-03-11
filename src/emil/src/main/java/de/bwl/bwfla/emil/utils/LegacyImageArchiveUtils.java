package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.resolver.DataResolvers;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;


public class LegacyImageArchiveUtils
{
	public enum ImageKind
	{
		base,
		containers,
		derivate,
		object,
		user,
	}

	public static void list(Path basedir, ImageKind kind, Map<String, Path> paths) throws IOException
	{
		final Consumer<Path> consumer = (path) -> {
			if (Files.isDirectory(path))
				return;

			final var filename = path.getFileName()
					.toString();

			if (filename.startsWith(".fuse"))
				return;

			final var prev = paths.put(filename, path);
			if (prev != null) {
				final var message = "Duplicate found for ID '" + filename + "' at:\n"
						+ "- " + prev + "\n"
						+ "- " + path;

				throw new IllegalStateException(message);
			}
		};

		basedir = basedir.resolve(kind.toString());
		try (final var files = Files.list(basedir)) {
			files.forEach(consumer);
		}
	}

	public static ImageInformation.QemuImageFormat findBackingFileFormat(String bfid, String bfurl,
			boolean isEmulatorImage, Map<String, Path> images, Logger log) throws Exception
	{
		final var urls = new ArrayList<String>(3);
		if (images.containsKey(bfid)) {
			// found backing file locally!
			final var path = images.get(bfid);
			urls.add(path.toString());
		}
		else {
			// look up backing file in the new image-archive!
			final String url;
			if (isEmulatorImage) {
				url = DataResolvers.emulators()
						.resolve(bfid);
			}
			else {
				final var binding = new ImageArchiveBinding();
				binding.setImageId(bfid);
				url = DataResolvers.images()
						.resolve(binding, null);
			}

			urls.add(url);
		}

		// use original url as fallback!
		urls.add(bfurl);

		// try all urls in sequence...
		Exception error = null;
		for (final var url : urls) {
			try {
				final var bfinfo = new ImageInformation(url, log);
				return bfinfo.getFileFormat();
			}
			catch (Exception exception) {
				error = exception;
			}
		}

		throw new IllegalStateException("Backing file '" + bfid + "' not found!", error);
	}

	public static void fixBackingFileRef(String imgid, Map<String, Path> images,
			Map<String, String> bfmap, Logger log) throws Exception
	{
		final var image = images.get(imgid);
		final var info = new ImageInformation(image.toString(), log);
		if (!info.hasBackingFile())
			return;

		final var bfurl = info.getBackingFile();
		final var bfid = ImageInformation.getBackingImageId(bfurl);
		var format = info.getBackingFileFormat();
		if (bfmap.put(imgid, bfid) != null)
			throw new IllegalStateException();

		if (bfid.equals(bfurl) && format != null)
			return;

		if (format == null) {
			final var isEmulatorImage = image.toString()
					.contains("emulators");

			format = LegacyImageArchiveUtils.findBackingFileFormat(bfid, bfurl, isEmulatorImage, images, log);
		}

		EmulatorUtils.changeBackingFile(image, bfid, format, log);
	}

	public static String summarize(Map<String, String> bfmap, Collection<String> failedImageIds)
	{
		// bfrevmap: backing-file-id -> directly dependent image-ids
		final var bfrevmap = new HashMap<String, Set<String>>();
		bfmap.forEach((imgid, bfid) -> {
			bfrevmap.computeIfAbsent(bfid, (unused) -> new HashSet<>())
					.add(imgid);
		});

		// chains: image-id -> backing-file chain
		final var bfchains = new LinkedHashMap<String, String>();
		LegacyImageArchiveUtils.computeBackingFileChains(failedImageIds, bfrevmap, bfchains);

		final var summary = new StringBuilder(4096);
		summary.append("Images found to be incomplete:\n");
		failedImageIds.forEach((imgid) -> {
			summary.append("- ").append(imgid);
			final var bfid = bfmap.get(imgid);
			if (bfid != null)
				summary.append(" (via: (!) ").append(bfid).append(")");

			summary.append("\n");
		});

		bfchains.forEach((bfid, chain) -> {
			summary.append("- ").append(bfid);
			summary.append(" (via: ").append(chain).append(")");
			summary.append("\n");
		});

		return summary.toString();
	}

	public static void computeBackingFileChains(Collection<String> bfids, Map<String, Set<String>> bfrevmap, Map<String, String> bfchains)
	{
		for (final var bfid : bfids) {
			final var images = bfrevmap.get(bfid);
			if (images == null)
				return;

			var chain = bfchains.get(bfid);
			if (chain == null)
				chain = "(!) " + bfid;
			else chain = bfid + " -> " + chain;

			for (final var imgid : images)
				bfchains.put(imgid, chain);

			LegacyImageArchiveUtils.computeBackingFileChains(images, bfrevmap, bfchains);
		}
	}
}
