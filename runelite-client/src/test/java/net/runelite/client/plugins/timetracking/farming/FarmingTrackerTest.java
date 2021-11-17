/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.timetracking.farming;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.config.RuneScapeProfile;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.timetracking.TimeTrackingConfig;
import net.runelite.client.plugins.timetracking.TimeTrackingPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FarmingTrackerTest
{
	@Inject
	private FarmingTracker farmingTracker;

	@Inject
	private TimeTrackingPlugin timeTrackingPlugin;

	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private RuneLiteConfig runeLiteConfig;

	@Mock
	@Bind
	private ScheduledExecutorService scheduledExecutorService;

	@Mock
	@Bind
	private ItemManager itemManager;

	@Mock
	@Bind
	private ConfigManager configManager;

	@Mock
	@Bind
	private TimeTrackingConfig config;

	@Mock
	@Bind
	private FarmingWorld farmingWorld;

	@Mock
	@Bind
	private Notifier notifier;

	@Mock
	@Bind
	private ChatMessageManager chatMessageManager;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));

		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Adam");
		when(client.getLocalPlayer()).thenReturn(player);
	}

	@Test(expected = IllegalStateException.class)
	public void testEmptyNotification()
	{
		RuneScapeProfile runeScapeProfile = new RuneScapeProfile("Adam", RuneScapeProfileType.STANDARD, null, null);

		PatchPrediction patchPrediction = new PatchPrediction(Produce.EMPTY_COMPOST_BIN, CropState.EMPTY, 0L, 0, 0);
		FarmingRegion region = new FarmingRegion("Ardougne", 10548, false,
			new FarmingPatch("North", Varbits.FARMING_4771, PatchImplementation.ALLOTMENT),
			new FarmingPatch("South", Varbits.FARMING_4772, PatchImplementation.ALLOTMENT),
			new FarmingPatch("", Varbits.FARMING_4773, PatchImplementation.FLOWER),
			new FarmingPatch("", Varbits.FARMING_4774, PatchImplementation.HERB),
			new FarmingPatch("", Varbits.FARMING_4775, PatchImplementation.COMPOST)
		);
		FarmingPatch patch = region.getPatches()[4];
		patch.setRegion(region);
		farmingTracker.sendNotification(runeScapeProfile, patchPrediction, patch);
	}

	@Test
	public void testHarvestableNotification()
	{
		RuneScapeProfile runeScapeProfile = new RuneScapeProfile("Adam", RuneScapeProfileType.STANDARD, null, null);

		PatchPrediction patchPrediction = new PatchPrediction(Produce.RANARR, CropState.HARVESTABLE, 0L, 0, 0);
		FarmingRegion region = new FarmingRegion("Ardougne", 10548, false,
			new FarmingPatch("North", Varbits.FARMING_4771, PatchImplementation.ALLOTMENT),
			new FarmingPatch("South", Varbits.FARMING_4772, PatchImplementation.ALLOTMENT),
			new FarmingPatch("", Varbits.FARMING_4773, PatchImplementation.FLOWER),
			new FarmingPatch("", Varbits.FARMING_4774, PatchImplementation.HERB),
			new FarmingPatch("", Varbits.FARMING_4775, PatchImplementation.COMPOST)
		);
		FarmingPatch patch = region.getPatches()[3];
		patch.setRegion(region);
		farmingTracker.sendNotification(runeScapeProfile, patchPrediction, patch);

		verify(notifier).notify("Your Ranarr is ready to harvest in Ardougne.");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadMessage()
	{
		WorldPoint loc = new WorldPoint(1247, 3726, 0);
		String message = "Did you ever hear the Tragedy of Darth Plagueis the Wise?";

		FarmingRegion region = new FarmingRegion("Ardougne", 10548, false,
				new FarmingPatch("North", Varbits.FARMING_4771, PatchImplementation.ALLOTMENT),
				new FarmingPatch("South", Varbits.FARMING_4772, PatchImplementation.ALLOTMENT),
				new FarmingPatch("", Varbits.FARMING_4773, PatchImplementation.FLOWER),
				new FarmingPatch("", Varbits.FARMING_4774, PatchImplementation.HERB),
				new FarmingPatch("", Varbits.FARMING_4775, PatchImplementation.COMPOST)
		);

		List<FarmingRegion> list = new ArrayList<>();
		list.add(region);

		when(farmingWorld.getRegionsForLocation(loc)).thenReturn(list);

		farmingTracker.setFarmingExamineText(loc, message);
	}

	@Test
	public void testFarmingExamineText()
	{
		WorldPoint loc = new WorldPoint(1, 1, 0);
		String message = "a herb is growing in this patch.";

		FarmingRegion region = new FarmingRegion("Ardougne", 10548, false,
				new FarmingPatch("North", Varbits.FARMING_4771, PatchImplementation.ALLOTMENT),
				new FarmingPatch("South", Varbits.FARMING_4772, PatchImplementation.ALLOTMENT),
				new FarmingPatch("", Varbits.FARMING_4773, PatchImplementation.FLOWER),
				new FarmingPatch("", Varbits.FARMING_4774, PatchImplementation.HERB),
				new FarmingPatch("", Varbits.FARMING_4775, PatchImplementation.COMPOST)
		);

		List<FarmingRegion> list = new ArrayList<>();
		list.add(region);
		FarmingPatch patch = region.getPatches()[3];
		patch.setRegion(region);

		long time =  Instant.now().getEpochSecond() + 10000;
		PatchPrediction prediction = new PatchPrediction(
				Produce.RANARR,
				CropState.GROWING,
				time,
				1,
				3
		);

		when(farmingWorld.getRegionsForLocation(loc)).thenReturn(list);
		doReturn(prediction).when(farmingTracker).predictPatch(patch, null);
//		TODO: get proper return type
//		when(configManager.getConfiguration(TimeTrackingConfig.CONFIG_GROUP, null, "10548.4774")).thenReturn("32:10548");
		when(farmingTracker.predictPatch(patch, null)).thenReturn(prediction);

		ArgumentCaptor<QueuedMessage> argumentCaptor = ArgumentCaptor.forClass(QueuedMessage.class);
		farmingTracker.setFarmingExamineText(loc, message);
		verify(chatMessageManager).queue(argumentCaptor.capture());
	}

	@Test(expected = NullPointerException.class)
	public void testFarmingMessageWhenConfigIsFalse()
	{
		String gameMessage = "A herb is growing in this patch.";
		ChatMessage chatMessage = new ChatMessage(null, ChatMessageType.OBJECT_EXAMINE, "", gameMessage, "", 0);
		when(configManager.getConfig(TimeTrackingConfig.class).farmingExamineTime()).thenReturn(false);
		timeTrackingPlugin.onChatMessage(chatMessage);
	}
}