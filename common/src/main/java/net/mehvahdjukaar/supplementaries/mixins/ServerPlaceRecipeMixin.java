package net.mehvahdjukaar.supplementaries.mixins;

import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.supplementaries.common.items.crafting.RecipeBookHack;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO: finish
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin<C extends Container> {


    @Shadow protected Inventory inventory;

    @Shadow protected abstract boolean testClearGrid();

    @Shadow @Final protected StackedContents stackedContents;

    @Shadow protected RecipeBookMenu<C> menu;

    @Shadow protected abstract void handleRecipeClicked(Recipe<C> recipe, boolean placeAll);

    @Shadow protected abstract void clearGrid(boolean bl);

    @Inject(method = "recipeClicked", at = @At("HEAD"))
    public void handleSpecialRecipeDisplays(ServerPlayer player, Recipe<C> recipe, boolean placeAll, CallbackInfo ci) {
        if (RecipeBookHack.getSpecialRecipe(recipe.getId()) != null) {
            this.inventory = player.getInventory();
            if (this.testClearGrid() || player.isCreative()) {
                this.stackedContents.clear();
                player.getInventory().fillStackedContents(this.stackedContents);
                this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                if (this.stackedContents.canCraft(recipe, null)) {
                    this.handleRecipeClicked(recipe, placeAll);
                } else {
                    this.clearGrid(true);
                    player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
                }
                player.getInventory().setChanged();
            }
        }
    }
}
