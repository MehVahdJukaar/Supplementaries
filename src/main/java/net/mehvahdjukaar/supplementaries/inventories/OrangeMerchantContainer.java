package net.mehvahdjukaar.supplementaries.inventories;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.network.PacketBuffer;

public class OrangeMerchantContainer extends MerchantContainer {
    public OrangeMerchantContainer(int id, PlayerInventory playerInventory, IMerchant merchant) {
        super(id, playerInventory, merchant);
    }

    public OrangeMerchantContainer(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        super(id,playerInventory);
    }


}
