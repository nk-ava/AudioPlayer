package com.example.shixun;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioUtil {
    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param pcmAudioByteCount 不包括header的音频数据总长度
     * @param longSampleRate    采样率,也就是录制时使用的频率
     * @param channels          audioRecord的频道数量
     */
    private static byte[] generateWavFileHeader(long pcmAudioByteCount, long longSampleRate, int channels) {
        long totalDataLen = pcmAudioByteCount + 36; // 不包含前8个字节的WAV文件总长度
        long byteRate = longSampleRate * 2 * channels;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (2 * channels);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (pcmAudioByteCount & 0xff);
        header[41] = (byte) ((pcmAudioByteCount >> 8) & 0xff);
        header[42] = (byte) ((pcmAudioByteCount >> 16) & 0xff);
        header[43] = (byte) ((pcmAudioByteCount >> 24) & 0xff);
        return header;
    }
    public static void pcmToWav(String oldFileName,String newFileName){
        File oldPcmFile = new File(oldFileName);
        File newWavFile = new File(newFileName);
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(newWavFile);
            fis = new FileInputStream(oldPcmFile);
            byte[] wavHeaders = generateWavFileHeader(oldPcmFile.length(),44100,2);
            fos.write(wavHeaders);
            byte[] buffer = new byte[1024];
            int hasRead = 0;
            while((hasRead= fis.read(buffer))>0){
                fos.write(buffer,0,hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 根据本地文件地址获取wav音频时长
     */
    public static long getWavLength(String filePath) {
        //Log.i("AudioUtil","文件名："+filePath);
        byte[] wavdata = getBytes(filePath);
        if (wavdata != null && wavdata.length > 44) {
            int byteRate = byteArrayToInt(wavdata, 28, 31);
            int waveSize = byteArrayToInt(wavdata, 40, 43);
            return waveSize / byteRate;
        }
        return 0;
    }

    /**
     * file 2 byte数组
     */
    private static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 将byte[]转化为int
     */
    private static int byteArrayToInt(byte[] b, int start, int end) {
        return ByteBuffer.wrap(b, start, end).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
