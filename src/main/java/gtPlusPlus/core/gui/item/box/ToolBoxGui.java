package gtPlusPlus.core.gui.item.box;

import gtPlusPlus.core.item.tool.misc.box.ContainerBoxBase;
import gtPlusPlus.core.lib.CORE;
import net.minecraft.util.ResourceLocation;

public class ToolBoxGui extends GuiBaseBox {
    public ToolBoxGui(ContainerBoxBase containerItem) {
        super(containerItem, new ResourceLocation(CORE.MODID, "textures/gui/schematic_rocket_GS1.png"));
    }
}
