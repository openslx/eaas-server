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

package de.bwl.bwfla.imagebuilder.client;

import java.net.URL;
import java.time.Duration;

import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderResult;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.AbstractServiceClient;
import de.bwl.bwfla.imagebuilder.api.ImageBuildHandle;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;


@ApplicationScoped
public class ImageBuilderClient extends AbstractServiceClient<ImageBuilderService>
{
	private static final String WSDL_URL_TEMPLATE = "%s/imagebuilder/ImageBuilder?wsdl";

	public static ImageBuilderClient get()
	{
		return CDI.current().select(ImageBuilderClient.class).get();
	}

	@Override
	protected ImageBuilderService createService(URL url)
	{
		return new ImageBuilderService(url);
	}

	@Override
	protected String getWsdlUrl(String host)
	{
		return String.format(WSDL_URL_TEMPLATE, host);
	}

	public ImageBuilder getImageBuilderPort(String host) throws BWFLAException
	{
		return this.getPort(host, ImageBuilder.class);
	}


	/**
	 * Wait for the result of the specified image-build.
	 * @param builder The ImageBuilder to talk to.
	 * @param handle The handle for the image-build.
	 * @param timeout The max. time to wait for the result.
	 * @return The BlobHandle for the built image.
	 */
	public static ImageBuilderResult awaitImageBuildResult(ImageBuilder builder, ImageBuildHandle handle, Duration timeout)
			throws BWFLAException
	{
		return ImageBuilderClient.awaitImageBuildResult(builder, handle, timeout, Duration.ofSeconds(3));
	}

	/**
	 * Wait for the result of the specified image-build.
	 * @param builder The ImageBuilder to talk to.
	 * @param handle The handle for the image-build.
	 * @param timeout The max. time to wait for the result.
	 * @param timeout The delay to wait between retries.
	 * @return The BlobHandle for the built image.
	 */
	public static ImageBuilderResult awaitImageBuildResult(ImageBuilder builder, ImageBuildHandle handle, Duration timeout, Duration delay)
			throws BWFLAException
	{
		final long endms = System.currentTimeMillis() + timeout.toMillis();
		final long delayms = delay.toMillis();

		while (!builder.isDone(handle)) {
			final long remaining = endms - System.currentTimeMillis();
			final long curdelay = Math.min(delayms, remaining);
			if (curdelay < 0)
				throw new BWFLAException("Waiting for image-build result timed out!");

			try {
				Thread.sleep(curdelay);
			}
			catch (Exception error) {
				// Ignore it!
			}
		}

		return builder.get(handle);
	}

	/**
	 * Build an image synchronously.
	 * @param builder The ImageBuilder to talk to.
	 * @param description The describtion for the image to build.
	 * @param timeout The max. time to wait for the result.
	 * @return The BlobHandle for the built image.
	 */
	public static ImageBuilderResult build(ImageBuilder builder, ImageDescription description, Duration timeout)
			throws BWFLAException
	{
		return ImageBuilderClient.build(builder, description, timeout, Duration.ofSeconds(3));
	}

	/**
	 * Wait for the result of the specified image-build.
	 * @param builder The ImageBuilder to talk to.
	 * @param description The describtion for the image to build.
	 * @param timeout The max. time to wait for the result.
	 * @param timeout The delay to wait between retries.
	 * @return The BlobHandle for the built image.
	 */
	public static ImageBuilderResult build(ImageBuilder builder, ImageDescription description, Duration timeout, Duration delay)
			throws BWFLAException
	{
		final ImageBuildHandle build = builder.build(description);
		return ImageBuilderClient.awaitImageBuildResult(builder, build, timeout, delay);
	}
}
