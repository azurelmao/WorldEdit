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
public class SetCommand implements com.bta.util.CommandHandler {
    @Override
    public Command command() {
        return new Command("/set") {
            @Override
            public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] args) {
                if (args.length == 1) {
                    int[] primaryPosition = WandPlayerData.primaryPositions.get(commandSender.getPlayer().username);
                    int[] secondPosition = WandPlayerData.secondaryPositions.get(commandSender.getPlayer().username);

                    if (primaryPosition == null || secondPosition == null) {
                        commandSender.sendMessage("Positions aren't set!");
                        return true;
                    }

                    int minX = primaryPosition[0];
                    int minY = primaryPosition[1];
                    int minZ = primaryPosition[2];
                    int maxX = secondPosition[0];
                    int maxY = secondPosition[1];
                    int maxZ = secondPosition[2];

                    int temp;
                    if (minX > maxX) {
                        temp = minX;
                        minX = maxX;
                        maxX = temp;
                    }

                    if (minY > maxY) {
                        temp = minY;
                        minY = maxY;
                        maxY = temp;
                    }

                    if (minZ > maxZ) {
                        temp = minZ;
                        minZ = maxZ;
                        maxZ = temp;
                    }

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

                    WandClipboard wandClipboard = WandPlayerData.wandClipboards.computeIfAbsent(commandSender.getPlayer().username, k -> new WandClipboard());

                    if (wandClipboard.page == -1) {
                        wandClipboard.createNewPage();
                    }

                    for(int x = minX; x <= maxX; ++x) {
                        for(int y = minY; y <= maxY; ++y) {
                            for(int z = minZ; z <= maxZ; ++z) {
                                int id = commandSender.getPlayer().worldObj.getBlockId(x, y, z);
                                int meta = commandSender.getPlayer().worldObj.getBlockMetadata(x, y, z);
                                wandClipboard.putBlock(x, y, z, id, meta);
                            }
                        }
                    }

                    wandClipboard.createNewPage();

                    for(int x = minX; x <= maxX; ++x) {
                        for(int y = minY; y <= maxY; ++y) {
                            for(int z = minZ; z <= maxZ; ++z) {
                                wandClipboard.putBlock(x, y, z, id1, meta1);
                                commandSender.getPlayer().worldObj.setBlockAndMetadataWithNotify(x, y, z, id1, meta1);
                            }
                        }
                    }

                    return true;
                }


                return false;
            }

            @Override
            public boolean opRequired(String[] strings) {
                return true;
            }

            @Override
            public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {
                commandSender.sendMessage("//set <block>");
                commandSender.sendMessage("*  <block> - block to place");
            }
        };
    }
}