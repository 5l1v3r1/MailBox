package cn.xd.mail

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import java.io.File


class MainActivity : AppCompatActivity() {
    private var smtpHost: EditText? = null
    private var smtpPort: EditText? = null
    private var smtpUsername: EditText? = null
    private var smtpPassword: EditText? = null
    private var toMail: EditText? = null
    private var subject: EditText? = null
    private var message: EditText? = null
    private var send: Button? = null
    private var filePass: EditText? = null
    private var selectOrigin: Button? = null
    private var encodeBtn: Button? = null
    private var originFilePath: EditText? = null
    private var selectEncoded: Button? = null
    private var decodeBtn: Button? = null
    private var encodedFilePath: EditText? = null
    private var md5Origin: Button? = null
    private var md5Encoded: Button? = null

    companion object {
        val TAG = MainActivity::class.java.name
        val SELECT_ORIGIN_FILE = 0
        val SELECT_ENCODE_FILE = 1
        //读写权限
        val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val REQUEST_PERMISSION_CODE = 1
    }

    private fun showFileChooser(code: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件进行处理"), code)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "请安装一个文件管理器", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            val path: String? = ContentUriUtil.getPath(this@MainActivity, uri!!)
            if (SELECT_ORIGIN_FILE == requestCode) {
                originFilePath?.setText(path)
            } else if (SELECT_ENCODE_FILE == requestCode) {
                encodedFilePath?.setText(path)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initView() {
        smtpHost = findViewById(R.id.smtpHost)
        smtpPort = findViewById(R.id.smtpPort)
        smtpUsername = findViewById(R.id.smtpUsername)
        smtpPassword = findViewById(R.id.smtpPassword)
        toMail = findViewById(R.id.toEmail)
        subject = findViewById(R.id.subject)
        message = findViewById(R.id.message)
        send = findViewById(R.id.send_mail_btn)
        filePass = findViewById(R.id.filePass)
        selectOrigin = findViewById(R.id.select_origin_btn)
        encodeBtn = findViewById(R.id.encode_btn)
        originFilePath = findViewById(R.id.origin_file_text)
        selectEncoded = findViewById(R.id.select_encoded_btn)
        decodeBtn = findViewById(R.id.decode_btn)
        encodedFilePath = findViewById(R.id.encoded_file_text)
        md5Origin = findViewById(R.id.md51)
        md5Encoded = findViewById(R.id.md52)
    }

    private fun setTestData() {
        smtpHost?.setText("smtp.163.com")
        smtpPort?.setText("25")
        smtpUsername?.setText("username")
        smtpPassword?.setText("password")
        toMail?.setText("test@gmail.com")
        subject?.setText("测试邮件")
        message?.setText("测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容")
        filePass?.setText("12345678")

    }

    private fun sendMail() {
        val pd = ProgressDialog(this@MainActivity)
        pd.setTitle("发送邮件")
        pd.setMessage("发送中...")
        pd.show()
        val host: String = smtpHost?.text.toString().replace("smtp.", "")
        val builder = MaildroidX.Builder()
                .smtp(smtpHost?.text.toString())
                .smtpUsername(smtpUsername?.text.toString())
                .smtpPassword(smtpPassword?.text.toString())
                .smtpAuthentication(true)
                .port(smtpPort?.text.toString())
                .type(MaildroidXType.HTML)
                .to(toMail?.text.toString())
                .from("""${smtpUsername?.text}@${host}""")
                .subject(subject?.text.toString())
                .body(message?.text.toString())
        val attachPath = encodedFilePath?.text.toString()
        if (attachPath.endsWith(".des") && File(attachPath).exists()) {
            builder.attachment(attachPath)
        }
        builder.onCompleteCallback(object : MaildroidX.onCompleteCallback {
            override val timeout: Long = 4000
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "Mail sent!", Toast.LENGTH_SHORT).show()
                pd.cancel()
            }

            override fun onFail(errorMessage: String) {
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                pd.cancel()
            }
        }).send()
    }

    private fun showInputDialog(title: String, msg: String) {
        val editText = EditText(this@MainActivity)
        editText.setText(msg)
        val inputDialog = AlertDialog.Builder(this@MainActivity)
        inputDialog.setTitle(title).setView(editText)
                .setNegativeButton("取消") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("复制") { dialog, which ->
                    //获取剪贴板管理器：
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    // 创建普通字符型ClipData
                    val mClipData = ClipData.newPlainText("Label", msg)
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData)
                    Toast.makeText(this@MainActivity, "复制成功", Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("添加到邮件") { dialog, which ->
                    message?.setText("${message?.text.toString()}\r\nFile md5: $msg")
                }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        initView()
        setTestData()
        md5Origin?.setOnClickListener {
            val fp = originFilePath?.text.toString()
            if (fp.isEmpty()) {
                Toast.makeText(this@MainActivity, "请输入文件路径", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val md5 = DesUtils.getMD5(fp)
            if (md5 == null) {
                Toast.makeText(this@MainActivity, "计算失败", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showInputDialog("MD5", md5)
        }
        md5Encoded?.setOnClickListener {
            val fp = encodedFilePath?.text.toString()
            if (fp.isEmpty()) {
                Toast.makeText(this@MainActivity, "请输入文件路径", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val md5 = DesUtils.getMD5(fp)
            if (md5 == null) {
                Toast.makeText(this@MainActivity, "计算失败", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showInputDialog("MD5", md5)
        }
        selectOrigin?.setOnClickListener {
            showFileChooser(SELECT_ORIGIN_FILE)
        }
        selectEncoded?.setOnClickListener {
            showFileChooser(SELECT_ENCODE_FILE)
        }
        encodeBtn?.setOnClickListener {
            val pd = ProgressDialog(this@MainActivity)
            pd.setTitle("加密中...")
            pd.show()
            val fp = originFilePath?.text.toString()
            val filePassword = filePass?.text.toString()
            val resultData = DesUtils.encryptFile(this@MainActivity, fp, filePassword)
            if (resultData != null) {
                encodedFilePath?.setText(resultData)
                Toast.makeText(this@MainActivity, "Encrypted File: $resultData.des", Toast.LENGTH_SHORT).show()
            }
            pd.cancel()
        }
        decodeBtn?.setOnClickListener {
            val pd = ProgressDialog(this@MainActivity)
            pd.setTitle("解密中...")
            pd.show()
            val fp = encodedFilePath?.text.toString()
            val filePassword = filePass?.text.toString()
            val resultData = DesUtils.decryptFile(this@MainActivity, fp, filePassword)
            if (resultData != null) {
                originFilePath?.setText(resultData)
                Toast.makeText(this@MainActivity, "Decrypted File: $resultData", Toast.LENGTH_SHORT).show()
            }
            pd.cancel()
        }
        send?.setOnClickListener {
            sendMail()
        }
    }
}

