package reknew.focus.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import reknew.focus.R;

public class ListViewAdapter extends BaseAdapter {

	private Context context;
	private List<String> nameList;
	private List<Drawable> imageList;
	private List<Boolean> selectedList;
	// 用来导入布局

	// Constructor
	public ListViewAdapter(AppInfo info, Context context) {
		this.context = context;
		this.nameList = info.nameList;
		this.imageList = info.imageList;
		this.selectedList = info.selectedList;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.list_view_item, null);
			viewHolder.text = (TextView) convertView.findViewById(R.id.text);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
			// 为view设置标签
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		try {
			viewHolder.text.setText(nameList.get(position));
			viewHolder.image.setImageDrawable(imageList.get(position));
			viewHolder.checkBox.setChecked(selectedList.get(position));
		} catch (Exception e) {
			viewHolder.text.setText("(ノ-_-)ノ");
			viewHolder.image.setImageResource(R.mipmap.ic_launcher);
			viewHolder.checkBox.setChecked(false);
		}
		return convertView;
	}

	public List<Boolean> getSelectedList() {
		return selectedList;
	}

	@Override
	public int getCount() {
		return nameList.size();
	}

	@Override
	public Object getItem(int position) {
		return nameList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		public TextView text;
		public ImageView image;
		public CheckBox checkBox;
	}
}