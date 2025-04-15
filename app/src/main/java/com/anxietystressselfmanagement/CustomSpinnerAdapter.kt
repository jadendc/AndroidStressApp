import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomSpinnerAdapter(
    context: Context,
    private val selectedItemLayoutRes: Int,
    private val dropdownLayoutRes: Int,
    private val items: List<String>,
    private val getCustomText: () -> String?
) : ArrayAdapter<String>(context, selectedItemLayoutRes, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(selectedItemLayoutRes, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        val customText = getCustomText()

        // If we have custom text and the selected position is the "Custom..." option
        if (customText != null && position == items.size - 1) {
            textView.text = customText
        } else {
            textView.text = getItem(position)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(dropdownLayoutRes, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)
        return view
    }
}