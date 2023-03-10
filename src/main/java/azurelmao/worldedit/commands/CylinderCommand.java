package azurelmao.worldedit.commands;

import azurelmao.worldedit.WandClipboard;
import azurelmao.worldedit.WandPlayerData;
import net.minecraft.src.Block;
import net.minecraft.src.command.Command;
import net.minecraft.src.command.CommandHandler;
import net.minecraft.src.command.CommandSender;
import net.minecraft.src.command.commands.SetBlockCommand;
import org.pf4j.Extension;

@Extension
public class CylinderCommand implements com.bta.util.CommandHandler {
    @Override
    public Command command() {
        return new Command("/cyl") {
            @Override
            public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] args) {
                if (args.length == 4) {
                    int[] primaryPosition = WandPlayerData.primaryPositions.get(commandSender.getPlayer().username);

                    if (primaryPosition == null) {
                        commandSender.sendMessage("Primary position isn't set!");
                        return true;
                    }

                    int originX = primaryPosition[0];
                    int originY = primaryPosition[1];
                    int originZ = primaryPosition[2];

                    String[] blockName = args[0].split(":");
                    int meta1 = 0;
                    if (blockName.length >= 2) {
                        meta1 = Integer.parseInt(blockName[1]);
                    }

                    int id1;
                    if (blockName[0].equals("0") || blockName[0].equals("air") || blockName[0].equals("tile.air")) {
                        id1 = 0;
                    } else {
                        Block block = SetBlockCommand.getBlock(blockName[0], meta1);

                        if (block == null) {
                            commandSender.sendMessage("Block does not exist!");
                            return true;
                        }

                        id1 = block.blockID;
                    }

                    double radius;
                    try {
                        radius = Double.parseDouble(args[1]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage("Invalid radius! Expected a decimal number.");
                        return false;
                    }
                    int blockRadius = (int) Math.round(radius);

                    int blockHeight;
                    try {
                        blockHeight = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage("Invalid height! Expected an integer number.");
                        return false;
                    }

                    WandClipboard wandClipboard = WandPlayerData.wandClipboards.computeIfAbsent(commandSender.getPlayer().username, k -> new WandClipboard());

                    if (wandClipboard.page == -1) {
                        wandClipboard.createNewPage();
                    }

                    for(int x = -blockRadius; x <= blockRadius; ++x) {
                        for(int y = 0; y < blockHeight; ++y) {
                            for(int z = -blockRadius; z <= blockRadius; ++z) {
                                if (isPointInsideCylinder(x, z, radius)) {
                                    int id = commandSender.getPlayer().worldObj.getBlockId(x+originX, y+originY, z+originZ);
                                    int meta = commandSender.getPlayer().worldObj.getBlockMetadata(x+originX, y+originY, z+originZ);
                                    wandClipboard.putBlock(x+originX, y+originY, z+originZ, id, meta);
                                }
                            }
                        }
                    }

                    wandClipboard.createNewPage();

                    for(int x = -blockRadius; x <= blockRadius; ++x) {
                        for(int y = 0; y < blockHeight; ++y) {
                            for(int z = -blockRadius; z <= blockRadius; ++z) {
                                if (isPointInsideCylinder(x, z, radius)) {
                                    wandClipboard.putBlock(x+originX, y+originY, z+originZ, id1, meta1);
                                    commandSender.getPlayer().worldObj.setBlockAndMetadataWithNotify(x+originX, y+originY, z+originZ, id1, meta1);
                                }
                            }
                        }
                    }

                    return true;
                }

                return false;
            }

            public boolean isPointInsideCylinder(int x, int z, double radius) {
                return x*x + z*z < radius*radius;
            }

            @Override
            public boolean opRequired(String[] strings) {
                return true;
            }

            @Override
            public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {
                commandSender.sendMessage("//cyl <block> <radius> <height> <raised?>");
                commandSender.sendMessage("*  <block> - block to place");
                commandSender.sendMessage("*  <radius> - cylinder radius");
                commandSender.sendMessage("*  <height> - cylinder height");
                commandSender.sendMessage("*  <raised?> - whether to shift the cylinder up");
            }
        };
    }
}