/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist;

import io.github.nucleuspowered.nucleus.api.service.NucleusBlacklistMigrationService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.StandardModule;
import io.github.nucleuspowered.nucleus.modules.blacklist.handler.BlacklistMigrationHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = BlacklistModule.ID, name = "Blacklist")
public class BlacklistModule extends StandardModule {

    public final static String ID = "blacklist";

    @Override public void performPreTasks() {
        plugin.getLogger().warn("-----------------------------------");
        plugin.getLogger().warn("NOTICE OF REMOVAL: BLACKLIST MODULE");
        plugin.getLogger().warn("-----------------------------------");
        plugin.getLogger().warn("The item blacklist module has been removed from Nucleus. Please use some other protection plugin, such as "
                + "GriefPrevention that can block these items.");
        plugin.getLogger().warn("A migrator for those of you that use ProtectionPerms has been provided, run `/blacklist migrate pp` to "
                + "attempt to migrate.");
        plugin.getLogger().warn("A migrator for those of you that use GriefPrevention will be provided soon.");
        plugin.getLogger().warn("-----------------------------------");

        BlacklistMigrationHandler bmh = new BlacklistMigrationHandler();
        plugin.getInternalServiceManager().registerService(BlacklistMigrationHandler.class, bmh);
        Sponge.getServiceManager().setProvider(plugin, NucleusBlacklistMigrationService.class, bmh);
    }
}
