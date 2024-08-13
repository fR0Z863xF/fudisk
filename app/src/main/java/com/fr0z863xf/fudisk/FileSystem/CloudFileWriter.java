/*package com.fr0z863xf.fudisk.FileSystem;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class CloudFileWriter {

    public static final byte[] chunkHeader = {(byte) 0xd2,(byte) 0xe5};

    public static void writeFile(String filePath, CloudFile cloudFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            // 写入文件头
            writeFileHeader(raf, cloudFile.getHeader());

            // 写入数据块
            writeDataChunks(raf, cloudFile.getDataChunks());

            // 写入文件尾部标识符
            writeFileEndMarker(raf);
        }
    }

    private static void writeFileHeader(RandomAccessFile raf, CloudFile.FileHeader header) throws IOException {
        // 文件头固定大小 17 字节
        byte[] fileHeader = new byte[17];

        fileHeader[0] = (byte) 0x52;
        fileHeader[1] = (byte) 0x10;
        fileHeader[2] = (byte) 0x72;
        fileHeader[3] = (byte) 0x99;

        // 写入 mode
        fileHeader[4] = header.mode;

        // 写入 encrypted
        fileHeader[5] = (byte) (header.encrypted ? 1 : 0);

        // 写入 fragmented
        fileHeader[8] = (byte) (header.fragmented ? 1 : 0);

        // 写入 externalDomain 和 region
        // 假设映射表如下
        byte domainByte = (header.externalDomain.equals("s-chengdu")) ? (byte) 0x01 : (byte) 0x00;
        byte regionByte = (header.region.equals("ap-shanghai")) ? (byte) 0x00 : (byte) 0x01;

        fileHeader[6] = domainByte;
        fileHeader[7] = regionByte;

        // 写入 year (两个字节)
        fileHeader[9] = (byte) (header.year >> 8);
        fileHeader[10] = (byte) (header.year & 0xFF);

        // 写入 month
        fileHeader[11] = (byte) header.month;

        // 写入 day
        fileHeader[12] = (byte) header.day;

        // 写入 taskId (四个字节)
        fileHeader[13] = (byte) (header.taskId >> 24);
        fileHeader[14] = (byte) (header.taskId >> 16);
        fileHeader[15] = (byte) (header.taskId >> 8);
        fileHeader[16] = (byte) (header.taskId & 0xFF);

        raf.write(fileHeader);
    }

    private static void writeDataChunks(RandomAccessFile raf, List<CloudFile.DataChunk> dataChunks) throws IOException {
        for (CloudFile.DataChunk chunk : dataChunks) {
            // 写入数据块头
            raf.write(chunkHeader);

            // 写入时间戳
            for (Long timestamp : chunk.timestamps) {
                raf.writeLong(timestamp);
            }
        }
    }

    private static void writeFileEndMarker(RandomAccessFile raf) throws IOException {
        byte[] endMarker = { (byte) 0xC5, (byte) 0x16, (byte) 0xC5, (byte) 0x90 };
        raf.write(endMarker);
    }
}


 */