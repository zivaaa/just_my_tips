# Буфер обмена (Clipboard)

Скопировать текст в буфер обмена для последующей вставки юзером.

```

        fun copyToClipboard(ctx : Context, text : String) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData : ClipData = ClipData.newPlainText("shared image url", text)
            clipboard.primaryClip = clipData
        }

```