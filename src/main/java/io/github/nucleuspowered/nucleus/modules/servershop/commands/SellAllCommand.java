/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ItemAliasArgument;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresEconomy;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@RunAsync
@NoCost
@NoCooldown
@NoWarmup
@RequiresEconomy
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand({"itemsellall", "sellall"})
public class SellAllCommand extends AbstractCommand<Player> {

    @Inject private ItemDataService itemDataService;
    @Inject private EconHelper econHelper;

    private final String itemKey = "item";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("a", "-accept").buildWith(GenericArguments.none()),
            GenericArguments.optional(
                new ItemAliasArgument(Text.of(itemKey))
            )
        };
    }

    @Override
    public CommandResult executeCommand(final Player src, CommandContext args) throws Exception {
        boolean accepted = args.hasAny("a");
        CatalogType ct = getCatalogTypeFromHandOrArgs(src, itemKey, args);
        String id = ct.getId();

        ItemStack query;
        if (ct instanceof BlockState) {
            query = ItemStack.builder().fromBlockState((BlockState)ct).quantity(1).build();
            query.setQuantity(-1); // Yeah...
        } else {
            // Having a quantity of -1 causes an IllegalArgumentException here...
            query = ItemStack.of((ItemType)ct, 1);

            // and doesn't care here...
            query.setQuantity(-1);
        }

        ItemDataNode node = itemDataService.getDataForItem(id);
        final double sellPrice = node.getServerSellPrice();
        if (sellPrice < 0) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.itemsell.notforselling"));
        }

        Iterable<Slot> slots = Util.getStandardInventory(src).query(query).slots();
        List<ItemStack> itemsToSell = StreamSupport.stream(Util.getStandardInventory(src).query(query).slots().spliterator(), false)
            .map(Inventory::peek).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        // Get the cost.
        final int amt = itemsToSell.stream().mapToInt(ItemStack::getQuantity).sum();
        if (amt <= 0) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.itemsellall.none", query.getTranslation().get()));
        }

        final double overallCost = sellPrice * amt;

        if (accepted) {
            if (econHelper.depositInPlayer(src, overallCost, false)) {
                slots.forEach(Inventory::clear);
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.itemsell.summary", String.valueOf(amt), query.getTranslation().get(), econHelper.getCurrencySymbol(overallCost)));
                return CommandResult.success();
            }

            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.itemsell.error", query.getTranslation().get()));
        }

        src.sendMessage(
            plugin.getMessageProvider()
                .getTextMessageWithFormat("command.itemsellall.summary",
                    String.valueOf(amt), query.getTranslation().get(), econHelper.getCurrencySymbol(overallCost), id)
                .toBuilder().onClick(TextActions.runCommand("/nucleus:itemsellall -a " + id)).build()
        );

        return CommandResult.success();
    }
}
