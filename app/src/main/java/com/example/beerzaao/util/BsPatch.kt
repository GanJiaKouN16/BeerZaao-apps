package com.example.beerzaao.util

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BsPatch {

    fun apply(oldFile: File, newFile: File, patchFile: File) {
        val patchData = patchFile.readBytes()

        val header = ByteBuffer.wrap(patchData, 0, 32).order(ByteOrder.BIG_ENDIAN)
        val magic = ByteArray(4)
        header.get(magic)
        require(String(magic) == "RAW\u0000") { "Invalid patch format" }

        val ctrlLen = header.getLong()
        val diffLen = header.getLong()
        val newSize = header.getLong()

        val body = patchData.copyOfRange(32, patchData.size)

        val ctrlData = body.copyOfRange(0, ctrlLen.toInt())
        val diffData = body.copyOfRange(ctrlLen.toInt(), (ctrlLen + diffLen).toInt())
        val extraData = body.copyOfRange((ctrlLen + diffLen).toInt(), body.size)

        val oldData = oldFile.readBytes()
        val oldSize = oldData.size.toLong()
        val newData = ByteArray(newSize.toInt())

        var oldPos = 0L
        var newPos = 0L
        var ctrlIdx = 0
        var diffIdx = 0
        var extraIdx = 0

        val ctrlBuf = ByteBuffer.wrap(ctrlData).order(ByteOrder.BIG_ENDIAN)

        while (newPos < newSize) {
            val diffLenTuple = ctrlBuf.getLong(ctrlIdx); ctrlIdx += 8
            val extraLenTuple = ctrlBuf.getLong(ctrlIdx); ctrlIdx += 8
            val seek = ctrlBuf.getLong(ctrlIdx); ctrlIdx += 8

            for (i in 0 until diffLenTuple.toInt()) {
                val d = diffData[diffIdx].toInt() and 0xFF
                diffIdx++
                val oldByte = if (oldPos + i < oldSize) (oldData[(oldPos + i).toInt()].toInt() and 0xFF) else 0
                newData[(newPos + i).toInt()] = ((oldByte + d) % 256).toByte()
            }
            newPos += diffLenTuple
            oldPos += diffLenTuple

            for (i in 0 until extraLenTuple.toInt()) {
                newData[(newPos + i).toInt()] = extraData[extraIdx]
                extraIdx++
            }
            newPos += extraLenTuple
            oldPos += seek
        }

        RandomAccessFile(newFile, "rw").use { raf ->
            raf.setLength(newSize)
            raf.write(newData)
        }
    }
}
