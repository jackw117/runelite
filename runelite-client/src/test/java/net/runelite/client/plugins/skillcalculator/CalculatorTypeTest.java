/*
 * Copyright (c) 2021, Jordan Atwood <nightfirecat@protonmail.com>
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
package net.runelite.client.plugins.skillcalculator;

import com.google.inject.testing.fieldbinder.Bind;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.skillcalculator.skills.SkillAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class CalculatorTypeTest
{
	@Inject
	SkillCalculatorPlugin skillCalculatorPlugin;

	@Mock
	@Bind
	Client client;

	@Mock
	@Bind
	ClientThread clientThread;

	@Mock
	@Bind
	SpriteManager spriteManager;

	@Mock
	@Bind
	ItemManager itemManager;

	@Test
	public void skillActionsInLevelOrder()
	{
		for (final CalculatorType calculatorType : CalculatorType.values())
		{
			int level = 1;

			for (final SkillAction skillAction : calculatorType.getSkillActions())
			{
				if (skillAction.getLevel() < level)
				{
					fail("Skill action " + skillAction + " is out of order for " + calculatorType.getSkill().getName());
				}

				level = skillAction.getLevel();
			}
		}
	}

	@Test
	public void testGetSetTargetAmount()
	{
		final UICalculatorInputArea uiInput = new UICalculatorInputArea();

		uiInput.setTargetAmountInput("a");
		int targetAmount_1 = uiInput.getTargetAmountInput();

		uiInput.setTargetAmountInput("20000000000000000000000000000");
		int targetAmount_2 = uiInput.getTargetAmountInput();

		uiInput.setTargetAmountInput(126);
		int targetAmount_3 = uiInput.getTargetAmountInput();

		uiInput.setTargetAmountInput("25a6");
		int targetAmount_4 = uiInput.getTargetAmountInput();

		assertEquals(0, targetAmount_1);
		assertEquals(0, targetAmount_2);
		assertEquals(126, targetAmount_3);
		assertEquals(256, targetAmount_4);
	}
}
