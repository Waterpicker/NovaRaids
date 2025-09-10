package me.unariginal.novaraids.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.unariginal.novaraids.NovaRaids;
import me.unariginal.novaraids.data.Location;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class LocationSuggestions implements com.mojang.brigadier.suggestion.SuggestionProvider<net.minecraft.server.command.ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (NovaRaids.LOADED) {
            for (Location boss : NovaRaids.INSTANCE.locationsConfig().locations) {
                builder.suggest(boss.id());
            }
        }

        return builder.buildFuture();
    }
}
