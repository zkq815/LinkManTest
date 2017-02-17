package com.meizu.linkmantest.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meizu.linkmantest.R;
import com.meizu.linkmantest.adapter.SortAdapter;
import com.meizu.linkmantest.handle.CharacterParser;
import com.meizu.linkmantest.handle.PinyinComparator;
import com.meizu.linkmantest.handle.SideBar;
import com.meizu.linkmantest.handle.SideBar.OnTouchingLetterChangedListener;
import com.meizu.linkmantest.model.LinkManBean;
import com.meizu.linkmantest.model.PhoneModel;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/**
 * 
 * @author Mr.Z
 */
public class MainActivity extends Activity {
	private Context				context	= MainActivity.this;
	private ListView			sortListView;
	private SideBar				sideBar;
	private TextView			dialog;
	private SortAdapter			adapter;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser		characterParser;
	private List<PhoneModel>	SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator	pinyinComparator;

	ArrayList<LinkManBean> contacts = new ArrayList<LinkManBean>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<PhoneModel> filledData(String[] date, String[] imgData) {
		List<PhoneModel> mSortList = new ArrayList<PhoneModel>();

		for (int i = 0; i < date.length; i++) {
			PhoneModel sortModel = new PhoneModel();
			sortModel.setImgSrc(imgData[i]);
			sortModel.setName(date[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 为ListView填充数据
	 *
	 * @param dataList
	 * @return
	 */
	private List<PhoneModel> filledDataNew(ArrayList<LinkManBean> dataList) {
		List<PhoneModel> mSortList = new ArrayList<PhoneModel>();

		for (int i = 0; i < dataList.size(); i++) {
			PhoneModel sortModel = new PhoneModel();
//			sortModel.setImgSrc(imgData[i]);
			sortModel.setName(dataList.get(i).getName());
			// 汉字转换成拼音
//			String pinyin = characterParser.getSelling(dataList.get(i).getName());
			String pinyin = getPinYin(dataList.get(i).getName());

			if(dataList.get(i).getName().contains("魅族")){
				Log.e("zkq","包含魅族字眼=="+pinyin);
			}
//			String sortString = pinyin.substring(0, 1).toUpperCase();
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}


	private void initViews() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				Toast.makeText(context, ((PhoneModel) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
			}
		});
		contacts = new ArrayList<LinkManBean>();
		contacts = readContacts(contacts);

//		SourceDateList = filledData(getResources().getStringArray(R.array.date), getResources().getStringArray(R.array.img_src_data));
		SourceDateList = filledDataNew(contacts);
		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(context, SourceDateList);
		sortListView.setAdapter(adapter);
	}

	// 读取设备联系人的一般方法。大致流程就是这样，模板化的操作代码。
	private ArrayList<LinkManBean> readContacts(ArrayList<LinkManBean> contacts) {
		Uri uri = Uri.parse("content://com.android.contacts/contacts");
		ContentResolver reslover = this.getContentResolver();

		// 在这里我们给query传递进去一个SORT_KEY_PRIMARY。
		// 告诉ContentResolver获得的结果安装联系人名字的首字母有序排列。
		Cursor cursor = reslover.query(uri, null, null, null,
				android.provider.ContactsContract.Contacts.SORT_KEY_PRIMARY);

		while (cursor.moveToNext()) {

			// 联系人ID
			String id = cursor
					.getString(cursor
							.getColumnIndex(android.provider.ContactsContract.Contacts._ID));

			// Sort Key，读取的联系人按照姓名从 A->Z 排序分组。
			String sort_key_primary = cursor
					.getString(cursor
							.getColumnIndex(android.provider.ContactsContract.Contacts.SORT_KEY_PRIMARY));

			// 获得联系人姓名
			String name = cursor
					.getString(cursor
							.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));

			LinkManBean mContact = new LinkManBean();
			mContact.id = id;
			mContact.name = name;
			mContact.sort_key_primary = sort_key_primary;

			// 获得联系人手机号码
			Cursor phone = reslover.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
							+ id, null, null);

			// 取得电话号码(可能存在多个号码)
			// 因为同一个名字下，用户可能存有一个以上的号，
			// 遍历。
			ArrayList<String> phoneNumbers = new ArrayList<String>();
			while (phone.moveToNext()) {
				int phoneFieldColumnIndex = phone
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				String phoneNumber = phone.getString(phoneFieldColumnIndex);
				phoneNumbers.add(phoneNumber);
				break;
			}

			mContact.phoneNumbers = phoneNumbers;

			contacts.add(mContact);
		}

		return contacts;
	}

	public static StringBuffer sb = new StringBuffer();

	/**
	 * 获取汉字字符串的首字母，英文字符不变
	 * 例如：阿飞→af
	 */
	public String getPinYinHeadChar(String chines) {
		sb.setLength(0);
		char[] chars = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] > 128) {
				try {
					sb.append(PinyinHelper.toHanyuPinyinStringArray(chars[i], defaultFormat)[0].charAt(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sb.append(chars[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * 获取汉字字符串的第一个字母
	 */
	public String getPinYinFirstLetter(String str) {
		sb.setLength(0);
		char c = str.charAt(0);
		String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
		if (pinyinArray != null) {
			sb.append(pinyinArray[0].charAt(0));
		} else {
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * 获取汉字字符串的汉语拼音，英文字符不变
	 */
	public String getPinYin(String chines) {
		sb.setLength(0);
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					sb.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sb.append(nameChar[i]);
			}
		}
		return sb.toString();
	}

}
