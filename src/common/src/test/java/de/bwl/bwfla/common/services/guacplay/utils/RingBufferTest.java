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

package de.bwl.bwfla.common.services.guacplay.utils;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;


public class RingBufferTest extends BaseTest
{
	private static final int MAX_TIMEOUT = 250; // ms
	
	private static final boolean VERBOSE_MODE_ENABLED = false;
	
	
	@Test
	public void test() throws InterruptedException
	{
		log.info("Testing RingBufferSPSC...");
		
		final Random random = new Random();
		final Random rand1 = new Random();
		final Random rand2 = new Random();
		
		final int numEntries = 16 + random.nextInt(64);
		Message[] buffer = new Message[numEntries];
		for (int i = 0; i < numEntries; ++i)
			buffer[i] = new Message();
		
		System.out.println("RingBuffer's capacity is " + numEntries + " messages.");
		
		final RingBufferSPSC<Message> queue = new RingBufferSPSC<Message>(buffer);
		
		final int numMessages = 100 + random.nextInt(400);
		System.out.println("Testing with " + numMessages + " messages.");
		
		Runnable consumer = new Runnable() {
			@Override
			public void run()
			{
				System.out.println("Consumer started!");
				
				Message message = null;
				for (int i = 0; i < numMessages; ++i) {
					while ((message = queue.beginTakeOp()) == null)
						Thread.yield();
					
					final long timestamp = message.getTimestamp();
					if (!RingBufferTest.checkTimestamp(timestamp, i))
						return;
					
					RingBufferTest.sleep(rand1, MAX_TIMEOUT);
					
					message.reset();
					queue.finishTakeOp();
				}
				
				System.out.println("Consumer stopped!");
			}
		};
		
		Runnable producer = new Runnable() {
			@Override
			public void run()
			{
				System.out.println("Producer started!");
				
				Message message = null;
				for (int i = 0; i < numMessages; ++i) {
					while ((message = queue.beginPutOp()) == null)
						Thread.yield();
					
					message.set(SourceType.INTERNAL, i, null, 0, 0);
					queue.finishPutOp();
					
					if (VERBOSE_MODE_ENABLED)
						System.out.println("Message #" + message.getTimestamp() + " produced.");
					
					RingBufferTest.sleep(rand2, MAX_TIMEOUT);
				}
				
				System.out.println("Producer stopped!");
			}
		};
		
		
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		// Randomize the start order of producer and consumer
		if (random.nextBoolean()) {
			executor.submit(consumer);
			executor.submit(producer);
		}
		else {
			executor.submit(producer);
			executor.submit(consumer);
		}
		
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.MINUTES);
		
		this.markAsPassed();
	}
	
	
	public static void sleep(Random random, int maxtime)
	{
		// Artificial timeout
		try {
			Thread.sleep(random.nextInt(maxtime));
		}
		catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}
	
	public static boolean checkTimestamp(long current, long expected)
	{
		if (VERBOSE_MODE_ENABLED)
			System.out.println("Message #" + current + " consumed.");
		
		if (current != expected)
			Assert.fail("Wrong message! Expected: " + expected + ", Current: " + current);
		
		return true;
	}
}
