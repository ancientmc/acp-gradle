package net.minecraft.src;

import java.util.Random;

public class BlockRubyOre extends Block {
    public BlockRubyOre(int blockID, int blockIndexInTexture) {
        super(blockID, blockIndexInTexture, Material.rock);
    }

    /**
     * Setting a custom class for your blocks (and other stuff) allows you to have more customization.
     * In this instance, we can use one of the Block class's methods to set the item ID dropped by our ruby ore block to the ruby item.
     */
    public int idDropped(int metadata, Random random) {
        return mod_Ruby.RUBY.itemID;
    }
}
