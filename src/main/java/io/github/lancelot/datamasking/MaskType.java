package io.github.lancelot.datamasking;

/**
 * 敏感数据类型定义。
 *
 * <p>每个类型对应 {@link Maskers} 中的一条内置脱敏规则，可通过
 * {@link MaskEngine#register(String, MaskType)} 与字段名绑定。</p>
 *
 * @author lancelot
 */
public enum MaskType {

	/**
	 * 中文名：只显示第一个汉字，其余隐藏为星号，如 李四 → 李*
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
	 * key:value 清除不显示
	 */
	NULL("NULL");

	private final String name;

	/**
	 * 构造方法
	 *
	 * @param name 枚举的描述名称
	 */
	MaskType(String name) {
		this.name = name;
	}

	/**
	 * 获取枚举的描述名称
	 *
	 * @return 描述名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 根据描述名称获取对应的枚举值（忽略大小写）
	 *
	 * @param name 枚举的描述名称
	 * @return 对应的枚举值，如果未找到则返回 null
	 */
	public static MaskType getByNameIgnoreCase(String name) {
		if (name == null) {
			return null;
		}
		for (MaskType type : values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}