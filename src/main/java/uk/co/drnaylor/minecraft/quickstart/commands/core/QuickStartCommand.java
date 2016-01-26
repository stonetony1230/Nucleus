package uk.co.drnaylor.minecraft.quickstart.commands.core;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.util.Arrays;

/**
 * Gives information about QuickStart.
 *
 * Command Usage: /quickstart
 * Permission: quickstart.quickstart.base
 */
@RunAsync
@Permissions
@NoWarmup
@NoCooldown
@NoCost
public class QuickStartCommand extends CommandBase {

    private final Text version = Text.of(QuickStart.MESSAGE_PREFIX, TextColors.GREEN, QuickStart.NAME + " version " + QuickStart.VERSION);
    private Text modules = null;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "quickstart" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        if (modules == null) {
            QuickStartModuleService qs = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

            Text.Builder tb = Text.builder("Modules: ").color(TextColors.GREEN);

            boolean addComma = false;
            for (PluginModule x : PluginModule.values()) {
                if (addComma) {
                    tb.append(Text.of(TextColors.GREEN, ", "));
                }

                tb.append(Text.of(qs.getModulesToLoad().contains(x) ? TextColors.GREEN : TextColors.RED, x.getKey()));
                addComma = true;
            }

            modules = tb.append(Text.of(TextColors.GREEN, ".")).build();
        }

        src.sendMessage(version);
        src.sendMessage(modules);
        return CommandResult.success();
    }
}
