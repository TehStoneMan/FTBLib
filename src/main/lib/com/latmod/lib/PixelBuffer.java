package com.latmod.lib;

import com.latmod.lib.util.LMColorUtils;
import com.latmod.lib.util.LMUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PixelBuffer
{
    public final int width, height;
    public final int[] pixels;

    public PixelBuffer(int w, int h)
    {
        width = w;
        height = h;
        pixels = new int[w * h];
    }

    public PixelBuffer(@Nonnull BufferedImage img)
    {
        this(img.getWidth(), img.getHeight());
        img.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public void setPixels(@Nonnull int[] rgbArray)
    {
        if(rgbArray.length == pixels.length)
        {
            System.arraycopy(rgbArray, 0, pixels, 0, pixels.length);
        }
    }

    public void setRGB(int x, int y, int col)
    {
        pixels[x + y * width] = col;
    }

    public int getRGB(int x, int y)
    {
        return pixels[x + y * width];
    }

    public void setRGB(int startX, int startY, int w, int h, @Nonnull int[] rgbArray)
    {
        int off = -1;
        for(int y = startY; y < startY + h; y++)
        {
            for(int x = startX; x < startX + w; x++)
            {
                setRGB(x, y, rgbArray[++off]);
            }
        }
    }

    public void setRGB(int startX, int startY, @Nonnull PixelBuffer buffer)
    {
        setRGB(startX, startY, buffer.width, buffer.height, buffer.pixels);
    }

    @Nonnull
    public int[] getRGB(int startX, int startY, int w, int h, @Nullable int[] rgbArray)
    {
        if(rgbArray == null || rgbArray.length != w * h)
        {
            rgbArray = new int[w * h];
        }
        int off = -1;
        for(int y = startY; y < startY + h; y++)
        {
            for(int x = startX; x < startX + w; x++)
            {
                rgbArray[++off] = getRGB(x, y);
            }
        }
        return rgbArray;
    }

    public BufferedImage toImage(int type)
    {
        BufferedImage image = new BufferedImage(width, height, type);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    public void fill(int col)
    {
        Arrays.fill(pixels, col);
    }

    public void fill(int startX, int startY, int w, int h, int col)
    {
        for(int y = startY; y < startY + h; y++)
        {
            for(int x = startX; x < startX + w; x++)
            {
                setRGB(x, y, col);
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }
        else if(o == this)
        {
            return true;
        }
        if(o instanceof PixelBuffer)
        {
            PixelBuffer b = (PixelBuffer) o;
            if(width == b.width && height == b.height)
            {
                for(int i = 0; i < pixels.length; i++)
                {
                    if(pixels[i] != b.pixels[i])
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return LMUtils.hashCode(width, height, pixels);
    }

    @Nonnull
    public PixelBuffer copy()
    {
        PixelBuffer b = new PixelBuffer(width, height);
        System.arraycopy(pixels, 0, b.pixels, 0, pixels.length);
        return b;
    }

    @Nonnull
    public PixelBuffer getSubimage(int x, int y, int w, int h)
    {
        PixelBuffer b = new PixelBuffer(w, h);
        getRGB(x, y, w, h, b.pixels);
        return b;
    }

    public void addHSB(float h, float s, float b)
    {
        LMColorUtils.addHSB(pixels, h, s, b);
    }
}