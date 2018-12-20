package com.zivaaa18.imagestorageapp

import android.content.Context
import android.content.Intent
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zivaaa18.imagestorageapp.models.SharedImage
import com.zivaaa18.imagestorageapp.presenters.ContentPresenter
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.inc_add_image.*


class ContentActivity : AppCompatActivity(), ContentPresenter.ContentView {

    private lateinit var adapter: Adapter
    private lateinit var presenter: ContentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)


        presenter = ContentPresenter(this)
        presenter.setup()


        adapter = Adapter(presenter)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        addNewImageBtn.setOnClickListener {
            goToAddImage()
        }
    }

    override fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun getAppContext(): Context {
        return applicationContext
    }

    override fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onGotSharedImages(sharedImages: List<SharedImage>) {
        adapter.setData(sharedImages)
    }

    override fun onClearSharedImages() {
        adapter.setData(listOf())
    }


    override fun goToAddImage() {
        startActivityForResult(Intent(this, ImageLoadingActivity::class.java), ContentPresenter.CODE_IMAGE_LOADED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) {
            return true
        }

        when(item.itemId) {
            R.id.itemLogout -> {
                presenter.logout()
            }
        }
        return true
    }

    class Adapter(val contentPresenter: ContentPresenter) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private var data: List<SharedImage> = listOf()

        fun setData(sharedImages: List<SharedImage>) {
            data = sharedImages
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            lateinit var sharedImage: SharedImage

            var imageView: ImageView = view.findViewById(R.id.sharedImage)
            var linkView: TextView = view.findViewById(R.id.link)
            var copyBtn: ImageButton = view.findViewById(R.id.copyBtn)
            var moreBtn: ImageButton = view.findViewById(R.id.moreBtn)

            init {
                copyBtn.setOnClickListener {
                    clipBoardCopy()
                }

                moreBtn.setOnClickListener {
                    showPopup()
                }
            }

            private fun showPopup() {
                val menu = PopupMenu(itemView.context, moreBtn)
                menu.inflate(R.menu.image_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.itemCopy -> {
                            clipBoardCopy()
                        }
                        R.id.itemDelete -> {
                            delete()
                        }
                    }
                    true
                }
                menu.show();
            }

            private fun clipBoardCopy() {
                Tools.copyToClipboard(itemView.context, sharedImage.getApiPath())
                Toast.makeText(itemView.context, R.string.copied, Toast.LENGTH_SHORT).show()
            }

            private fun delete() {
                Toast.makeText(itemView.context, "deleting...", Toast.LENGTH_SHORT).show()
                contentPresenter.deleteImage(sharedImage)
            }


            fun setImage(img: SharedImage) {
                this.sharedImage = img
                linkView.text = img.getApiPath()
                Picasso.get().load(img.getApiPath()).into(imageView, object : Callback {
                    override fun onSuccess() {}

                    override fun onError(e: Exception?) {
                        imageView.setImageResource(R.drawable.ic_error)
                    }
                })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.shared_item, parent, false)
            val holder = ViewHolder(view)
            holder.setImage(data[viewType])
            return holder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setImage(data[position])
        }
    }
}
