package com.asdflj.ae2thing.inventory.item;

import com.asdflj.ae2thing.util.DimensionalCoordSide;

public interface IClickableInTerminal {

    void setClickedInterface(DimensionalCoordSide tile);

    DimensionalCoordSide getClickedInterface();
}
