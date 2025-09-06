package io.github.lancelot.datamasking;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lancelot
 */
public final class SensitiveConvertor {


	private static final String NULLSTR = "null";

	/**
	 * [中文姓名] 只显示第一个汉字，其他隐藏为星号<例子：李**>
	 * 
	 * @param name
	 * @return
	 */
	public static String chineseName(String fullName) {
		if (StringUtils.isBlank(fullName)) {
			return fullName;
		}
		String name = StringUtils.left(fullName, 1);
		return StringUtils.rightPad(name, 2, "*");
	}

	/**
	 * [证件号码类]（身份证，军官证，护照等身份证明证件类） 后8位用******（6个*）代替。不足8位，直接******（6个*）代替
	 * 
	 * @param id
	 * @return
	 */
	public static String idCardNum(String id) {
		if (StringUtils.isBlank(id)) {
			return id;
		}

		if (id.length() > 8) {
			return StringUtils.left(id, 3) + "******"
					+ StringUtils.right(id, 3);
		}
		return "******";
	}

	/**
	 * [固定电话] 后四位，其他隐藏<例子：****1234>
	 * 
	 * @param num
	 * @return
	 */
	public static String fixedPhone(String phone) {
		if (StringUtils.isBlank(phone)) {
			return phone;
		}
		if (phone.length() > 4) {
			return StringUtils.leftPad(StringUtils.right(phone, 4),
					StringUtils.length(phone), "*");
		}
		return "******";
	}

	/**
	 * [手机号码类]（联系人电话，个人手机） 前3位显示，后4位显示，中间部分******（6个*）代替 不足7位直接******（6个*）代替
	 * 
	 * @param num
	 * @return
	 */
	public static String mobilePhone(String phone) {
		if (StringUtils.isBlank(phone)) {
			return phone;
		}
		if (phone.length() > 7) {
			return StringUtils.left(phone, 3) + "******"
					+ (StringUtils.right(phone, 4));
		}
		return "******";
	}

	/**
	 * [地址类] 前2位显示 ，后2位显示，中间部分******（6个*）代替 不足4位全部******（6个*）代替
	 * 
	 * @param address
	 * @return
	 */
	public static String address(String address) {
		if (StringUtils.isBlank(address)) {
			return address;
		}
		if (address.length() > 4) {
			return StringUtils.left(address, 2) + "******"
					+ StringUtils.right(address, 2);
		}
		return "******";
	}

	/**
	 * [邮箱类]******.com / (.)号之前全部******（6个*）代替，（.）号之后显示出来
	 * 
	 * @param email
	 * @return
	 */
	public static String email(String email) {
		if (StringUtils.isBlank(email)) {
			return email;
		}
		int index = email.lastIndexOf(".");
		if (index > 1) {
			return "******" + StringUtils.right(email, email.length() - index);
		}
		return "******";
	}

	/**
	 * [卡号 ] 前2位显示，后4位显示，中间部分******（6个*）代替
	 * 
	 * @param cardNum
	 * @return
	 */
	public static String bankCard(String cardNum) {
		if (StringUtils.isBlank(cardNum)) {
			return cardNum;
		}
		if (cardNum.length() > 6) {
			return StringUtils.left(cardNum, 2) + "******"
					+ StringUtils.right(cardNum, 4);
		}
		return "******";
	}

	/**
	 * [银行名] 显示前4位
	 * 
	 * @param bankName
	 * @return
	 */
	public static String bankName(String bankName) {
		if (StringUtils.isBlank(bankName)) {
			return bankName;
		}
		if (bankName.length() > 4) {
			return StringUtils.rightPad(StringUtils.left(bankName, 4),
					StringUtils.length(bankName), "*");
		}
		return StringUtils.rightPad(StringUtils.left(bankName, 1),
				StringUtils.length(bankName), "*");
	}

	/**
	 * [统一社会信用代码类（注册码，营业执照，牌照类的码） ] 前4位显示，后4位显示，中间部分******（6个*）代替
	 * 
	 * @param code
	 * @return
	 */
	public static String cnapsCode(String code) {
		if (StringUtils.isBlank(code)) {
			return code;
		}
		if (code.length() > 8) {
			return StringUtils.left(code, 4) + "******"
					+ StringUtils.right(code, 4);
		}
		return "******";
	}

	/**
	 * [银行卡有效期] 前1位，后1位，其他隐藏<例子:“0**6”>
	 * 
	 * @param num
	 * @return
	 */
	public static String cardValidDate(String date) {
		if (StringUtils.isBlank(date)) {
			return date;
		}
		return StringUtils.left(date, 1).concat(
				StringUtils.removeStart(StringUtils.leftPad(
						StringUtils.right(date, 1), StringUtils.length(date),
						"*"), "*"));
	}

	/**
	 * 全部隐藏
	 * 
	 * @param num
	 * @return
	 */
	public static String all(String data, int sensitiveSize) {
		return StringUtils.repeat("*", sensitiveSize);
	}

	/**
	 * 根据信息类型屏蔽<br>
	 * 
	 * @param type
	 *            信息类型
	 * @param key
	 *            信息key 如bankaccountno
	 * @param value
	 *            信息value 如 62121212312312
	 * @return
	 */
	public static String convertMsg(SensitiveType type, String value) {
		if (StringUtils.isBlank(value)) {
			return "";
		}
		// 返回null 时保持原样返回方便查问题
		if (NULLSTR.equalsIgnoreCase(value)) {
			return value;
		}
		switch (type) {
		case CHINESE_NAME: {
			value = SensitiveConvertor.chineseName(value);
			break;
		}
		case ID_CARD: {
			value = SensitiveConvertor.idCardNum(value);
			break;
		}
		case FIXED_PHONE: {
			value = SensitiveConvertor.fixedPhone(value);
			break;
		}
		case MOBILE_PHONE: {
			value = SensitiveConvertor.mobilePhone(value);
			break;
		}
		case ADDRESS: {
			value = SensitiveConvertor.address(value);
			break;
		}
		case EMAIL: {
			value = SensitiveConvertor.email(value);
			break;
		}
		case BANK_CARD: {
			value = SensitiveConvertor.bankCard(value);
			break;
		}
		case BANK_NAME: {
			value = SensitiveConvertor.bankName(value);
			break;
		}
		case CNAPS_CODE: {
			value = SensitiveConvertor.cnapsCode(value);
			break;
		}
		case BANK_CARD_DATE: {
			value = SensitiveConvertor.cardValidDate(value);
			break;
		}
		case ALL: {
			value = SensitiveConvertor.all(value, 3);
			break;
		}
		case NULL: {
			value = "";
			break;
		}
		}
		return value;
	}
}
