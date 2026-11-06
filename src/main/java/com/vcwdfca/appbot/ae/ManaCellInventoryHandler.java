package com.vcwdfca.appbot.ae;

import appeng.api.storage.IMEInventory;
import appeng.me.storage.CellInventoryHandler;

public class ManaCellInventoryHandler extends CellInventoryHandler<AEManaStack> {

    public ManaCellInventoryHandler(IMEInventory<AEManaStack> inventory) {
        super(inventory, AEManaStackType.MANA_STACK_TYPE);
    }
}
