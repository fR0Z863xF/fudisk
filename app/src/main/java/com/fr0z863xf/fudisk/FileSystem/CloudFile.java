package com.fr0z863xf.fudisk.FileSystem;

import static android.content.Context.MODE_PRIVATE;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudFile {
    /*Example File Structure
           head         mode       enc      domain      region        frag     year     M D       taskId       data       tail
       | 52 10 72 99 |   00    |   00   |    00    |      00     |     00    | 14 18 | 08 05 | 11 22 33 44 | ....... | C5 16 C5 90 |
                       0=D 1=S           0=s 1=s-cd   ap-shanghai     bool     (L1)     L2        int,L3
     */
    /*
      data chunk header(mode=0)   timestamp,long(frag=0)     t2,.......t_n(frag!=0)
       |      D2 E5      |  11 22 33 44 55 66 77 88 | 11 22 33 44 55 66 77 88 |

      data chunk header(mode=1)   timestamp,long(frag=0)     t2,.......t_n(frag!=0)
       |      D2 E5      |  11 22 33 44 55 66 77 88 | 11 22 33 44 55 66 77 88 |

     mode=1 ,S ,not implemented
     */
    /*
    private String name;
    private Integer storageType; //0=D 1=S
    private Boolean encrypted;
    private Boolean fragmented;
    private String externalDomain;
    private String externalPath;

     */

    public static class FileHeader {
        byte mode;
        boolean encrypted;
        boolean fragmented;
        String externalDomain;
        String region;
        int year;
        short month;
        short day;
        int taskId;

        FileHeader(byte mode, boolean encrypted, boolean fragmented, String externalDomain, String region,
                   int year, short month, short day, int taskId) {
            this.mode = mode;
            this.encrypted = encrypted;
            this.fragmented = fragmented;
            this.externalDomain = externalDomain;
            this.region = region;
            this.year = year;
            this.month = month;
            this.day = day;
            this.taskId = taskId;
        }
    }

    public static class DataChunk {
        List<Long> timestamps;


        DataChunk(List<Long> timestamps) {
            this.timestamps = timestamps;
        }
    }

    public FileHeader header;
    public DataChunk dataChunk;

    public File file;

    public CloudFile(FileHeader header, DataChunk dataChunk, File file) {
        this.header = header;
        this.dataChunk = dataChunk;
        this.file = file;
    }

    public FileHeader getHeader() {
        return header;
    }

    public DataChunk getDataChunks() {
        return dataChunk;
    }

    public static CloudFile parseFile(String filePath) throws IOException {
        File file = new File(filePath);

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // 读取文件头
            byte[] headerBytes = new byte[17];
            raf.readFully(headerBytes);

            // 解析文件头
            //int mode = headerBytes[4] & 0xFF;
            byte mode = headerBytes[4];
            boolean encrypted = (headerBytes[5] & 0xFF) != 0;
            boolean fragmented = (headerBytes[8] & 0xFF) != 0;

            // 根据外部的映射转换 domain 和 region
            /*
            Map<Byte, String> domainMap = new HashMap<>(); // 需要填充实际的映射
            Map<Byte, String> regionMap = new HashMap<>(); // 需要填充实际的映射
            domainMap.put((byte)0x00,"s");
            domainMap.put((byte)0x01,"s-chengdu");
            regionMap.put((byte)0x00,"ap-shanghai");

            String externalDomain = domainMap.get(headerBytes[6]);
            String region = regionMap.get(headerBytes[7]);*/

            String externalDomain = "s";
            if (headerBytes[6] == 0x01) {
                externalDomain = "s-chengdu";
            }
            String region = "ap-shanghai";

            /*

            int year = ((headerBytes[9] & 0xFF) << 8) | (headerBytes[10] & 0xFF);
            int month = headerBytes[11] & 0xFF;
            int day = headerBytes[12] & 0xFF;
            int taskId = ((headerBytes[13] & 0xFF) << 24) | ((headerBytes[14] & 0xFF) << 16) |
                    ((headerBytes[15] & 0xFF) << 8) | (headerBytes[16] & 0xFF);*/

            int year = headerBytes[9] & 0xFF;
            year = year * 100 + (headerBytes[10] & 0xFF);
            short month = (short) (headerBytes[11] & 0xFF);
            short day = (short) (headerBytes[12] & 0xFF);
            int taskId = ((headerBytes[13] & 0xFF) << 24) | ((headerBytes[14] & 0xFF) << 16) |
                    ((headerBytes[15] & 0xFF) << 8) | (headerBytes[16] & 0xFF);

            FileHeader header = new FileHeader(mode, encrypted, fragmented, externalDomain, region, year, month, day, taskId);

            // 解析数据块
            DataChunk dataChunk;

            long fileLength = raf.length();
            raf.seek(17); // 跳过文件头

            if (raf.getFilePointer() < fileLength - 4) {
                byte[] chunkHeader = new byte[2];
                raf.readFully(chunkHeader);


                List<Long> timestamps = new ArrayList<>();

                if (chunkHeader[0] == (byte)0xD2 && chunkHeader[1] == (byte)0xE5) {
                    // 读取时间戳 (8字节)
                    while (raf.getFilePointer() + 8 <= fileLength - 4) {
                        timestamps.add(raf.readLong());
                    }
                    dataChunk = new DataChunk(timestamps);
                } else {
                    throw new IOException("Invalid chunk header");
                }

                // 读取数据项
                /*
                while (raf.getFilePointer() < fileLength - 4) {
                    byte[] dataItem = new byte[2]; // 示例读取每项2字节，实际情况可能需要调整
                    raf.readFully(dataItem);
                    dataItems.add(dataItem);
                }*/


            } else {
                throw new IOException("文件格式错误");
            }

            // 检查文件尾部标识符
            byte[] endMarker = { (byte)0xC5, (byte)0x16, (byte)0xC5, (byte)0x90 };
            byte[] fileEndMarker = new byte[4];
            raf.seek(fileLength - 4);
            raf.readFully(fileEndMarker);

            boolean isEndMarkerValid = true;
            for (int i = 0; i < endMarker.length; i++) {
                if (fileEndMarker[i] != endMarker[i]) {
                    isEndMarkerValid = false;
                    break;
                }
            }

            if (!isEndMarkerValid) {
                throw new IOException("Invalid file end marker");
            }

            return new CloudFile(header, dataChunk, file);
        }
    }

    public static CloudFile newFile(byte mode, boolean encrypted, boolean fragmented,
                                    String externalDomain, String region, int year,
                                    short month, short day, int taskId,
                                    List<Long> timestamps) {
        FileHeader header = new FileHeader(mode, encrypted, fragmented, externalDomain, region, year, month, day, taskId);
        DataChunk dataChunk = new DataChunk(timestamps);
        return new CloudFile(header, dataChunk , null);
    }

    public void serializeToFile(String fileName) throws IOException {
        fileName = fileName + ".fudisk";

        Log.i("CloudFile", "Serializing file " + fileName);
        try (RandomAccessFile raf = new RandomAccessFile(new File(FileManager.getInstance(null).dataDir,fileName), "rw")) {
            // 写入文件头
            raf.write(new byte[]{(byte) 0x52, (byte) 0x10, (byte) 0x72, (byte) 0x99}); // 固定的前导字节
            raf.writeByte(header.mode);
            raf.writeByte(header.encrypted ? 0x01 : 0x00);// 预留字节

            // 写入 externalDomain 和 region 的映射值
            if (header.externalDomain.equals("s-chengdu")) {
                raf.writeByte(0x01);
            } else {
                raf.writeByte(0x00); // 默认是 "s"
            }

            if (header.region.equals("ap-shanghai")) {
                raf.writeByte(0x00);
            } else {
                raf.writeByte(0x01);
            }

            raf.writeByte(header.fragmented ? 0x01 : 0x00);


            // 写入日期和任务ID
            //raf.writeShort(header.year);
            raf.writeByte((byte) (header.year / 100));
            raf.writeByte((byte) (header.year % 100));
            raf.writeByte((byte) header.month);
            raf.writeByte((byte) header.day);

            raf.writeByte((byte) (header.taskId >> 24));
            raf.writeByte((byte) (header.taskId >> 16));
            raf.writeByte((byte) (header.taskId >> 8));
            raf.writeByte((byte) (header.taskId));

            // 写入数据块
            raf.write(new byte[]{(byte) 0xD2, (byte) 0xE5}); // 数据块头部
            for (Long timestamp : dataChunk.timestamps) {
                raf.writeLong(timestamp);
            }

            // 写入文件尾标识符
            raf.write(new byte[]{(byte) 0xC5, (byte) 0x16, (byte) 0xC5, (byte) 0x90});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDate() {
        return String.valueOf(this.header.year) + "-" + String.valueOf(this.header.month) + "-" + String.valueOf(this.header.day);
    }

    public String getLink() {
        return "https://s.100tifen.com/media/tk/exercise/" + String.valueOf(this.header.year) + "/" + (this.header.month >=10 ? String.valueOf(this.header.month) : "0" + String.valueOf(this.header.month)) + "-" + (this.header.day >=10 ? String.valueOf(this.header.day) : "0" + String.valueOf(this.header.day)) + "/t" + String.valueOf(this.header.taskId) + "/" + this.file.getName().split(".fudisk")[0];
    }


}
