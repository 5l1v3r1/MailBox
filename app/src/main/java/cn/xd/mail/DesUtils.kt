package cn.xd.mail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec


object DesUtils {
    fun encryptFile(context: Context, inputFilePath: String, password: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "无写入权限", Toast.LENGTH_SHORT).show()
            return null
        }
        if (inputFilePath.isEmpty()) {
            Toast.makeText(context, "请输入文件路径", Toast.LENGTH_SHORT).show()
            return null
        }
        val file = File(inputFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "未找到文件$inputFilePath", Toast.LENGTH_SHORT).show()
            return null
        }
        try {
            val msg = file.readBytes()
            val dks = DESKeySpec(password.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secretKey: SecretKey = keyFactory.generateSecret(dks)
            val cipher = Cipher.getInstance("DES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val out = FileOutputStream("$inputFilePath.des")
            out.write(cipher.doFinal(msg))
            out.close()
            return "$inputFilePath.des"
        } catch (e: NoSuchAlgorithmException) {
            Toast.makeText(context, "No Such Algorithm:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: NoSuchPaddingException) {
            Toast.makeText(context, "No Such Padding:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: InvalidKeyException) {
            Toast.makeText(context, "Invalid Key:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unknown Error:" + e.message, Toast.LENGTH_SHORT).show()
        }
        return null
    }

    fun decryptFile(context: Context, inputFilePath: String, password: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "无写入权限", Toast.LENGTH_SHORT).show()
            return null
        }
        if (!inputFilePath.endsWith(".des")) {
            Toast.makeText(context, "文件必须以DES结尾", Toast.LENGTH_SHORT).show()
            return null
        }
        if (inputFilePath.isEmpty()) {
            Toast.makeText(context, "请输入文件路径", Toast.LENGTH_SHORT).show()
            return null
        }
        val file = File(inputFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "未找到文件$inputFilePath", Toast.LENGTH_SHORT).show()
            return null
        }
        try {
            val msg = file.readBytes()
            val dks = DESKeySpec(password.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secretKey: SecretKey = keyFactory.generateSecret(dks)
            val cipher = Cipher.getInstance("DES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val outPath = inputFilePath.substring(0, inputFilePath.length - 4)
            val out = FileOutputStream(outPath)
            out.write(cipher.doFinal(msg))
            out.close()
            return outPath
        } catch (e: NoSuchAlgorithmException) {
            Toast.makeText(context, "No Such Algorithm:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: NoSuchPaddingException) {
            Toast.makeText(context, "No Such Padding:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: InvalidKeyException) {
            Toast.makeText(context, "Invalid Key:" + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unknown Error:" + e.message, Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun bytesToHex(hashInBytes: ByteArray): String? {
        val sb = StringBuilder()
        for (b in hashInBytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    fun getMD5(filePath: String): String? {
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }
        var fileInputStream: FileInputStream? = null
        return try {
            val MD5: MessageDigest = MessageDigest.getInstance("MD5")
            fileInputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var length: Int
            while (fileInputStream.read(buffer).also { length = it } != -1) {
                MD5.update(buffer, 0, length)
            }
            bytesToHex(MD5.digest())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}