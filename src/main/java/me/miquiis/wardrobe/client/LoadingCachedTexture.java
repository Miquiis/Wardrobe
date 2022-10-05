package me.miquiis.wardrobe.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.database.LocalCache;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.RequestSkinDownloadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class LoadingCachedTexture extends SimpleTexture {
   private static final Logger LOGGER = LogManager.getLogger();
   @Nullable
   private final File cacheFile;
   private final String skinHex;
   private final boolean legacySkin;
   @Nullable
   private final Runnable processTask;
   @Nullable
   private CompletableFuture<?> future;
   private boolean textureUploaded;

   public LoadingCachedTexture(@Nullable File cacheFileIn, String skinHex, ResourceLocation textureResourceLocation, boolean legacySkinIn, @Nullable Runnable processTaskIn) {
      super(textureResourceLocation);
      this.cacheFile = cacheFileIn;
      this.skinHex = skinHex;
      this.legacySkin = legacySkinIn;
      this.processTask = processTaskIn;
   }

   private void setImage(NativeImage nativeImageIn) {
      if (this.processTask != null) {
         this.processTask.run();
      }

      Minecraft.getInstance().execute(() -> {
         this.textureUploaded = true;
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
               this.upload(nativeImageIn);
            });
         } else {
            this.upload(nativeImageIn);
         }

      });
   }

   private void upload(NativeImage imageIn) {
      TextureUtil.prepareImage(this.getGlTextureId(), imageIn.getWidth(), imageIn.getHeight());
      imageIn.uploadTextureSub(0, 0, 0, true);
   }

   public void loadTexture(IResourceManager manager) throws IOException {
      Minecraft.getInstance().execute(() -> {
         if (!this.textureUploaded) {
            try {
               super.loadTexture(manager);
            } catch (IOException ioexception) {
               LOGGER.warn("Failed to load texture: {}", this.textureLocation, ioexception);
            }

            this.textureUploaded = true;
         }

      });

      if (this.future == null) {
         NativeImage nativeimage;
         if (this.cacheFile != null && this.cacheFile.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", (Object)this.cacheFile);
            FileInputStream fileinputstream = new FileInputStream(this.cacheFile);
            nativeimage = this.loadTexture(fileinputstream);
         } else {
            nativeimage = null;
         }

         if (nativeimage != null) {
            this.setImage(nativeimage);
         } else {
            try {
               Predicate<LocalCache<TextureCache>.Cached> cachedPredicate = cached -> {
                  return ImageUtils.byteToHex(cached.getValue().getTextureHash()).equals(skinHex);
               };
               if (!Wardrobe.getInstance().getClientTextureCache().hasCache(cachedPredicate))
               {
                  try {
                     ModNetwork.CHANNEL.sendToServer(new RequestSkinDownloadPacket(ImageUtils.hexToBytes(skinHex)));
                  } catch (Exception e)
                  {
                     e.printStackTrace();
                  }
               }
               Wardrobe.getInstance().getClientTextureCache().getCache(cachedPredicate).ifPresent(cached -> {
                  try {
                     File imageFile = File.createTempFile("temp_skin_" + skinHex, null);
                     Files.write(imageFile.toPath(), cached.getValue().getTextureBytes());
                     InputStream inputstream;
                     if (this.cacheFile != null) {
                        FileUtils.copyInputStreamToFile(new FileInputStream(imageFile), this.cacheFile);
                        inputstream = new FileInputStream(this.cacheFile);
                     } else {
                        inputstream = new FileInputStream(imageFile);
                     }
                     Minecraft.getInstance().execute(() -> {
                        NativeImage nativeimage1 = this.loadTexture(inputstream);
                        if (nativeimage1 != null) {
                           this.setImage(nativeimage1);
                        }
                     });
                  } catch (Exception exception) {
                     LOGGER.error("Couldn't load local texture " + skinHex);
                     return;
                  }
               });
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   @Nullable
   private NativeImage loadTexture(InputStream inputStreamIn) {
      NativeImage nativeimage = null;
      try {
         nativeimage = NativeImage.read(inputStreamIn);
         if (this.legacySkin) {
            nativeimage = processLegacySkin(nativeimage);
         }
      } catch (IOException ioexception) {
         LOGGER.warn("Error while loading the skin texture", (Throwable)ioexception);
      }

      return nativeimage;
   }

   private static NativeImage processLegacySkin(NativeImage nativeImageIn) {
      boolean flag = nativeImageIn.getHeight() == 32;
      if (flag) {
         NativeImage nativeimage = new NativeImage(64, 64, true);
         nativeimage.copyImageData(nativeImageIn);
         nativeImageIn.close();
         nativeImageIn = nativeimage;
         nativeimage.fillAreaRGBA(0, 32, 64, 32, 0);
         nativeimage.copyAreaRGBA(4, 16, 16, 32, 4, 4, true, false);
         nativeimage.copyAreaRGBA(8, 16, 16, 32, 4, 4, true, false);
         nativeimage.copyAreaRGBA(0, 20, 24, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(4, 20, 16, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(8, 20, 8, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(12, 20, 16, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(44, 16, -8, 32, 4, 4, true, false);
         nativeimage.copyAreaRGBA(48, 16, -8, 32, 4, 4, true, false);
         nativeimage.copyAreaRGBA(40, 20, 0, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(44, 20, -8, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(48, 20, -16, 32, 4, 12, true, false);
         nativeimage.copyAreaRGBA(52, 20, -8, 32, 4, 12, true, false);
      }

      setAreaOpaque(nativeImageIn, 0, 0, 32, 16);
      if (flag) {
         setAreaTransparent(nativeImageIn, 32, 0, 64, 32);
      }

      setAreaOpaque(nativeImageIn, 0, 16, 64, 32);
      setAreaOpaque(nativeImageIn, 16, 48, 48, 64);
      return nativeImageIn;
   }

   private static void setAreaTransparent(NativeImage image, int x, int y, int width, int height) {
      for(int i = x; i < width; ++i) {
         for(int j = y; j < height; ++j) {
            int k = image.getPixelRGBA(i, j);
            if ((k >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int l = x; l < width; ++l) {
         for(int i1 = y; i1 < height; ++i1) {
            image.setPixelRGBA(l, i1, image.getPixelRGBA(l, i1) & 16777215);
         }
      }

   }

   private static void setAreaOpaque(NativeImage image, int x, int y, int width, int height) {
      for(int i = x; i < width; ++i) {
         for(int j = y; j < height; ++j) {
            image.setPixelRGBA(i, j, image.getPixelRGBA(i, j) | -16777216);
         }
      }

   }
}