package io.github.lancelot.datamasking;

/**
 * 敏感数据类型定义
 * @author lancelot
 */
public enum SensitiveType {

	/**
	 * 中文名
	 */
	CHINESE_NAME("CHINESE_NAME"),

	/**
	 * 身份证号
	 */
	ID_CARD("ID_CARD"),

	/**
	 * 座机号
	 */
	FIXED_PHONE("FIXED_PHONE"),

	/**
	 * 手机号
	 */
	MOBILE_PHONE("MOBILE_PHONE"),

	/**
	 * 地址
	 */
	ADDRESS("ADDRESS"),

	/**
	 * 电子邮件
	 */
	EMAIL("EMAIL"),

	/**
	 * 银行卡
	 */
	BANK_CARD("BANK_CARD"),

	/**
	 * 银行名
	 */
	BANK_NAME("BANK_NAME"),

	/**
	 * 公司开户银行联号
	 */
	CNAPS_CODE("CNAPS_CODE"),

	/**
	 * 银行卡有效期
	 */
	BANK_CARD_DATE("BANK_CARD_DATE"),

	/**
	 * 全部隐藏
	 */
	ALL("ALL"),

	/**
	 * key:value清除不显示
	 */
	NULL("NULL");

	private final String name;

	/**
	 * 构造方法
	 * @param name 枚举的描述名称
	 */
	SensitiveType(String name) {
		this.name = name;
	}

	/**
	 * 获取枚举的描述名称
	 * @return 描述名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 根据描述名称获取对应的枚举值（忽略大小写）
	 * @param name 枚举的描述名称
	 * @return 对应的枚举值，如果未找到则返回null
	 */
	public static SensitiveType getByNameIgnoreCase(String name) {
		if (name == null) return null;

		for (SensitiveType type : values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}