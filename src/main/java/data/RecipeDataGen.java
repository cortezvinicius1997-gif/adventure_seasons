package data;

import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGen extends FabricRecipeProvider {
    public RecipeDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, AdventureSeasonBlocks.SEASONSENSOR)
                .pattern("   ")
                .pattern("#I#")
                .pattern("###")
                .input('#', Blocks.DEEPSLATE_BRICK_SLAB)
                .input('I', Items.ITEM_FRAME)
                .criterion(FabricRecipeProvider.hasItem(Blocks.DEEPSLATE_BRICK_SLAB), FabricRecipeProvider.conditionsFromItem(Blocks.DEEPSLATE_BRICK_SLAB))
                .criterion(FabricRecipeProvider.hasItem(Items.ITEM_FRAME), FabricRecipeProvider.conditionsFromItem(Items.ITEM_FRAME))
                .offerTo(exporter, Identifier.of(FabricRecipeProvider.getRecipeName(AdventureSeasonBlocks.SEASONSENSOR)));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, AdventureSeasonBlocks.SEASONCALENDAR, 3)
                .pattern("PP ")
                .pattern("II ")
                .pattern("## ")
                .input('#', Blocks.OAK_SLAB)
                .input('I', Items.ITEM_FRAME)
                .input('P', Items.PAPER)
                .criterion(FabricRecipeProvider.hasItem(Blocks.OAK_SLAB), FabricRecipeProvider.conditionsFromItem(Blocks.OAK_SLAB))
                .criterion(FabricRecipeProvider.hasItem(Items.ITEM_FRAME), FabricRecipeProvider.conditionsFromItem(Items.ITEM_FRAME))
                .criterion(FabricRecipeProvider.hasItem(Items.PAPER), FabricRecipeProvider.conditionsFromItem(Items.PAPER))
                .offerTo(exporter, Identifier.of(FabricRecipeProvider.getRecipeName(AdventureSeasonBlocks.SEASONCALENDAR)));
    }
}
