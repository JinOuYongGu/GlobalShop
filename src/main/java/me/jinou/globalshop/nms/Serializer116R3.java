package me.jinou.globalshop.nms;

import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;

/**
 * @author 69142
 */
public class Serializer116R3 implements ItemStackSerializer {

    @Override
    public String toBase64(ItemStack is) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nmsItem.save(nbtTagCompound);

        try {
            NBTCompressedStreamTools.a(nbtTagCompound, (DataOutput) dataOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @Override
    public ItemStack fromBase64(String base64) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));

        NBTTagCompound nbtTagCompound = null;
        try {
            nbtTagCompound = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}
