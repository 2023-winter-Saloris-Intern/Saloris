package world.saloris.donoff.util.user

import android.content.Context
import android.widget.Toast

class MakeToast {
    private var toast: Toast? = null

    fun makeToast(context: Context, message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }
}