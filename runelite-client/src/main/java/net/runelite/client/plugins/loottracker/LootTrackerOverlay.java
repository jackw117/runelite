package net.runelite.client.plugins.loottracker;

import com.google.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.itemidentification.ItemIdentification;
import net.runelite.client.plugins.itemidentification.ItemIdentificationConfig;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;

import static net.runelite.api.widgets.WidgetID.GUIDE_PRICE_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.KEPT_ON_DEATH_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.KINGDOM_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.LOOTING_BAG_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.SEED_BOX_GROUP_ID;

public class LootTrackerOverlay extends WidgetItemOverlay
{
	private final ItemManager itemManager;
	private final ItemIdentificationConfig itemConfig;

	@Inject
	LootTrackerOverlay(ConfigManager configManager, ItemManager itemManager)
	{
		this.itemManager = itemManager;
		this.itemConfig = configManager.getConfig(ItemIdentificationConfig.class);
		showOnInventory();
		showOnBank();
		showOnInterfaces(KEPT_ON_DEATH_GROUP_ID, GUIDE_PRICE_GROUP_ID, LOOTING_BAG_GROUP_ID, SEED_BOX_GROUP_ID, KINGDOM_GROUP_ID);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		ItemIdentification iden = findItemIdentification(itemId);
		if (iden == null || !iden.getType().getEnabled().test(itemConfig))
		{
			return;
		}

		graphics.setFont(FontManager.getRunescapeSmallFont());
		renderText(graphics, widgetItem.getCanvasBounds(), iden);
	}

	private void renderText(Graphics2D graphics, Rectangle bounds, ItemIdentification iden)
	{
		final TextComponent textComponent = new TextComponent();
		textComponent.setPosition(new Point(bounds.x - 1, bounds.y + bounds.height - 1));
		textComponent.setColor(itemConfig.textColor());
		switch (itemConfig.identificationType())
		{
			case SHORT:
				textComponent.setText(iden.getShortName());
				break;
			case MEDIUM:
				textComponent.setText(iden.getMedName());
				break;
		}
		textComponent.render(graphics);
	}

	private ItemIdentification findItemIdentification(final int itemID)
	{
		final int realItemId = itemManager.canonicalize(itemID);
		return ItemIdentification.get(realItemId);
	}
}
