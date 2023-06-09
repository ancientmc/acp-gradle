package net.minecraft.src;

import java.util.List;

/**
 * ModLoader mods extend the main class from the BaseMod class, and inheriting all of its methods.
 * All ModLoader main classes must start with "mod_". This is how ModLoader parses through the modded class files in the JAR.
 */
public class mod_Ruby extends BaseMod {
    /**
     * Item registration is as follows. Be mindful of the ID's. It's a lot more picky here than in modern MC.
     * Check the Block and Item IDs to see what hasn't been taken yet.
     * --
     * We can use ModLoader's getUniqueSpriteIndex to give items and blocks a unique index value in Minecraft's sprite maps. For
     * items, type in "/gui/items.png" as the argument. For blocks, type in "/terrain.png". Include the forward slash at the beginning.
     */
    public static final Item RUBY = new Item(201).setIconIndex(ModLoader.getUniqueSpriteIndex("/gui/items.png"));

    /**
     * Block registration is similar to items, but seems a little more finnicky. From what I can gather, you can't have your IDs above 127,
     * which gives you little room for adding a lot of blocks. If it's 128 or above Java fucks up number stuff when trying to place a block, and it crashes.
     * You also need to inherit the base Block class, or make your own class that inherits Block (probably the same for items).
     * Initially I did not do that, and it messed up the crafting recipes for whatever reason.
     */
    public static final Block RUBY_ORE = new BlockRubyOre(101, ModLoader.getUniqueSpriteIndex("/terrain.png")).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep);
    public static final Block RUBY_BLOCK = new Block(102, ModLoader.getUniqueSpriteIndex("/terrain.png"), Material.rock).setHardness(4.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep);

    /**
     * Recipe registration. If you're familiar with making mod recipes in modern MC, it's a very similar idea here. T
     * The first arg of "addRecipe" is an item stack of the result item, while the second arg is a varargs array thing
     * containing the shape of the recipe as well as defining what ingredients we're using.
     * Looking at the ruby block, for example, the first two entries in the array are four items with the character 'R' in groups of two.
     * Each entry group represents a line on the crafting table. We then see a key-value pair, where 'R' is assigned to
     * our ruby item. In a crafting grid, this would equate to four rubies making one ruby block.
     */
    public void AddRecipes(CraftingManager recipes) {
        recipes.addRecipe(new ItemStack(RUBY), new Object[]{ "AA", "AA", 'A', Block.dirt });
        recipes.addRecipe(new ItemStack(RUBY_BLOCK), new Object[]{  "RR", "RR", 'R', RUBY });

        // In the ItemStack arg we can define how much of the result item we want to return. In this case, 1 ruby block makes 9 rubies.
        recipes.addRecipe(new ItemStack(RUBY, 9), new Object[] { "B", 'B', RUBY_BLOCK });
        recipes.addRecipe(new ItemStack(RUBY_ORE), new Object[] { "TT", "TT", 'T', Block.cobblestone });
    }

    /**
     * This method registers the blocks in our mod. The for-loop setup is just something I did to make it look nicer. You could
     * just call the add method every time you want to register a block, like in the comments below. Either way should work fine.
     */
    public void RegisterBlocks(List registry) {
        Block[] blocks = new Block[]{ RUBY_ORE, RUBY_BLOCK };
        for (int i = 0; i < blocks.length; i++) {
            registry.add(blocks[i]);
        }

        /*
         * ALTERNATIVE:
         * registry.add(RUBY_BLOCK);
         * registry.add(RUBY_ORE);
         */
    }

    /**
     * Register our texture overrides, using the same sprite map paths like in the getUniqueSpriteIndex() method during
     * item/block registration. The sprite maps are located in src/main/resources, and you should put your mod textures
     * in that folder. Note that your images must be 16x16, otherwise the game will crash.
     */
    public void RegisterTextureOverrides() {
        ModLoader.addOverride("/terrain.png", "/ruby/block/ruby_block.png", RUBY_BLOCK.blockIndexInTexture);
        ModLoader.addOverride("/terrain.png", "/ruby/block/ruby_ore.png", RUBY_ORE.blockIndexInTexture);
        ModLoader.addOverride("/gui/items.png", "/ruby/item/ruby.png", RUBY.iconIndex);
    }
}
